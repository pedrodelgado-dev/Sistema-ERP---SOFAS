/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: VentasYPedidosTest
 * Descripción: Archivo fuente Java de la clase VentasYPedidosTest.
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@IntegracionTest
class VentasYPedidosTest {

    @Autowired VentaService ventas;
    @Autowired ProductoService productos;
    @Autowired ClienteService clientes;
    @Autowired PedidoService pedidos;
    @Autowired ConfiguradorService configurador;
    @Autowired ProduccionService produccion;

    @Test
    void cobrarDescuentaStockYNumeraLaBoleta() {
        Producto p = productos.porCodigo("SF-001").orElseThrow();
        int stock = p.getStock();
        int numero = ventas.siguienteNumero(Venta.Comprobante.BOLETA);
        Venta v = ventas.cobrar(new VentaService.Cobro(null, Venta.Comprobante.BOLETA,
                List.of(new VentaService.Linea(p.getId(), 1, null)), BigDecimal.ZERO, "Efectivo", new BigDecimal("2500"), null, null, null));
        assertThat(v.getSerie()).isEqualTo("B001");
        assertThat(v.getNumero()).isEqualTo(numero);
        assertThat(v.getVuelto()).isEqualByComparingTo("10.00");
        assertThat(productos.porCodigo("SF-001").orElseThrow().getStock()).isEqualTo(stock - 1);
    }

    @Test
    void facturaExigeRucYMixtoDebeSumarElTotal() {
        Producto p = productos.porCodigo("SF-010").orElseThrow();
        Cliente persona = clientes.listar().stream().filter(c -> !c.tieneRuc()).findFirst().orElseThrow();
        assertThatThrownBy(() -> ventas.cobrar(new VentaService.Cobro(persona.getId(), Venta.Comprobante.FACTURA,
                List.of(new VentaService.Linea(p.getId(), 1, null)), null, "Tarjeta", null, null, null, null)))
                .isInstanceOf(NegocioException.class).hasMessageContaining("RUC");
        assertThatThrownBy(() -> ventas.cobrar(new VentaService.Cobro(null, Venta.Comprobante.BOLETA,
                List.of(new VentaService.Linea(p.getId(), 1, null)), null, "Mixto", null, "Yape", new BigDecimal("100"), new BigDecimal("100"))))
                .isInstanceOf(NegocioException.class);
        Venta ok = ventas.cobrar(new VentaService.Cobro(null, Venta.Comprobante.BOLETA,
                List.of(new VentaService.Linea(p.getId(), 1, null)), null, "Mixto", null, "Yape", new BigDecimal("160"), new BigDecimal("100")));
        assertThat(ok.getMetodoPago()).startsWith("Mixto");
        Producto sinStock = productos.porCodigo("SF-007").orElseThrow();
        assertThatThrownBy(() -> ventas.cobrar(new VentaService.Cobro(null, Venta.Comprobante.BOLETA,
                List.of(new VentaService.Linea(sinStock.getId(), 1, null)), null, "Tarjeta", null, null, null, null)))
                .isInstanceOf(NegocioException.class).hasMessageContaining("Stock insuficiente");
    }

    @Test
    void pedidoNecesitaElCincuentaPorCientoParaConfirmarYGenerarOP() {
        Pedido p = pedidos.listar().stream().filter(x -> x.getCodigo().equals("PE-0249")).findFirst().orElseThrow();
        assertThatThrownBy(() -> pedidos.avanzarA(p.getId(), EstadoPedido.CONFIRMADO)).isInstanceOf(NegocioException.class);
        assertThatThrownBy(() -> pedidos.generarOP(p.getId(), null)).isInstanceOf(NegocioException.class);
        Pedido conAdelanto = pedidos.registrarAdelanto(p.getId(), p.getPrecio().multiply(new BigDecimal("0.5")), "Yape");
        assertThat(conAdelanto.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        OrdenProduccion op = pedidos.generarOP(p.getId(), null);
        assertThat(op.getFichaUid()).isEqualTo("FT-009v1");
        assertThat(pedidos.pedido(p.getId()).getEstado()).isEqualTo(EstadoPedido.EN_PRODUCCION);
        assertThat(pedidos.pedido(p.getId()).getOpCodigo()).isEqualTo(op.getCodigo());
    }

    @Test
    void crearPedidoDesdeElConfiguradorConSuOP() {
        Cliente c = clientes.listar().get(0);
        ConfigSofa cfg = configurador.defecto(ConfigSofa.Modelo.CHESTERFIELD);
        ConfiguradorService.Cotizacion cot = configurador.cotizar(cfg);
        Pedido p = configurador.crearPedido(cfg, c.getId(), null, BigDecimal.valueOf(cot.precio() / 2), "Efectivo", null, true, null);
        assertThat(p.getTipo()).isEqualTo(Pedido.Tipo.PERSONALIZADO);
        assertThat(p.getPrecio()).isEqualByComparingTo(BigDecimal.valueOf(cot.precio()));
        assertThat(p.getOpCodigo()).isNotNull();
        OrdenProduccion op = produccion.porCodigo(p.getOpCodigo()).orElseThrow();
        assertThat(op.getFichaUid()).isEqualTo("FT-001v3");
        assertThat(op.getExtras()).anyMatch(x -> x.getMaterialCodigo().equals("MP-016") && x.getCantidad() == 48);
        assertThat(op.getSustituciones()).containsEntry("MP-003", "MP-004");
        assertThat(ConfigSofa.leer(p.getConfiguracion())).isEqualTo(cfg);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
