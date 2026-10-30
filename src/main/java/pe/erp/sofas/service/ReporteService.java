/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ReporteService
 * Descripción: Reportes con rango de fechas, listos para vista previa y exportacion.
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
import pe.erp.sofas.core.Money;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Reportes con rango de fechas, listos para vista previa y exportacion. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    public enum Tipo {
        VENTAS_MODELO("Ventas por modelo", "Unidades e importe vendido por modelo de sofá.", "INSERT_CHART"),
        TELAS_VENDIDAS("Telas y colores más vendidos", "Colores con más pedidos y ventas.", "PALETTE"),
        METROS_TELA("Metros de tela consumidos", "Metros estimados y reales por tipo de tela.", "STRAIGHTEN"),
        PRODUCCION_TAPICERO("Producción por tapicero", "Órdenes y sofás por responsable.", "ENGINEERING"),
        COSTO_ESTANDAR_REAL("Costo estándar frente al real", "Desvío valorizado por orden de producción.", "COMPARE_ARROWS"),
        MERMAS("Mermas por material", "Sobremerma y su costo por material.", "CONTENT_CUT"),
        PEDIDOS_PENDIENTES("Pedidos pendientes", "Pedidos abiertos con su saldo y entrega.", "PENDING_ACTIONS"),
        GARANTIAS("Garantías y reclamos", "Reclamos, cobertura y costo.", "VERIFIED_USER"),
        KARDEX("Kardex", "Movimientos de almacén del periodo.", "RECEIPT_LONG");

        private final String nombre;
        private final String descripcion;
        private final String icono;

        Tipo(String nombre, String descripcion, String icono) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.icono = icono;
        }

        public String nombre() {
            return nombre;
        }

        public String descripcion() {
            return descripcion;
        }

        public String icono() {
            return icono;
        }
    }

    private final VentaRepository ventaRepository;
    private final PedidoRepository pedidoRepository;
    private final ConsumoRealRepository consumoRepository;
    private final OrdenProduccionRepository opRepository;
    private final MovimientoRepository movimientoRepository;
    private final TelaRepository telaRepository;
    private final GarantiaService garantiaService;

    public Tabla generar(Tipo tipo, LocalDate desde, LocalDate hasta) {
        String rango = "Del " + AppClock.fmt(desde) + " al " + AppClock.fmt(hasta);
        return switch (tipo) {
            case VENTAS_MODELO -> ventasModelo(rango, desde, hasta);
            case TELAS_VENDIDAS -> telasVendidas(rango, desde, hasta);
            case METROS_TELA -> metrosTela(rango, desde, hasta);
            case PRODUCCION_TAPICERO -> produccionTapicero(rango, desde, hasta);
            case COSTO_ESTANDAR_REAL -> costoReal(rango, desde, hasta);
            case MERMAS -> mermas(rango, desde, hasta);
            case PEDIDOS_PENDIENTES -> pedidosPendientes(rango, desde, hasta);
            case GARANTIAS -> garantias(rango, desde, hasta);
            case KARDEX -> kardex(rango, desde, hasta);
        };
    }

    private static boolean en(LocalDate f, LocalDate d, LocalDate h) {
        return f != null && !f.isBefore(d) && !f.isAfter(h);
    }

    private Tabla ventasModelo(String rango, LocalDate d, LocalDate h) {
        Map<String, double[]> acc = new LinkedHashMap<>();
        double total = 0;
        for (Venta v : ventaRepository.findAll()) {
            if (!en(v.getFecha().toLocalDate(), d, h)) continue;
            for (ItemVenta i : v.getItems()) {
                double[] a = acc.computeIfAbsent(i.getProducto().getCodigo() + "|" + i.getProducto().getNombre(), k -> new double[2]);
                a[0] += i.getCantidad();
                a[1] += i.getSubtotal().doubleValue();
                total += i.getSubtotal().doubleValue();
            }
        }
        Tabla t = Tabla.de(Tipo.VENTAS_MODELO.nombre(), rango, List.of("Código", "Modelo", "Unidades", "Importe", "% del total"),
                new boolean[]{false, false, true, true, true});
        double tot = total;
        int unidades = 0;
        for (var e : acc.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1])).toList()) {
            String[] k = e.getKey().split("\\|");
            unidades += (int) e.getValue()[0];
            t.fila(k[0], k[1], (int) e.getValue()[0], Money.fmt(e.getValue()[1]), String.format("%.1f %%", tot == 0 ? 0 : e.getValue()[1] * 100 / tot));
        }
        return t.conTotales("", "Total", String.valueOf(unidades), Money.fmt(total), "100 %");
    }

    private Tabla telasVendidas(String rango, LocalDate d, LocalDate h) {
        Map<String, int[]> acc = new LinkedHashMap<>();
        Map<String, Tela> telas = new HashMap<>();
        telaRepository.findAll().forEach(t -> telas.put(t.getCodigo(), t));
        for (Venta v : ventaRepository.findAll()) {
            if (!en(v.getFecha().toLocalDate(), d, h)) continue;
            for (ItemVenta i : v.getItems()) if (i.getTelaCodigo() != null) acc.computeIfAbsent(i.getTelaCodigo(), k -> new int[2])[0] += i.getCantidad();
        }
        for (Pedido p : pedidoRepository.findAll()) {
            if (p.getTela() == null || p.getEstado() == EstadoPedido.CANCELADO || p.getFecha() == null || p.getFecha().isAfter(h)) continue;
            acc.computeIfAbsent(p.getTela().getCodigo(), k -> new int[2])[1] += p.getCantidad();
        }
        Tabla t = Tabla.de(Tipo.TELAS_VENDIDAS.nombre(), rango, List.of("Código", "Tela", "Tipo", "Ventas tienda", "Pedidos", "Total"),
                new boolean[]{false, false, false, true, true, true});
        acc.entrySet().stream().sorted((a, b) -> (b.getValue()[0] + b.getValue()[1]) - (a.getValue()[0] + a.getValue()[1]))
                .forEach(e -> {
                    Tela tela = telas.get(e.getKey());
                    t.fila(e.getKey(), tela == null ? "—" : tela.getColor(), tela == null ? "—" : tela.getTipo().nombre(),
                            e.getValue()[0], e.getValue()[1], e.getValue()[0] + e.getValue()[1]);
                });
        return t;
    }

    private List<ConsumoReal> consumos(LocalDate d, LocalDate h) {
        return consumoRepository.findAll().stream().filter(c -> en(c.getFecha(), d, h)).toList();
    }

    private Tabla metrosTela(String rango, LocalDate d, LocalDate h) {
        Tabla t = Tabla.de(Tipo.METROS_TELA.nombre(), rango, List.of("Material", "Órdenes", "Estimado (m)", "Real (m)", "Diferencia", "Costo real"),
                new boolean[]{false, true, true, true, true, true});
        Map<Material, List<ConsumoReal>> g = consumos(d, h).stream().filter(c -> c.getMaterial().isTela())
                .collect(Collectors.groupingBy(ConsumoReal::getMaterial, LinkedHashMap::new, Collectors.toList()));
        double te = 0, tr = 0;
        for (var e : g.entrySet()) {
            double est = e.getValue().stream().mapToDouble(ConsumoReal::getEstimado).sum();
            double real = e.getValue().stream().mapToDouble(ConsumoReal::getRealConsumido).sum();
            te += est;
            tr += real;
            t.fila(e.getKey().getNombre(), e.getValue().stream().map(ConsumoReal::getOpCodigo).distinct().count(),
                    Money.numero(est), Money.numero(real), String.format("%+.1f %%", est == 0 ? 0 : (real - est) * 100 / est),
                    Money.fmt(real * e.getKey().getCosto()));
        }
        return t.conTotales("Total", "", Money.numero(te), Money.numero(tr), String.format("%+.1f %%", te == 0 ? 0 : (tr - te) * 100 / te), "");
    }

    private Tabla produccionTapicero(String rango, LocalDate d, LocalDate h) {
        Tabla t = Tabla.de(Tipo.PRODUCCION_TAPICERO.nombre(), rango, List.of("Responsable", "Cargo", "OP terminadas", "OP en proceso", "Sofás"),
                new boolean[]{false, false, true, true, true});
        Map<String, int[]> acc = new LinkedHashMap<>();
        Map<String, String> cargos = new HashMap<>();
        for (OrdenProduccion o : opRepository.findAll()) {
            if (o.getAnulada() || o.getResponsable() == null) continue;
            String n = o.getResponsable().getNombre();
            cargos.put(n, o.getResponsable().getCargo());
            int[] a = acc.computeIfAbsent(n, k -> new int[3]);
            if (o.getEtapa() == Etapa.LISTO && en(o.getFechaFin(), d, h)) {
                a[0]++;
                a[2] += o.getCantidad();
            } else if (o.isActiva()) {
                a[1]++;
                a[2] += o.getCantidad();
            }
        }
        for (ConsumoReal c : consumos(d, h)) {
            acc.computeIfAbsent(c.getResponsable(), k -> new int[3]);
            cargos.putIfAbsent(c.getResponsable(), "Taller");
        }
        Map<String, Set<String>> hist = consumos(d, h).stream().collect(Collectors.groupingBy(ConsumoReal::getResponsable,
                Collectors.mapping(ConsumoReal::getOpCodigo, Collectors.toSet())));
        hist.forEach((n, ops) -> {
            int[] a = acc.get(n);
            long nuevas = ops.stream().filter(code -> opRepository.findByCodigo(code).isEmpty()).count();
            a[0] += nuevas;
            a[2] += nuevas;
        });
        acc.forEach((n, a) -> t.fila(n, cargos.get(n), a[0], a[1], a[2]));
        return t;
    }

    private Tabla costoReal(String rango, LocalDate d, LocalDate h) {
        Tabla t = Tabla.de(Tipo.COSTO_ESTANDAR_REAL.nombre(), rango, List.of("OP", "Producto", "Responsable", "Estándar materiales", "Real", "Desvío", "Desvío %"),
                new boolean[]{false, false, false, true, true, true, true});
        Map<String, List<ConsumoReal>> g = consumos(d, h).stream().collect(Collectors.groupingBy(ConsumoReal::getOpCodigo, TreeMap::new, Collectors.toList()));
        double te = 0, tr = 0;
        for (var e : g.entrySet()) {
            double est = e.getValue().stream().mapToDouble(c -> c.getEstimado() * c.getMaterial().getCosto()).sum();
            double real = e.getValue().stream().mapToDouble(c -> c.getRealConsumido() * c.getMaterial().getCosto()).sum();
            te += est;
            tr += real;
            ConsumoReal c0 = e.getValue().get(0);
            t.fila(e.getKey(), c0.getProducto(), c0.getResponsable(), Money.fmt(est), Money.fmt(real), Money.fmt(real - est),
                    String.format("%+.1f %%", est == 0 ? 0 : (real - est) * 100 / est));
        }
        return t.conTotales("Total", "", "", Money.fmt(te), Money.fmt(tr), Money.fmt(tr - te), String.format("%+.1f %%", te == 0 ? 0 : (tr - te) * 100 / te));
    }

    private Tabla mermas(String rango, LocalDate d, LocalDate h) {
        Tabla t = Tabla.de(Tipo.MERMAS.nombre(), rango, List.of("Código", "Material", "Estimado", "Real", "Sobremerma", "Costo sobremerma"),
                new boolean[]{false, false, true, true, true, true});
        Map<Material, List<ConsumoReal>> g = consumos(d, h).stream()
                .collect(Collectors.groupingBy(ConsumoReal::getMaterial, LinkedHashMap::new, Collectors.toList()));
        double total = 0;
        List<Object[]> filas = new ArrayList<>();
        for (var e : g.entrySet()) {
            double est = e.getValue().stream().mapToDouble(ConsumoReal::getEstimado).sum();
            double real = e.getValue().stream().mapToDouble(ConsumoReal::getRealConsumido).sum();
            double sobre = Math.max(0, real - est);
            total += sobre * e.getKey().getCosto();
            filas.add(new Object[]{e.getKey().getCodigo(), e.getKey().getNombre(), Money.numero(est) + " " + e.getKey().getUnidad(),
                    Money.numero(real) + " " + e.getKey().getUnidad(), Money.numero(sobre), sobre * e.getKey().getCosto()});
        }
        filas.sort((a, b) -> Double.compare((double) b[5], (double) a[5]));
        for (Object[] f : filas) t.fila(f[0], f[1], f[2], f[3], f[4], Money.fmt((double) f[5]));
        return t.conTotales("", "Total", "", "", "", Money.fmt(total));
    }

    private Tabla pedidosPendientes(String rango, LocalDate d, LocalDate h) {
        Tabla t = Tabla.de(Tipo.PEDIDOS_PENDIENTES.nombre(), rango, List.of("Pedido", "Cliente", "Modelo", "Tipo", "Entrega", "Estado", "Precio", "Saldo"),
                new boolean[]{false, false, false, false, false, false, true, true});
        double saldo = 0;
        for (Pedido p : pedidoRepository.findAllByOrderByCodigoDesc()) {
            if (p.getEstado().cerrado()) continue;
            saldo += p.getSaldo().doubleValue();
            t.fila(p.getCodigo(), p.getCliente().getNombre(), p.getProductoNombre(), p.getTipo().nombre(), AppClock.fmt(p.getFechaEntrega()),
                    p.getEstado().nombre(), Money.fmt(p.getPrecio()), Money.fmt(p.getSaldo()));
        }
        return t.conTotales("", "", "", "", "", "", "Total", Money.fmt(saldo));
    }

    private Tabla garantias(String rango, LocalDate d, LocalDate h) {
        Tabla t = Tabla.de(Tipo.GARANTIAS.nombre(), rango, List.of("Reclamo", "Cliente", "Sofá", "Falla", "Cobertura", "SLA (días)", "Estado", "Costo"),
                new boolean[]{false, false, false, false, false, true, false, true});
        double costo = 0;
        for (ReclamoGarantia r : garantiaService.listar()) {
            costo += r.getCosto() == null ? 0 : r.getCosto().doubleValue();
            t.fila(r.getCodigo(), r.getCliente().getNombre(), r.getProductoNombre(), r.getFalla(), garantiaService.cobertura(r).texto(),
                    garantiaService.sla(r), r.getEstado().nombre(), Money.fmt(r.getCosto()));
        }
        return t.conTotales("", "", "", "", "", "", "Total", Money.fmt(costo));
    }

    private Tabla kardex(String rango, LocalDate d, LocalDate h) {
        Tabla t = Tabla.de(Tipo.KARDEX.nombre(), rango, List.of("Fecha", "Material", "Tipo", "Cantidad", "Referencia", "Concepto", "Saldo", "Usuario"),
                new boolean[]{false, false, false, true, false, false, true, false});
        movimientoRepository.findAllByOrderByFechaDescIdDesc().stream().filter(m -> en(m.getFecha().toLocalDate(), d, h))
                .forEach(m -> t.fila(m.getFecha().toLocalDate().format(AppClock.FECHA) + " " + m.getFecha().toLocalTime().toString().substring(0, 5),
                        m.getMaterial().getCodigo() + " " + m.getMaterial().getNombre(), m.getTipo().nombre(),
                        Money.numero(m.getCantidad()) + " " + m.getMaterial().getUnidad(), m.getReferencia(), m.getConcepto(),
                        Money.numero(m.getSaldo() == null ? 0 : m.getSaldo()), m.getUsuario()));
        return t;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
