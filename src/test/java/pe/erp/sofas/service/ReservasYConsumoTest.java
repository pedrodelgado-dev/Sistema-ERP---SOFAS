/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ReservasYConsumoTest
 * Descripción: Archivo fuente Java de la clase ReservasYConsumoTest.
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
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.ConsumoRealRepository;
import pe.erp.sofas.repository.MovimientoRepository;
import pe.erp.sofas.service.calculo.Explosion;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@IntegracionTest
class ReservasYConsumoTest {

    @Autowired ProduccionService produccion;
    @Autowired ProductoService productos;
    @Autowired InventarioService inventario;
    @Autowired TelaService telas;
    @Autowired MovimientoRepository movimientos;
    @Autowired ConsumoRealRepository consumos;

    private OrdenProduccion nueva(String producto, int cant, String tela) {
        Producto p = productos.porCodigo(producto).orElseThrow();
        Tela t = telas.porCodigo(tela).orElseThrow();
        return produccion.crear(new ProduccionService.NuevaOP(p.getId(), null, cant, null, null, Prioridad.MEDIA, null, null,
                t.getId(), null, Map.of(), List.of(), null, null, null, Etapa.PENDIENTE, null, null));
    }

    @Test
    void reservaLiberacionYConsumo() {
        OrdenProduccion op = nueva("SF-002", 1, "TL-08");
        assertThat(op.getFichaUid()).isEqualTo("FT-002v1");
        assertThat(op.getSustituciones()).containsEntry("MP-003", "MP-003");
        Explosion.Resultado req = produccion.requerimiento(op);
        Material madera = inventario.material("MP-001");
        double reservadoAntes = madera.getReservado();
        double stockAntes = madera.getStock();
        double necesario = req.porMaterial().stream().filter(x -> x.material().getCodigo().equals("MP-001")).findFirst().orElseThrow().cantidad();

        // Pendiente → Corte de madera: reserva
        assertThat(produccion.evaluar(op, Etapa.CORTE_MADERA).requisito()).isIn(ProduccionService.Requisito.NINGUNO, ProduccionService.Requisito.CONFIRMAR_FALTANTES);
        produccion.mover(op.getId(), Etapa.CORTE_MADERA, null);
        assertThat(inventario.material("MP-001").getReservado()).isCloseTo(reservadoAntes + necesario, within(1e-6));
        assertThat(inventario.material("MP-001").getStock()).isEqualTo(stockAntes);
        assertThat(produccion.reservadoPor(produccion.op(op.getId()), madera)).isCloseTo(necesario, within(1e-6));

        // Volver a Pendiente libera
        produccion.mover(op.getId(), Etapa.PENDIENTE, null);
        assertThat(inventario.material("MP-001").getReservado()).isCloseTo(reservadoAntes, within(1e-6));

        // Avanzar otra vez y pasar a Listo consume lo reservado
        produccion.mover(op.getId(), Etapa.TAPIZADO, null);
        double telaAntes = telas.porCodigo("TL-08").orElseThrow().getStock();
        long consumosAntes = consumos.count();
        produccion.completar(op.getId(), Map.of(madera.getId(), necesario + 1));
        Material despues = inventario.material("MP-001");
        assertThat(despues.getReservado()).isCloseTo(reservadoAntes, within(1e-6));
        assertThat(despues.getStock()).isCloseTo(stockAntes - necesario - 1, within(1e-6));
        assertThat(produccion.op(op.getId()).getEtapa()).isEqualTo(Etapa.LISTO);
        List<Movimiento> kardex = movimientos.findByMaterialIdOrderByFechaAscIdAsc(madera.getId());
        assertThat(kardex).anyMatch(m -> m.getTipo() == TipoMovimiento.CONSUMO_OP && m.getReferencia().equals(op.getCodigo()));
        assertThat(kardex).anyMatch(m -> m.getTipo() == TipoMovimiento.RESERVA && m.getReferencia().equals(op.getCodigo()));
        assertThat(kardex).anyMatch(m -> m.getTipo() == TipoMovimiento.LIBERACION && m.getReferencia().equals(op.getCodigo()));
        assertThat(consumos.count()).isEqualTo(consumosAntes + req.porMaterial().size());
        // La tela consumida sale del color de la OP y el material sigue siendo la suma de colores
        assertThat(telas.porCodigo("TL-08").orElseThrow().getStock()).isLessThan(telaAntes);
        double suma = telas.porTipo(TipoTela.CHENILLE).stream().filter(t -> !t.getDescontinuada()).mapToDouble(Tela::getStock).sum();
        assertThat(inventario.material("MP-003").getStock()).isCloseTo(suma, within(1e-6));
        assertThatThrownBy(() -> produccion.mover(op.getId(), Etapa.COSTURA, null)).isInstanceOf(NegocioException.class);
    }

    @Test
    void unaReservaNuncaDejaElDisponibleNegativo() {
        // El barniz esta agotado (todo reservado): una OP nueva no puede reservar y queda con faltante.
        Material barniz = inventario.material("MP-029");
        assertThat(barniz.getDisponible()).isZero();
        OrdenProduccion op = nueva("SF-001", 1, "TL-01");
        ProduccionService.ResultadoReserva r = produccion.reservar(op.getId());
        assertThat(r.faltantes()).anyMatch(f -> f.material().getCodigo().equals("MP-029"));
        Material despues = inventario.material("MP-029");
        assertThat(despues.getStock() - despues.getReservado()).isGreaterThanOrEqualTo(-1e-9);
        assertThat(produccion.estadoMateriales(produccion.op(op.getId())).lineas())
                .filteredOn(l -> l.material().getCodigo().equals("MP-029")).first().extracting(ProduccionService.LineaMaterial::estado).isEqualTo("Faltará");
        produccion.anular(op.getId());
        assertThat(produccion.reservadoPor(produccion.op(op.getId()), inventario.material("MP-001"))).isZero();
    }

    @Test
    void sinFichaAprobadaPideAutorizacion() {
        OrdenProduccion op = nueva("SF-011", 1, "TL-16");
        assertThat(op.isSinFicha()).isTrue();
        assertThat(produccion.evaluar(op, Etapa.CORTE_MADERA).requisito()).isEqualTo(ProduccionService.Requisito.AUTORIZACION);
        assertThatThrownBy(() -> produccion.mover(op.getId(), Etapa.CORTE_MADERA, null)).isInstanceOf(NegocioException.class);
        produccion.mover(op.getId(), Etapa.CORTE_MADERA, "Autorizado por Supervisor");
        OrdenProduccion despues = produccion.op(op.getId());
        assertThat(despues.getEtapa()).isEqualTo(Etapa.CORTE_MADERA);
        assertThat(produccion.indicador(despues).texto()).isEqualTo("Sin ficha · autorizado");
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
