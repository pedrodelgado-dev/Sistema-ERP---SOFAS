/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SemillaCoherenteTest
 * Descripción: Las cifras de la semilla coinciden con el prototipo y los modulos son coherentes entre si.
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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.MovimientoRepository;
import pe.erp.sofas.service.calculo.Explosion;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Las cifras de la semilla coinciden con el prototipo y los modulos son coherentes entre si. */
@IntegracionTest
class SemillaCoherenteTest {

    @Autowired DashboardService dashboard;
    @Autowired TelaService telas;
    @Autowired InventarioService inventario;
    @Autowired ProduccionService produccion;
    @Autowired NotificacionService notificaciones;
    @Autowired ProductoService productos;
    @Autowired MovimientoRepository movimientos;

    @Test
    void indicadoresDelDashboard() {
        DashboardService.Kpis k = dashboard.kpis();
        assertThat(k.ventasMes()).isEqualTo(86420);
        assertThat(k.variacionPct()).isCloseTo(14, within(0.1));
        assertThat(k.sofasVendidos()).isEqualTo(34);
        assertThat(k.sofasFabricacion()).isEqualTo(17);
        assertThat(k.ordenesFabricacion()).isEqualTo(10);
        assertThat(k.pedidosPendientes()).isEqualTo(9);
        assertThat(k.metrosTela()).isEqualTo(277);
        assertThat(k.colores()).isEqualTo(21);
    }

    @Test
    void telasYMaterialesDeTela() {
        assertThat(telas.porCodigo("TL-07").orElseThrow().getColor()).isEqualTo("Mostaza");
        assertThat(telas.porCodigo("TL-07").orElseThrow().getEstado()).isEqualTo("Stock bajo");
        assertThat(telas.porCodigo("TL-11").orElseThrow().getEstado()).isEqualTo("Agotado");
        for (TipoTela t : TipoTela.values()) {
            double suma = telas.porTipo(t).stream().filter(x -> !x.getDescontinuada()).mapToDouble(Tela::getStock).sum();
            assertThat(inventario.material(t.materialCodigo()).getStock()).as(t.nombre()).isCloseTo(suma, within(1e-6));
        }
    }

    @Test
    void ordenesConReservasYTresParciales() {
        List<OrdenProduccion> productivas = produccion.activas().stream().filter(o -> o.getEtapa().productiva() && !o.isSinFicha()).toList();
        long parciales = productivas.stream().filter(o -> produccion.estadoMateriales(o).faltantes() > 0).count();
        assertThat(parciales).isEqualTo(3);
        // Cada color reservado alcanza para su OP
        Map<String, Double> porColor = new HashMap<>();
        for (OrdenProduccion o : productivas) {
            Explosion.Resultado r = produccion.requerimiento(o);
            r.porMaterial().stream().filter(p -> p.material().isTela()).forEach(p -> porColor.merge(o.getTela().getCodigo(), p.cantidad(), Double::sum));
        }
        var soft = new org.assertj.core.api.SoftAssertions();
        porColor.forEach((codigo, metros) -> soft.assertThat(telas.porCodigo(codigo).orElseThrow().getStock()).as(codigo).isGreaterThanOrEqualTo(metros));
        soft.assertAll();
        // Ningun material queda con disponible negativo
        assertThat(inventario.materiales()).allMatch(m -> m.getStock() - m.getReservado() >= -1e-6);
    }

    @Test
    void kardexCuadraConElStock() {
        for (Material m : inventario.materiales()) {
            double saldo = movimientos.findByMaterialIdOrderByFechaAscIdAsc(m.getId()).stream().mapToDouble(Movimiento::getEfecto).sum();
            assertThat(saldo).as(m.getCodigo()).isCloseTo(m.getStock(), within(1e-6));
        }
    }

    @Test
    void alertasDeLaBarraSuperior() {
        List<String> titulos = notificaciones.todas().stream().map(NotificacionService.Notificacion::titulo).toList();
        assertThat(titulos).anyMatch(t -> t.startsWith("Stock crítico") && t.contains("fibra siliconada") && t.contains("Mostaza"));
        assertThat(titulos).anyMatch(t -> t.contains("reclinable de Carmen Flores") && t.contains("14/10"));
        assertThat(titulos).anyMatch(t -> t.startsWith("Entrega mañana: 4 sofás para el Hotel Royal Inn"));
        assertThat(titulos).anyMatch(t -> t.startsWith("Material agotado") && t.contains("barniz para patas"));
        assertThat(titulos).anyMatch(t -> t.contains("bloqueadas por falta de material"));
    }

    @Test
    void estadoDelModeloSaleDelStock() {
        assertThat(productos.porCodigo("SF-007").orElseThrow().getEstado()).isEqualTo("A pedido");
        assertThat(productos.porCodigo("SF-003").orElseThrow().getEstado()).isEqualTo("Stock bajo");
        assertThat(productos.porCodigo("SF-001").orElseThrow().getEstado()).isEqualTo("Disponible");
        assertThat(productos.listar()).hasSize(11);
        assertThat(productos.costoEstandar(productos.porCodigo("SF-011").orElseThrow())).isNull();
        assertThat(productos.costoEstandar(productos.porCodigo("SF-001").orElseThrow()).ficha().getEtiqueta()).isEqualTo("FT-001 v3");
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
