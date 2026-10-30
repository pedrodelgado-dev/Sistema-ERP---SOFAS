/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: FichaService
 * Descripción: Fichas tecnicas con versionado: editar una ficha que no esta en Borrador crea automaticamente la siguiente
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
import pe.erp.sofas.core.AppClock;
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.FichaTecnicaRepository;
import pe.erp.sofas.repository.MaterialRepository;
import pe.erp.sofas.repository.OrdenProduccionRepository;
import pe.erp.sofas.service.calculo.Explosion;
import pe.erp.sofas.service.calculo.Medidas;
import pe.erp.sofas.service.calculo.Opciones;

import java.util.*;
import java.util.function.Consumer;

/**
 * Fichas tecnicas con versionado: editar una ficha que no esta en Borrador crea automaticamente la siguiente
 * version en Borrador con el cambio. Las OP en curso conservan la version con la que se planificaron.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FichaService {

    /** Resultado de una edicion: la ficha editada y si se creo una version nueva. */
    public record Edicion(FichaTecnica ficha, boolean nuevaVersion) {
    }

    /** Fila de la simulacion de medidas. */
    public record FilaSimulacion(Material material, Componente componente, double base, double simulado) {
        public double diferencia() {
            return simulado - base;
        }
    }

    public record Simulacion(Explosion.Resultado base, Explosion.Resultado simulado, List<FilaSimulacion> filas) {
    }

    /** Segmento de la dona de composicion del costo. */
    public record Composicion(String nombre, double valor, String color) {
    }

    private final FichaTecnicaRepository repository;
    private final MaterialRepository materialRepository;
    private final OrdenProduccionRepository opRepository;
    private final CatalogoService catalogo;
    private final UsuarioActual usuario;

    public List<FichaTecnica> todas() {
        return repository.findAllByOrderByCodigoAscVersionDesc();
    }

    public FichaTecnica ficha(Long id) {
        return repository.findById(id).orElseThrow(() -> new NegocioException("La ficha ya no existe."));
    }

    public Optional<FichaTecnica> porUid(String uid) {
        return uid == null ? Optional.empty() : repository.findByUid(uid);
    }

    public FichaTecnica aprobada(String codigo) {
        return catalogo.fichaAprobada(codigo);
    }

    /** Ficha aprobada del modelo (o null si no tiene). */
    public FichaTecnica aprobadaDe(Producto p) {
        if (p == null) return null;
        return todas().stream().filter(f -> f.getProducto() != null && f.getProducto().getId().equals(p.getId())
                && f.getEstado() == EstadoFicha.APROBADA).findFirst().orElse(null);
    }

    /** Ultima version (cualquier estado) del modelo. */
    public FichaTecnica ultimaDe(Producto p) {
        if (p == null) return null;
        return todas().stream().filter(f -> f.getProducto() != null && f.getProducto().getId().equals(p.getId()))
                .findFirst().orElse(null);
    }

    public Explosion.Resultado explotar(FichaTecnica f, int cantidad, Opciones op) {
        return Explosion.explotar(f, cantidad, op, catalogo);
    }

    public Explosion.Resultado explotar(FichaTecnica f) {
        return explotar(f, 1, Opciones.NINGUNA);
    }

    public double costoEstandar(FichaTecnica f) {
        return explotar(f).costoEstandar();
    }

    /** Hijas de una ficha compuesta (version aprobada de cada codigo). */
    public List<FichaTecnica> hijas(FichaTecnica f) {
        return f.getHijos().stream().map(catalogo::fichaAprobada).filter(Objects::nonNull).toList();
    }

    public List<Composicion> composicion(Explosion.Resultado r) {
        double madera = 0, espuma = 0, tela = 0, insumos = 0;
        for (Explosion.Linea l : r.lineas()) {
            Material m = l.material();
            String cat = m.getCategoria();
            if (cat.equals("Madera") || cat.equals("Tableros")) madera += l.costo();
            else if (cat.equals("Espuma")) espuma += l.costo();
            else if (m.isTela() || m.getNombre().toLowerCase(Locale.ROOT).contains("guata")) tela += l.costo();
            else insumos += l.costo();
        }
        return List.of(
                new Composicion("Madera y tableros", madera, "#A47148"),
                new Composicion("Espuma", espuma, "#0EA5E9"),
                new Composicion("Tela y guata", tela, "#EC4899"),
                new Composicion("Insumos", insumos, "#F59E0B"),
                new Composicion("Mano de obra", r.costoMO(), "#2563EB"));
    }

    /** OP activas que usan esta version. */
    public List<OrdenProduccion> usos(FichaTecnica f) {
        return opRepository.findAllByOrderByCodigoAsc().stream()
                .filter(o -> f.getUid().equals(o.getFichaUid()) && o.isActiva()).toList();
    }

    public Simulacion simular(FichaTecnica f, Medidas medidas, int piezas) {
        Explosion.Resultado base = explotar(f, piezas, Opciones.NINGUNA);
        Explosion.Resultado sim = explotar(f, piezas, Opciones.medidas(medidas));
        List<FilaSimulacion> filas = new ArrayList<>();
        Map<String, Double> simPorMat = new HashMap<>();
        for (Explosion.Linea l : sim.lineas()) simPorMat.merge(l.material().getCodigo() + "|" + l.componente(), l.bruta(), Double::sum);
        Map<String, FilaSimulacion> acc = new LinkedHashMap<>();
        for (Explosion.Linea l : base.lineas()) {
            String k = l.material().getCodigo() + "|" + l.componente();
            FilaSimulacion prev = acc.get(k);
            double b = (prev == null ? 0 : prev.base()) + l.bruta();
            acc.put(k, new FilaSimulacion(l.material(), l.componente(), b, simPorMat.getOrDefault(k, 0d)));
        }
        filas.addAll(acc.values());
        return new Simulacion(base, sim, filas);
    }

    // ============================================================ edicion

    /**
     * Aplica un cambio. Si la ficha no es Borrador, el cambio se aplica sobre la siguiente version
     * (creada en Borrador) y la original no se toca.
     */
    @Transactional
    public Edicion editar(Long fichaId, Consumer<FichaTecnica> cambio) {
        FichaTecnica f = ficha(fichaId);
        boolean nueva = false;
        if (!f.isEditable()) {
            f = copiar(f);
            nueva = true;
        }
        cambio.accept(f);
        return new Edicion(repository.save(f), nueva);
    }

    @Transactional
    public Edicion editarLinea(Long fichaId, int indice, Consumer<LineaBOM> cambio) {
        return editar(fichaId, f -> cambio.accept(f.getLineas().get(indice)));
    }

    @Transactional
    public Edicion agregarLinea(Long fichaId, Long materialId, Componente componente, double cantidad, double merma, Dependencia dep) {
        if (materialId == null) throw new NegocioException("Elige un material.");
        if (cantidad <= 0) throw new NegocioException("La cantidad debe ser mayor a cero.");
        Material m = materialRepository.findById(materialId).orElseThrow();
        return editar(fichaId, f -> f.agregarLinea(LineaBOM.builder().material(m).componente(componente)
                .cantidad(cantidad).mermaPct(merma).dependencia(dep).opcional(false).build()));
    }

    @Transactional
    public Edicion quitarLinea(Long fichaId, int indice) {
        return editar(fichaId, f -> {
            f.getLineas().remove(indice);
            for (int i = 0; i < f.getLineas().size(); i++) f.getLineas().get(i).setOrden(i);
        });
    }

    @Transactional
    public Edicion editarHoras(Long fichaId, int indice, double horas) {
        if (horas < 0) throw new NegocioException("Las horas no pueden ser negativas.");
        return editar(fichaId, f -> f.getOperaciones().get(indice).setHoras(horas));
    }

    @Transactional
    public Edicion editarIndirectos(Long fichaId, double pct) {
        if (pct < 0 || pct > 100) throw new NegocioException("Los indirectos deben estar entre 0 y 100 %.");
        return editar(fichaId, f -> f.setIndirectosPct(pct));
    }

    /** Duplica como nueva version en Borrador. */
    @Transactional
    public FichaTecnica duplicar(Long fichaId) {
        return repository.save(copiar(ficha(fichaId)));
    }

    /** Aprueba la version; la aprobada anterior del mismo codigo pasa a Obsoleta. */
    @Transactional
    public FichaTecnica aprobar(Long fichaId) {
        FichaTecnica f = ficha(fichaId);
        if (f.getEstado() == EstadoFicha.APROBADA) throw new NegocioException("La ficha ya está aprobada.");
        if (!Boolean.TRUE.equals(f.getCompuesta()) && f.getLineas().isEmpty()) {
            throw new NegocioException("La ficha no tiene materiales.");
        }
        for (FichaTecnica otra : repository.findByCodigoOrderByVersionDesc(f.getCodigo())) {
            if (otra.getEstado() == EstadoFicha.APROBADA && !otra.getId().equals(f.getId())) {
                otra.setEstado(EstadoFicha.OBSOLETA);
                repository.save(otra);
            }
        }
        f.setEstado(EstadoFicha.APROBADA);
        f.setAprobadoPor(usuario.nombre());
        f.setFechaAprobacion(AppClock.hoy());
        return repository.save(f);
    }

    @Transactional
    public FichaTecnica marcarObsoleta(Long fichaId) {
        FichaTecnica f = ficha(fichaId);
        if (f.getEstado() == EstadoFicha.OBSOLETA) throw new NegocioException("La ficha ya está obsoleta.");
        if (!usos(f).isEmpty() && f.getEstado() == EstadoFicha.APROBADA) {
            // Se permite: las OP conservan su version.
        }
        f.setEstado(EstadoFicha.OBSOLETA);
        return repository.save(f);
    }

    /** Nueva version en Borrador con las cantidades ajustadas por factor (real / estimado) por material. */
    @Transactional
    public FichaTecnica ajustarConFactores(FichaTecnica base, Map<String, Double> factores) {
        FichaTecnica nueva = copiar(base);
        for (LineaBOM l : nueva.getLineas()) {
            Double fac = factores.get(l.getMaterial().getCodigo());
            if (fac != null && fac > 0) l.setCantidad(Math.round(l.getCantidad() * fac * 1000) / 1000.0);
        }
        nueva.setNotas("Ajustada con el promedio real de consumo (" + AppClock.fmt(AppClock.hoy()) + ").");
        return repository.save(nueva);
    }

    /** Crea una ficha en Borrador para un modelo sin ficha, a partir de la ficha aprobada mas parecida. */
    @Transactional
    public FichaTecnica crearPara(Producto p, FichaTecnica plantilla) {
        if (ultimaDe(p) != null) throw new NegocioException("El modelo ya tiene ficha técnica.");
        String codigo = Codigos.siguiente(repository.findAll(), FichaTecnica::getCodigo, "FT-", 3);
        FichaTecnica f = FichaTecnica.builder().codigo(codigo).version(1).uid(FichaTecnica.uid(codigo, 1))
                .estado(EstadoFicha.BORRADOR).producto(p).nombre(p.getNombre())
                .largo(p.getLargo()).fondo(p.getFondo()).alto(p.getAlto()).cuerpos(p.getCuerpos())
                .tipoCorte(plantilla == null ? "Recto" : plantilla.getTipoCorte())
                .indirectosPct(plantilla == null ? 6d : plantilla.getIndirectosPct()).build();
        if (plantilla != null) {
            plantilla.getLineas().forEach(l -> f.agregarLinea(l.copia()));
            plantilla.getOperaciones().forEach(o -> f.agregarOperacion(o.copia()));
        } else {
            for (EtapaMO e : EtapaMO.values()) f.agregarOperacion(OperacionMO.builder().etapa(e).horas(1d).costoHora(e.costoHora()).build());
        }
        return repository.save(f);
    }

    private FichaTecnica copiar(FichaTecnica f) {
        int version = repository.findByCodigoOrderByVersionDesc(f.getCodigo()).stream()
                .mapToInt(FichaTecnica::getVersion).max().orElse(f.getVersion()) + 1;
        FichaTecnica n = FichaTecnica.builder()
                .codigo(f.getCodigo()).version(version).uid(FichaTecnica.uid(f.getCodigo(), version))
                .estado(EstadoFicha.BORRADOR).producto(f.getProducto()).nombre(f.getNombre())
                .largo(f.getLargo()).fondo(f.getFondo()).alto(f.getAlto()).cuerpos(f.getCuerpos())
                .chaise(f.getChaise()).tipoCorte(f.getTipoCorte()).indirectosPct(f.getIndirectosPct())
                .notas(f.getNotas()).compuesta(f.getCompuesta()).hijos(new ArrayList<>(f.getHijos())).build();
        f.getLineas().forEach(l -> n.agregarLinea(l.copia()));
        f.getOperaciones().forEach(o -> n.agregarOperacion(o.copia()));
        return n;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
