/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CorteService
 * Descripción: Hojas de corte: despiece de la OP (o de una ficha para 1 unidad) agrupado por material y tipo de corte,
 *
 * @author PLMD - Pedro Luis Mamani Delgado
 * @author MGML - Maribel Gloria Mamani Lopez
 * @author AMCS - Alessandro Mijail Calcina Sanchez
 * @author DRJB - Danny Rodrigo Jove Benites
 * @version 1.0
 * @since 2026
 *
 * Identificación de autoría: equipo PLMD, MGML, AMCS y DRJB.
 * Conservar esta atribución en copias y modificaciones del código fuente.
 * =======================================================================
 */
package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.MaterialRepository;
import pe.erp.sofas.service.calculo.*;

import java.util.*;

/**
 * Hojas de corte: despiece de la OP (o de una ficha para 1 unidad) agrupado por material y tipo de corte,
 * con la opcion de optimizar junto con las demas OP activas que usan el mismo material.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorteService {

    /** Origen de la hoja: una OP activa o una ficha (1 unidad). */
    public record Fuente(OrdenProduccion op, FichaTecnica ficha) {
        public String codigo() {
            return op != null ? op.getCodigo() : ficha.getEtiqueta();
        }

        public String titulo() {
            return op != null ? op.getCodigo() + " · " + op.getProductoNombre() + " ×" + op.getCantidad()
                    : ficha.getEtiqueta() + " · " + ficha.getNombre() + " (1 unidad)";
        }
    }

    /** Material de una hoja con sus piezas. */
    public record MaterialCorte(Material material, TipoCorte tipo, List<Pieza> piezas) {
        public int numeroPiezas() {
            return piezas.stream().mapToInt(Pieza::cantidad).sum();
        }
    }

    private final ProduccionService produccionService;
    private final FichaService fichaService;
    private final MaterialRepository materialRepository;
    private final InventarioService inventarioService;

    public List<Fuente> fuentes() {
        List<Fuente> l = new ArrayList<>();
        produccionService.activas().stream().filter(o -> !o.isSinFicha()).forEach(o -> l.add(new Fuente(o, null)));
        fichaService.todas().stream().filter(f -> f.getEstado() == EstadoFicha.APROBADA).forEach(f -> l.add(new Fuente(null, f)));
        return l;
    }

    /** Materiales a cortar de la fuente (opcionalmente con las piezas de las demas OP activas del mismo material). */
    public List<MaterialCorte> materiales(Fuente f, boolean conjunto) {
        Map<String, MaterialCorte> m = new LinkedHashMap<>();
        agregar(m, f);
        if (conjunto && f.op() != null) {
            Set<String> mats = m.keySet();
            for (OrdenProduccion otra : produccionService.activas()) {
                if (otra.getId().equals(f.op().getId()) || otra.isSinFicha()) continue;
                Map<String, MaterialCorte> o = new LinkedHashMap<>();
                agregar(o, new Fuente(otra, null));
                for (var e : o.entrySet()) {
                    if (!mats.contains(e.getKey())) continue;
                    MaterialCorte base = m.get(e.getKey());
                    List<Pieza> piezas = new ArrayList<>(base.piezas());
                    piezas.addAll(e.getValue().piezas());
                    m.put(e.getKey(), new MaterialCorte(base.material(), base.tipo(), piezas));
                }
            }
        }
        return new ArrayList<>(m.values());
    }

    private void agregar(Map<String, MaterialCorte> acc, Fuente f) {
        FichaTecnica ficha = f.op() != null ? produccionService.ficha(f.op()) : f.ficha();
        if (ficha == null) return;
        int cantidad = f.op() != null ? f.op().getCantidad() : 1;
        Map<String, String> sust = f.op() != null ? f.op().getSustituciones() : Map.of();
        TipoTela tipoTela = f.op() != null && f.op().getTela() != null ? f.op().getTela().getTipo() : null;
        List<FichaTecnica> partes = Boolean.TRUE.equals(ficha.getCompuesta()) ? fichaService.hijas(ficha) : List.of(ficha);
        for (FichaTecnica parte : partes) {
            Despiece.Sofa s = sofa(parte, f.op(), tipoTela, f.codigo());
            for (var e : Despiece.piezas(s).entrySet()) {
                String codigo = sust.getOrDefault(e.getKey(), e.getKey());
                if (Explosion.QUITAR.equals(codigo)) continue;
                if (e.getKey().equals(Despiece.TELA) && tipoTela == null) codigo = telaDeFicha(parte, e.getKey());
                Material mat = materialRepository.findByCodigo(codigo).orElse(null);
                if (mat == null) continue;
                TipoCorte tipo = TipoCorte.paraCategoria(mat.getCategoria(), mat.getCodigo());
                if (tipo == null) continue;
                List<Pieza> piezas = new ArrayList<>();
                boolean giroTela = tipo != TipoCorte.TELA || TipoTela.porMaterial(mat.getCodigo()).permiteGiro();
                for (Pieza p : e.getValue()) piezas.add(p.conCantidad(p.cantidad() * cantidad).conGiro(p.girable() && giroTela));
                MaterialCorte prev = acc.get(codigo);
                if (prev != null) piezas.addAll(0, prev.piezas());
                acc.put(codigo, new MaterialCorte(mat, tipo, piezas));
            }
        }
    }

    /** Material de tela que usa la ficha (MP-003 por defecto, MP-008 en el escandinavo...). */
    private String telaDeFicha(FichaTecnica f, String defecto) {
        return f.getLineas().stream().map(LineaBOM::getMaterial).filter(Material::isTela).map(Material::getCodigo)
                .findFirst().orElse(defecto);
    }

    private Despiece.Sofa sofa(FichaTecnica f, OrdenProduccion op, TipoTela tela, String origen) {
        boolean usarMedidas = op != null && op.getLargo() != null && op.getLargo() > 0 && !Boolean.TRUE.equals(f.getCompuesta());
        double L = usarMedidas ? op.getLargo() : f.getLargo();
        double F = usarMedidas && op.getFondo() != null ? op.getFondo() : f.getFondo();
        double A = usarMedidas && op.getAlto() != null ? op.getAlto() : f.getAlto();
        int n = usarMedidas && op.getCuerpos() != null ? op.getCuerpos() : f.getCuerpos();
        boolean chaise = Boolean.TRUE.equals(f.getChaise()) || (op != null && op.getExtras().stream()
                .anyMatch(x -> "MP-001".equals(x.getMaterialCodigo()) && x.getCantidad() >= 10));
        if (chaise && !Boolean.TRUE.equals(f.getChaise())) F = F + 65;
        boolean giro = tela == null || tela.permiteGiro();
        if (tela == null) {
            String t = telaDeFicha(f, Despiece.TELA);
            TipoTela tt = TipoTela.porMaterial(t);
            giro = tt == null || tt.permiteGiro();
        }
        return new Despiece.Sofa(L, F, A, n, chaise, "Puff".equals(f.getTipoCorte()), giro, origen);
    }

    /** Metros de tela segun el plano de corte optimizado (rollo de 140 cm) para la ficha, la cantidad y el tipo de tela. */
    public double metrosPlano(FichaTecnica ficha, int cantidad, TipoTela tipo) {
        List<FichaTecnica> partes = Boolean.TRUE.equals(ficha.getCompuesta()) ? fichaService.hijas(ficha) : List.of(ficha);
        List<Pieza> piezas = new ArrayList<>();
        for (FichaTecnica parte : partes) {
            Despiece.Sofa s = sofa(parte, null, tipo, parte.getEtiqueta());
            for (Pieza p : Despiece.piezas(s).getOrDefault(Despiece.TELA, List.of())) piezas.add(p.conCantidad(p.cantidad() * cantidad));
        }
        if (piezas.isEmpty()) return 0;
        return Corte.optimizar(TipoCorte.TELA, piezas, Corte.Estrategia.MEJOR_LADO).metros();
    }

    /** Lo que estima la ficha (cantidad bruta) para el material en la fuente. */
    public double estimadoFicha(Fuente f, Material m) {
        Explosion.Resultado r = f.op() != null ? produccionService.requerimiento(f.op()) : fichaService.explotar(f.ficha());
        if (r == null) return 0;
        return r.porMaterial().stream().filter(p -> p.material().getCodigo().equals(m.getCodigo()))
                .mapToDouble(Explosion.PorMaterial::cantidad).sum();
    }

    public Corte.Resultado optimizar(MaterialCorte mc, Corte.Estrategia estrategia) {
        return Corte.optimizar(mc.tipo(), mc.piezas(), estrategia);
    }

    public Corte.Resultado1D optimizarMadera(MaterialCorte mc) {
        return Corte.optimizar1D(mc.piezas(), TipoCorte.MADERA.ancho());
    }

    /** Cantidad "usada" en la unidad del material (planchas, metros o pies tablares). */
    public static double usado(MaterialCorte mc, Corte.Resultado r) {
        return switch (mc.tipo()) {
            case TELA -> r.metros();
            default -> r.laminas();
        };
    }

    /** Registra el sobrante reutilizable como "Retazo" en el kardex (no altera el saldo). */
    @Transactional
    public void guardarRetazo(Material m, double cantidad, String referencia, String detalle) {
        if (cantidad <= 0) throw new NegocioException("No hay sobrante reutilizable para guardar.");
        Material fresco = materialRepository.findById(m.getId()).orElseThrow();
        inventarioService.registrar(fresco, TipoMovimiento.RETAZO, cantidad, referencia, "Retazo reutilizable · " + detalle);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
