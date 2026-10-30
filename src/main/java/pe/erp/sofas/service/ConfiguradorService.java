/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConfiguradorService
 * Descripción: Precio en tiempo real del configurador (seccion 6.7): toma la ficha base segun los cuerpos
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
import pe.erp.sofas.model.ConfigSofa.*;
import pe.erp.sofas.service.calculo.Explosion;
import pe.erp.sofas.service.calculo.Formula;
import pe.erp.sofas.service.calculo.Medidas;
import pe.erp.sofas.service.calculo.Opciones;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Precio en tiempo real del configurador (seccion 6.7): toma la ficha base segun los cuerpos
 * (1 → FT-003, 2 → FT-002, 3 o mas → FT-001) con L = 95 / 160 / 210 / 260, suma los extras y aplica
 * precio = costo × 1.55 redondeado a la decena.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguradorService {

    public static final double FACTOR_PRECIO = 1.55;

    public static final String TELA_GENERICA = "MP-003";
    public static final String D23 = "MP-019";
    public static final String PATA_MADERA = "MP-021";
    public static final String CINCHA = "MP-009";
    public static final String MADERA = "MP-001";
    public static final String GUATA = "MP-011";
    public static final String FIBRA = "MP-013";
    public static final String BOTON = "MP-016";
    public static final String RECLINABLE = "MP-030";
    public static final String CAMA = "MP-031";
    public static final String COLCHONETA = "MP-032";
    public static final String ELECTRICO = "MP-036";

    /** Linea del desglose con su estado de stock. */
    public record Linea(Material material, String componente, double cantidad, double costo, String estado) {
    }

    public record Cotizacion(ConfigSofa config, FichaTecnica ficha, Explosion.Resultado explosion, List<Linea> lineas,
                             double costoMateriales, double horasMO, double costoMO, double indirectos, double costo,
                             double precio, int dias, boolean bajoPedido, boolean critico, Tela tela) {

        public double margenPct() {
            return precio == 0 ? 0 : (precio - costo) * 100 / precio;
        }

        /** "ficha FT-001 v3". */
        public String referencia() {
            return ficha == null ? "sin ficha aprobada" : "ficha " + ficha.getEtiqueta();
        }
    }

    private final FichaService fichaService;
    private final CatalogoService catalogo;
    private final TelaService telaService;
    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private final ProduccionService produccionService;

    /** Valores por defecto de cada modelo base (p. ej. el Chesterfield pasa a cuero cognac). */
    public ConfigSofa defecto(Modelo m) {
        return switch (m) {
            case MODERNO -> new ConfigSofa(m, 3, Chaise.NINGUNA, 92, tela("TL-01"), Brazo.RECTO, Patas.MADERA,
                    AcabadoMadera.NATURAL, Espuma.D23, 2, tela("TL-07"), Mecanismo.FIJO);
            case SECCIONAL_L -> new ConfigSofa(m, 3, Chaise.DERECHA, 92, tela("TL-09"), Brazo.RECTO, Patas.METAL_NEGRO,
                    AcabadoMadera.NATURAL, Espuma.D23, 4, tela("TL-10"), Mecanismo.FIJO);
            case SILLON -> new ConfigSofa(m, 1, Chaise.NINGUNA, 92, tela("TL-15"), Brazo.REDONDEADO, Patas.MADERA,
                    AcabadoMadera.NOGAL, Espuma.D23, 0, tela("TL-15"), Mecanismo.FIJO);
            case CHESTERFIELD -> new ConfigSofa(m, 3, Chaise.NINGUNA, 92, tela("TL-13"), Brazo.REDONDEADO, Patas.MADERA,
                    AcabadoMadera.WENGUE, Espuma.D23, 0, tela("TL-13"), Mecanismo.FIJO);
            case SOFA_CAMA -> new ConfigSofa(m, 3, Chaise.NINGUNA, 92, tela("TL-21"), Brazo.RECTO, Patas.MADERA,
                    AcabadoMadera.NOGAL, Espuma.D23, 2, tela("TL-20"), Mecanismo.CAMA);
            case ESCANDINAVO -> new ConfigSofa(m, 2, Chaise.NINGUNA, 92, tela("TL-18"), Brazo.DELGADO, Patas.MADERA,
                    AcabadoMadera.NATURAL, Espuma.D23, 2, tela("TL-19"), Mecanismo.FIJO);
        };
    }

    private Long tela(String codigo) {
        return telaService.porCodigo(codigo).map(Tela::getId).orElse(null);
    }

    private final Map<Modelo, BigDecimal> desdeCache = new java.util.concurrent.ConcurrentHashMap<>();

    /** Precio "desde": el de la configuracion por defecto del modelo con 1 cuerpo menos de lo habitual. */
    public BigDecimal desde(Modelo m) {
        return desdeCache.computeIfAbsent(m, x -> {
            try {
                ConfigSofa c = defecto(x);
                ConfigSofa minimo = c.conCojines(0, null).conCuerpos(x == Modelo.SECCIONAL_L ? 2 : x == Modelo.SILLON ? 1 : Math.max(1, c.cuerpos() - 1));
                return BigDecimal.valueOf(cotizar(minimo).precio());
            } catch (RuntimeException e) {
                return productoService.porCodigo(x.producto()).map(Producto::getPrecio).orElse(BigDecimal.ZERO);
            }
        });
    }

    public static String fichaBase(int cuerpos) {
        return cuerpos <= 1 ? "FT-003" : cuerpos == 2 ? "FT-002" : "FT-001";
    }

    public Cotizacion cotizar(ConfigSofa c) {
        FichaTecnica ficha = fichaService.aprobada(fichaBase(c.cuerpos()));
        if (ficha == null) throw new NegocioException("No hay ficha aprobada " + fichaBase(c.cuerpos()) + " para cotizar.");
        Tela tela = c.telaId() == null ? null : telaService.tela(c.telaId());
        Tela telaCojines = c.telaCojinesId() == null ? tela : telaService.tela(c.telaCojinesId());
        String telaMat = tela == null ? TELA_GENERICA : tela.getTipo().materialCodigo();
        String espumaMat = c.espuma().material() == null ? D23 : c.espuma().material();

        Map<String, String> sust = new HashMap<>();
        sust.put(TELA_GENERICA, telaMat);
        if (c.espuma().material() != null) sust.put(D23, c.espuma().material());
        if (c.patas() == Patas.ZOCALO) sust.put(PATA_MADERA, Explosion.QUITAR);
        else if (c.patas().material() != null) sust.put(PATA_MADERA, c.patas().material());
        if (c.mecanismo() == Mecanismo.CAMA) sust.put(CINCHA, Explosion.QUITAR);

        List<LineaExtra> extras = new ArrayList<>();
        double recargoMO = 0, moFija = 0;
        if (c.conChaise()) {
            extras.add(new LineaExtra(MADERA, Componente.ESTRUCTURA.name(), 10d));
            extras.add(new LineaExtra(espumaMat, Componente.ASIENTO.name(), 0.6));
            extras.add(new LineaExtra(telaMat, Componente.TAPIZADO.name(), 3.5));
            if (c.mecanismo() != Mecanismo.CAMA) extras.add(new LineaExtra(CINCHA, Componente.ASIENTO.name(), 8d));
            extras.add(new LineaExtra(GUATA, Componente.TAPIZADO.name(), 2d));
            recargoMO += 25;
        }
        if (c.cojines() > 0) {
            String telaCoj = telaCojines == null ? telaMat : telaCojines.getTipo().materialCodigo();
            extras.add(new LineaExtra(telaCoj, Componente.COJINES.name(), 0.6 * c.cojines()));
            extras.add(new LineaExtra(FIBRA, Componente.COJINES.name(), 0.3 * c.cojines()));
        }
        switch (c.mecanismo()) {
            case RECLINABLE_MANUAL -> extras.add(new LineaExtra(RECLINABLE, Componente.MECANISMO.name(), (double) c.cuerpos()));
            case RECLINABLE_ELECTRICO -> extras.add(new LineaExtra(ELECTRICO, Componente.MECANISMO.name(), (double) c.cuerpos()));
            case CAMA -> {
                extras.add(new LineaExtra(CAMA, Componente.MECANISMO.name(), 1d));
                extras.add(new LineaExtra(COLCHONETA, Componente.MECANISMO.name(), 1d));
            }
            default -> {
            }
        }
        if (c.mecanismo() != Mecanismo.FIJO) moFija += 40;
        if (c.modelo() == Modelo.CHESTERFIELD) {
            extras.add(new LineaExtra(BOTON, Componente.TAPIZADO.name(), 48d));
            extras.add(new LineaExtra(telaMat, Componente.TAPIZADO.name(), 2.2));
            moFija += 54;
        }
        if (c.patas() == Patas.ZOCALO) extras.add(new LineaExtra("MP-035", Componente.PATAS.name(), 1d));

        Medidas medidas = new Medidas(c.largo(), c.profundidad(), c.alto(), c.cuerpos());
        Opciones op = new Opciones(medidas, sust, extras, false, recargoMO, moFija);
        Explosion.Resultado r = fichaService.explotar(ficha, 1, op);

        List<Linea> lineas = new ArrayList<>();
        boolean bajoPedido = c.patas().bajoPedido() || c.espuma().bajoPedido() || c.mecanismo().bajoPedido();
        boolean critico = false;
        Map<String, String> compPorMat = new HashMap<>();
        for (Explosion.Linea l : r.lineas()) compPorMat.merge(l.material().getCodigo(), l.componente().nombre(), (a, b) -> a.contains(b) ? a : a + ", " + b);
        for (Explosion.PorMaterial pm : r.porMaterial()) {
            Material m = pm.material();
            String estado;
            if (Boolean.TRUE.equals(m.getBajoPedido()) && m.getStock() <= 0) {
                estado = "Bajo pedido";
                bajoPedido = true;
            } else if (m.isTela() && tela != null && m.getCodigo().equals(tela.getTipo().materialCodigo())) {
                double metrosColor = tela.getStock();
                estado = metrosColor >= pm.cantidad() ? "Suficiente" : metrosColor > 0 ? "Parcial" : "Sin stock";
            } else {
                double disp = m.getDisponible();
                estado = disp >= pm.cantidad() ? "Suficiente" : disp > 0 ? "Parcial" : "Sin stock";
            }
            if (estado.equals("Parcial") || estado.equals("Sin stock")) critico = true;
            lineas.add(new Linea(m, compPorMat.getOrDefault(m.getCodigo(), ""), pm.cantidad(), pm.costo(), estado));
        }
        double costo = r.costoEstandar();
        double precio = Formula.redondearDecena(costo * FACTOR_PRECIO);
        int dias = (int) Math.ceil(r.horasMO() / 8.0 + 3) + (bajoPedido ? 4 : 0) + (critico ? 5 : 0);
        return new Cotizacion(c, ficha, r, lineas, r.costoMateriales(), r.horasMO(), r.costoMO(), r.indirectos(), costo,
                precio, dias, bajoPedido, critico, tela);
    }

    /** Resumen legible: "Sofá Moderno · 3 cuerpos · Chenille Gris perla · brazo recto · patas madera cónica (natural)…". */
    public String especificacion(ConfigSofa c) {
        Tela t = c.telaId() == null ? null : telaService.tela(c.telaId());
        Tela tc = c.telaCojinesId() == null ? null : telaService.tela(c.telaCojinesId());
        StringBuilder sb = new StringBuilder();
        sb.append(c.modelo().nombre()).append(" · ").append(c.cuerpos()).append(c.cuerpos() == 1 ? " cuerpo" : " cuerpos");
        if (c.conChaise()) sb.append(" · chaise ").append(c.chaise().nombre().toLowerCase(Locale.ROOT));
        sb.append(" · ").append(c.largo()).append(" × ").append(c.conChaise() ? c.profundidad() + 65 : c.profundidad()).append(" × ").append(c.alto()).append(" cm");
        sb.append(" · ").append(t == null ? "tela según ficha" : t.getNombre());
        sb.append(" · brazo ").append(c.brazo().nombre().toLowerCase(Locale.ROOT));
        sb.append(" · patas ").append(c.patas().nombre().toLowerCase(Locale.ROOT));
        if (c.patas() == Patas.MADERA) sb.append(" (").append(c.acabado().nombre().toLowerCase(Locale.ROOT)).append(")");
        sb.append(" · ").append(c.espuma().nombre());
        if (c.cojines() > 0) sb.append(" · ").append(c.cojines()).append(" cojines").append(tc == null ? "" : " " + tc.getColor());
        if (c.mecanismo() != Mecanismo.FIJO) sb.append(" · ").append(c.mecanismo().nombre().toLowerCase(Locale.ROOT));
        return sb.toString();
    }

    /** Producto del catalogo al que se asocia un pedido configurado. */
    public Producto productoPara(ConfigSofa c) {
        String codigo = switch (c.modelo()) {
            case MODERNO -> c.cuerpos() <= 1 ? "SF-003" : c.cuerpos() == 2 ? "SF-002" : "SF-001";
            default -> c.modelo().producto();
        };
        return productoService.porCodigo(codigo).orElseThrow();
    }

    /** Datos de la OP para una configuracion (ficha base, sustituciones, extras y medidas). */
    public ProduccionService.NuevaOP nuevaOP(ConfigSofa c, Cotizacion cot, Long clienteId, Long responsableId, LocalDate entrega,
                                             String pedidoCodigo) {
        Producto p = productoPara(c);
        Opciones op = opciones(c);
        Map<String, String> sust = new HashMap<>(op.sust());
        return new ProduccionService.NuevaOP(p.getId(), c.modelo().nombre() + " " + c.cuerpos() + (c.cuerpos() == 1 ? " cuerpo" : " cuerpos") + " · a medida",
                1, clienteId, responsableId, Prioridad.MEDIA, AppClock.hoy(), entrega, c.telaId(),
                new Medidas(c.largo(), c.profundidad(), c.alto(), c.cuerpos()), sust, op.ext(), pedidoCodigo, null,
                especificacion(c), Etapa.PENDIENTE, BigDecimal.valueOf(cot.costo()).setScale(2, java.math.RoundingMode.HALF_UP),
                cot.ficha().getUid());
    }

    /** Opciones de explosion de una configuracion (las mismas que usa cotizar). */
    public Opciones opciones(ConfigSofa c) {
        Cotizacion cot = cotizar(c);
        Explosion.Resultado r = cot.explosion();
        Map<String, String> sust = new HashMap<>();
        List<LineaExtra> extras = new ArrayList<>();
        for (Explosion.Linea l : r.lineas()) {
            if (l.extra()) extras.add(new LineaExtra(l.material().getCodigo(), l.componente().name(), l.base()));
        }
        Tela tela = cot.tela();
        sust.put(TELA_GENERICA, tela == null ? TELA_GENERICA : tela.getTipo().materialCodigo());
        if (c.espuma().material() != null) sust.put(D23, c.espuma().material());
        if (c.patas() == Patas.ZOCALO) sust.put(PATA_MADERA, Explosion.QUITAR);
        else if (c.patas().material() != null) sust.put(PATA_MADERA, c.patas().material());
        if (c.mecanismo() == Mecanismo.CAMA) sust.put(CINCHA, Explosion.QUITAR);
        double recargo = c.conChaise() ? 25 : 0;
        double fija = (c.mecanismo() != Mecanismo.FIJO ? 40 : 0) + (c.modelo() == Modelo.CHESTERFIELD ? 54 : 0);
        return new Opciones(new Medidas(c.largo(), c.profundidad(), c.alto(), c.cuerpos()), sust, extras, false, recargo, fija);
    }

    /** Crea el pedido personalizado (y opcionalmente su OP con la ficha y las sustituciones). */
    @Transactional
    public Pedido crearPedido(ConfigSofa c, Long clienteId, LocalDate entrega, BigDecimal adelanto, String metodo,
                              String observaciones, boolean crearOP, Long responsableId) {
        if (clienteId == null) throw new NegocioException("Elige el cliente.");
        Cotizacion cot = cotizar(c);
        Producto p = productoPara(c);
        Pedido ped = pedidoService.crear(new PedidoService.NuevoPedido(clienteId, p.getId(), Pedido.Tipo.PERSONALIZADO, 1,
                c.largo(), c.conChaise() ? c.profundidad() + 65 : c.profundidad(), c.alto(), c.telaId(),
                entrega == null ? AppClock.hoy().plusDays(cot.dias()) : entrega,
                BigDecimal.valueOf(cot.precio()), adelanto, metodo, observaciones, c.serializar(), especificacion(c)));
        if (crearOP) {
            if (adelanto != null && adelanto.compareTo(ped.getPrecio().multiply(new BigDecimal("0.5"))) >= 0) {
                ped = pedidoService.avanzarA(ped.getId(), EstadoPedido.CONFIRMADO);
            }
            OrdenProduccion op = produccionService.crear(nuevaOP(c, cot, clienteId, responsableId, ped.getFechaEntrega(), ped.getCodigo()));
            ped = pedidoService.vincularOP(ped.getId(), op.getCodigo());
        }
        return ped;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
