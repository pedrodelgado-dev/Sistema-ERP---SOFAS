/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: DashboardService
 * Descripción: Indicadores y series del Dashboard (todo calculado de la base compartida).
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
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.ResumenMensualRepository;

import java.time.LocalDate;
import java.util.*;

/** Indicadores y series del Dashboard (todo calculado de la base compartida). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    public record Kpis(double ventasMes, double variacionPct, int sofasVendidos, long sofasFabricacion, long ordenesFabricacion,
                       long pedidosPendientes, double metrosTela, long colores) {
    }

    public record Entrega(LocalDate fecha, String titulo, String cliente, String codigo, String modulo, String estado) {
    }

    public record Segmento(String nombre, double valor, String color) {
    }

    private final VentaService ventaService;
    private final ProduccionService produccionService;
    private final PedidoService pedidoService;
    private final TelaService telaService;
    private final FinanzasService finanzasService;
    private final ResumenMensualRepository resumenRepository;

    public Kpis kpis() {
        LocalDate anterior = AppClock.hoy().minusMonths(1);
        double previo = resumenRepository.findAll().stream()
                .filter(r -> r.getAnio() == anterior.getYear() && r.getMes() == anterior.getMonthValue())
                .mapToDouble(r -> r.getVentas().doubleValue()).findFirst().orElse(0);
        double ventas = ventaService.totalMes().doubleValue();
        var k = produccionService.kpis();
        return new Kpis(ventas, previo == 0 ? 0 : (ventas - previo) * 100 / previo, ventaService.sofasMes(),
                k.sofasEnProceso(), k.ordenesEnProceso(), pedidoService.pendientesAMedida(), telaService.metrosEnStock(),
                telaService.activas().size());
    }

    public List<FinanzasService.Mes> ventas12() {
        return finanzasService.ultimos12();
    }

    /** Ventas del mes por grupo de modelo. */
    public List<Segmento> ventasPorModelo() {
        Map<String, Double> m = new LinkedHashMap<>();
        for (String g : List.of("Sofá moderno", "Seccional", "Juego de sala", "Sofá cama", "Chesterfield", "Otros")) m.put(g, 0d);
        for (Venta v : ventaService.delMes(AppClock.hoy())) {
            double factor = v.getItems().stream().mapToDouble(i -> i.getSubtotal().doubleValue()).sum();
            factor = factor == 0 ? 0 : v.getTotal().doubleValue() / factor;
            for (ItemVenta i : v.getItems()) m.merge(grupo(i.getProducto().getForma()), i.getSubtotal().doubleValue() * factor, Double::sum);
        }
        String[] colores = {"#2563EB", "#A47148", "#14B8A6", "#8B5CF6", "#C69A6D", "#94A3B8"};
        List<Segmento> l = new ArrayList<>();
        int i = 0;
        for (var e : m.entrySet()) l.add(new Segmento(e.getKey(), e.getValue(), colores[i++]));
        return l;
    }

    public static String grupo(Forma f) {
        return switch (f) {
            case MODERNO -> "Sofá moderno";
            case SECCIONAL_L, SECCIONAL_U -> "Seccional";
            case JUEGO -> "Juego de sala";
            case SOFA_CAMA -> "Sofá cama";
            case CHESTERFIELD -> "Chesterfield";
            default -> "Otros";
        };
    }

    public List<Entrega> proximasEntregas(int max) {
        List<Entrega> l = new ArrayList<>();
        for (Pedido p : pedidoService.listar()) {
            if (p.getEstado().cerrado() || p.getFechaEntrega() == null || p.getFechaEntrega().isBefore(AppClock.hoy().minusDays(3))) continue;
            l.add(new Entrega(p.getFechaEntrega(), p.getCantidad() + " × " + p.getProductoNombre(), p.getCliente().getNombre(),
                    p.getCodigo(), "pedidos", p.isRetrasado() ? "Retrasado" : p.getEstado().nombre()));
        }
        l.sort(Comparator.comparing(Entrega::fecha));
        return l.subList(0, Math.min(max, l.size()));
    }

    public List<Pedido> ultimosPedidos(int max) {
        List<Pedido> l = pedidoService.listar();
        return l.subList(0, Math.min(max, l.size()));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
