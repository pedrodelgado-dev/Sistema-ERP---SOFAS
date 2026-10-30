/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: GestorCarritoServiceTest
 * Descripción: Archivo fuente Java de la clase GestorCarritoServiceTest.
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
import pe.erp.sofas.repository.ConfiguracionGuardadaRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GestorCarritoServiceTest {

    private GestorCarritoService service;

    @Mock
    private CarritoRepository carritoRepo;

    @Mock
    private ConfiguracionGuardadaRepository configRepo;

    @Mock
    private CalculoPrecioService precioService;

    private Usuario usuarioTest;
    private ConfiguracionGuardada configTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GestorCarritoService(carritoRepo, configRepo, precioService);

        usuarioTest = new Usuario();
        usuarioTest.setId(1L);
        usuarioTest.setNombre("Test User");

        configTest = ConfiguracionGuardada.builder()
            .id(1L)
            .usuario(usuarioTest)
            .nombre("Mi Moderno")
            .modelo(ConfigSofa.Modelo.MODERNO)
            .cuerpos(3)
            .build();
    }

    @Test
    void testObtenerOCrearCarritoNuevo() {
        when(carritoRepo.findByUsuario(usuarioTest)).thenReturn(Optional.empty());
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(inv -> {
            Carrito c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Carrito carrito = service.obtenerOCrearCarrito(usuarioTest);

        assertNotNull(carrito);
        assertEquals(usuarioTest, carrito.getUsuario());
    }

    @Test
    void testAgregarAlCarrito() {
        Carrito carrito = Carrito.builder().usuario(usuarioTest).build();

        when(carritoRepo.findByUsuario(usuarioTest)).thenReturn(Optional.of(carrito));
        when(configRepo.findByIdAndUsuario(1L, usuarioTest)).thenReturn(Optional.of(configTest));
        when(carritoRepo.save(any(Carrito.class))).thenReturn(carrito);

        service.agregarAlCarrito(usuarioTest, 1L, 2);

        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void testRemoverDelCarrito() {
        CarritoItem item = CarritoItem.builder().id(1L).configuracion(configTest).cantidad(2).build();
        List<CarritoItem> items = new ArrayList<>();
        items.add(item);
        Carrito carrito = Carrito.builder().usuario(usuarioTest).items(items).build();

        when(carritoRepo.findByUsuario(usuarioTest)).thenReturn(Optional.of(carrito));
        when(carritoRepo.save(any(Carrito.class))).thenReturn(carrito);

        service.removerDelCarrito(usuarioTest, 1L);

        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void testActualizarCantidad() {
        CarritoItem item = CarritoItem.builder().id(1L).configuracion(configTest).cantidad(2).build();
        List<CarritoItem> items = new ArrayList<>();
        items.add(item);
        Carrito carrito = Carrito.builder().usuario(usuarioTest).items(items).build();

        when(carritoRepo.findByUsuario(usuarioTest)).thenReturn(Optional.of(carrito));
        when(carritoRepo.save(any(Carrito.class))).thenReturn(carrito);

        service.actualizarCantidad(usuarioTest, 1L, 5);

        assertEquals(5, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void testCalcularTotal() {
        CarritoItem item = CarritoItem.builder().id(1L).configuracion(configTest).cantidad(2).build();
        List<CarritoItem> items = new ArrayList<>();
        items.add(item);
        Carrito carrito = Carrito.builder().usuario(usuarioTest).items(items).build();

        when(carritoRepo.findByUsuario(usuarioTest)).thenReturn(Optional.of(carrito));
        when(precioService.calcularPrecio(any(ConfigSofa.class))).thenReturn(BigDecimal.valueOf(1500));

        BigDecimal total = service.calcularTotal(usuarioTest);

        assertEquals(BigDecimal.valueOf(3000).setScale(2, java.math.RoundingMode.HALF_UP), total);
    }

    @Test
    void testObtenerDetalleCarrito() {
        CarritoItem item = CarritoItem.builder().id(1L).configuracion(configTest).cantidad(2).build();
        List<CarritoItem> items = new ArrayList<>();
        items.add(item);
        Carrito carrito = Carrito.builder().usuario(usuarioTest).items(items).build();

        when(carritoRepo.findByUsuario(usuarioTest)).thenReturn(Optional.of(carrito));
        when(precioService.calcularPrecio(any(ConfigSofa.class))).thenReturn(BigDecimal.valueOf(1500));

        GestorCarritoService.DetalleCarrito detalle = service.obtenerDetalleCarrito(usuarioTest);

        assertEquals(1, detalle.cantidadItems());
        assertEquals(2, detalle.cantidadTotal());
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
