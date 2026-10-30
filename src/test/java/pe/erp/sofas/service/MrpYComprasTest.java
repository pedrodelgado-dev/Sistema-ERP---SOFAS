/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: MrpYComprasTest
 * Descripción: Archivo fuente Java de la clase MrpYComprasTest.
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
import pe.erp.sofas.service.calculo.UnidadCompra;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@IntegracionTest
class MrpYComprasTest {

    @Autowired MrpService mrp;
    @Autowired CompraService compras;
    @Autowired InventarioService inventario;
    @Autowired TelaService telas;

    @Test
    void faltanteNetoConsideraLasComprasEnTransito() {
        MrpService.Resultado r = mrp.calcular(MrpService.Filtro.TODO);
        assertThat(r.ops()).isGreaterThan(0);
        for (MrpService.Fila f : r.filas()) {
            assertThat(f.faltante()).isCloseTo(Math.max(0, f.requerido() - f.stock() - f.transito()), within(1e-6));
        }
        // El barniz tiene 3 gal en la OC-0320 aprobada (en transito); la OC-0321 de fibra sigue en Borrador
        MrpService.Fila barniz = r.filas().stream().filter(f -> f.material().getCodigo().equals("MP-029")).findFirst().orElseThrow();
        assertThat(barniz.transito()).isEqualTo(3);
        assertThat(barniz.faltante()).isZero();
        MrpService.Fila fibra = r.filas().stream().filter(f -> f.material().getCodigo().equals("MP-013")).findFirst().orElseThrow();
        assertThat(fibra.transito()).isZero();
        assertThat(fibra.faltante()).isGreaterThan(0);
        // Incluye el pedido confirmado sin OP (PE-0248)
        assertThat(r.pedidos()).isEqualTo(1);
    }

    @Test
    void generarOrdenesRedondeaALaUnidadDeCompraYRecibirSumaStock() {
        Material madera = inventario.material("MP-001");
        MrpService.Fila fila = new MrpService.Fila(madera, 120, 0, 37, null, madera.getProveedor(), java.util.Set.of("OP-T"));
        List<String> codigos = mrp.generarOrdenes(List.of(fila), "MRP");
        assertThat(codigos).hasSize(1);
        OrdenCompra oc = compras.listar().stream().filter(o -> o.getCodigo().equals(codigos.get(0))).findFirst().orElseThrow();
        assertThat(oc.getEstado()).isEqualTo(EstadoCompra.BORRADOR);
        assertThat(oc.getOrigen()).isEqualTo("MRP");
        assertThat(oc.getItems().get(0).getCantidad()).isEqualTo(UnidadCompra.redondear("pt", 37)).isEqualTo(50);

        double antes = inventario.material("MP-001").getStock();
        compras.solicitar(oc.getId());
        compras.aprobar(oc.getId());
        compras.recibir(oc.getId());
        assertThat(inventario.material("MP-001").getStock()).isEqualTo(antes + 50);
        assertThat(inventario.kardex(madera.getId())).anyMatch(m -> m.getTipo() == TipoMovimiento.ENTRADA && m.getReferencia().equals(oc.getCodigo()));
    }

    @Test
    void recibirTelaEntraAlColorYMantieneLaSuma() {
        Material lino = inventario.material("MP-006");
        Tela salvia = telas.porCodigo("TL-11").orElseThrow();
        OrdenCompra oc = compras.crear(lino.getProveedor().getId(), List.of(new CompraService.LineaCompra(lino.getId(), salvia.getId(), 20,
                java.math.BigDecimal.valueOf(34))), true, null, "Manual", false);
        compras.aprobar(oc.getId());
        compras.recibir(oc.getId());
        assertThat(telas.porCodigo("TL-11").orElseThrow().getStock()).isEqualTo(20);
        double suma = telas.porTipo(TipoTela.LINO).stream().filter(t -> !t.getDescontinuada()).mapToDouble(Tela::getStock).sum();
        assertThat(inventario.material("MP-006").getStock()).isCloseTo(suma, within(1e-6));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
