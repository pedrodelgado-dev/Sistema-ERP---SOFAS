/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CreadorPedidoServiceTest
 * Descripción: Archivo fuente Java de la clase CreadorPedidoServiceTest.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.CarritoRepository;
import pe.erp.sofas.repository.PedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreadorPedidoServiceTest {

    private CreadorPedidoService service;

    @Mock
    private CarritoRepository carritoRepo;

    @Mock
    private PedidoRepository pedidoRepo;

    @Mock
    private CalculoPrecioService precioService;

    @Mock
    private GestorCarritoService gestorCarrito;

    private Usuario usuarioTest;
    private Cliente clienteTest;
    private ConfiguracionGuardada configTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CreadorPedidoService(carritoRepo, pedidoRepo, precioService, gestorCarrito);

        usuarioTest = new Usuario();
        usuarioTest.setId(1L);

        clienteTest = new Cliente();
        clienteTest.setId(1L);
        clienteTest.setNombre("Cliente Test");

        configTest = ConfiguracionGuardada.builder()
            .id(1L)
            .usuario(usuarioTest)
            .nombre("Mi Moderno")
            .modelo(ConfigSofa.Modelo.MODERNO)
            .cuerpos(3)
            .build();
    }

    @Test
    void testCrearPedidoDesdeCarritoVacio() {
        Carrito carritoVacio = Carrito.builder().usuario(usuarioTest).items(new ArrayList<>()).build();
        when(gestorCarrito.obtenerOCrearCarrito(usuarioTest)).thenReturn(carritoVacio);

        assertThrows(IllegalArgumentException.class, () ->
            service.crearPedidosDesdeCarrito(usuarioTest, clienteTest, false)
        );
    }

    @Test
    void testCrearPedidoDesdeCarritoConItems() {
        CarritoItem item = CarritoItem.builder()
            .id(1L)
            .configuracion(configTest)
            .cantidad(2)
            .build();

        List<CarritoItem> items = new ArrayList<>();
        items.add(item);

        Carrito carrito = Carrito.builder()
            .usuario(usuarioTest)
            .items(items)
            .build();

        when(gestorCarrito.obtenerOCrearCarrito(usuarioTest)).thenReturn(carrito);
        when(precioService.calcularPrecio(any(ConfigSofa.class))).thenReturn(BigDecimal.valueOf(1500));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        List<Pedido> pedidos = service.crearPedidosDesdeCarrito(usuarioTest, clienteTest, false);

        assertEquals(1, pedidos.size());
        Pedido pedido = pedidos.get(0);
        assertEquals(clienteTest, pedido.getCliente());
        assertEquals(2, pedido.getCantidad());
        assertEquals(BigDecimal.valueOf(3000), pedido.getPrecio());
    }

    @Test
    void testCrearPedidoVaciaCarrito() {
        CarritoItem item = CarritoItem.builder()
            .id(1L)
            .configuracion(configTest)
            .cantidad(1)
            .build();

        List<CarritoItem> items = new ArrayList<>();
        items.add(item);

        Carrito carrito = Carrito.builder()
            .usuario(usuarioTest)
            .items(items)
            .build();

        when(gestorCarrito.obtenerOCrearCarrito(usuarioTest)).thenReturn(carrito);
        when(precioService.calcularPrecio(any(ConfigSofa.class))).thenReturn(BigDecimal.valueOf(1500));
        when(pedidoRepo.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        service.crearPedidosDesdeCarrito(usuarioTest, clienteTest, true);

        verify(gestorCarrito).vaciarCarrito(usuarioTest);
    }

    @Test
    void testRegistrarAdelanto() {
        Pedido pedido = Pedido.builder()
            .id(1L)
            .precio(BigDecimal.valueOf(3000))
            .adelanto(BigDecimal.valueOf(500))
            .build();

        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(pedido);

        service.registrarAdelanto(1L, BigDecimal.valueOf(1000));

        assertEquals(BigDecimal.valueOf(1500), pedido.getAdelanto());
    }

    @Test
    void testRegistrarAdelantoExcedente() {
        Pedido pedido = Pedido.builder()
            .id(1L)
            .precio(BigDecimal.valueOf(1000))
            .adelanto(BigDecimal.valueOf(500))
            .build();

        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () ->
            service.registrarAdelanto(1L, BigDecimal.valueOf(700))
        );
    }

    @Test
    void testCambiarEstado() {
        Pedido pedido = Pedido.builder()
            .id(1L)
            .estado(EstadoPedido.CONFIRMADO)
            .build();

        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(pedido);

        service.cambiarEstado(1L, EstadoPedido.EN_PRODUCCION);

        assertEquals(EstadoPedido.EN_PRODUCCION, pedido.getEstado());
    }

    @Test
    void testResumenPedidos() {
        Pedido p1 = Pedido.builder()
            .id(1L)
            .precio(BigDecimal.valueOf(1500))
            .cantidad(1)
            .build();

        Pedido p2 = Pedido.builder()
            .id(2L)
            .precio(BigDecimal.valueOf(3000))
            .cantidad(2)
            .build();

        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(p1);
        pedidos.add(p2);

        CreadorPedidoService.ResumenPedidos resumen = CreadorPedidoService.ResumenPedidos.desde(pedidos);

        assertEquals(2, resumen.totalPedidos());
        assertEquals(BigDecimal.valueOf(4500), resumen.montoTotal());
        assertEquals(3, resumen.cantidadTotal());
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
