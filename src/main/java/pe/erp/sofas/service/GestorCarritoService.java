/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: GestorCarritoService
 * Descripción: Archivo fuente Java de la clase GestorCarritoService.
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
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.CarritoRepository;
import pe.erp.sofas.repository.ConfiguracionGuardadaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GestorCarritoService {

    private final CarritoRepository carritoRepo;
    private final ConfiguracionGuardadaRepository configRepo;
    private final CalculoPrecioService precioService;

    public Carrito obtenerOCrearCarrito(Usuario usuario) {
        Optional<Carrito> existente = carritoRepo.findByUsuario(usuario);
        if (existente.isPresent()) {
            return existente.get();
        }

        Carrito nuevo = Carrito.builder()
            .usuario(usuario)
            .build();
        return carritoRepo.save(nuevo);
    }

    public Carrito agregarAlCarrito(Usuario usuario, Long configuracionId, Integer cantidad) {
        Carrito carrito = obtenerOCrearCarrito(usuario);

        ConfiguracionGuardada config = configRepo.findByIdAndUsuario(configuracionId, usuario)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));

        // Verificar si ya existe en carrito
        Optional<CarritoItem> existente = carrito.getItems().stream()
            .filter(item -> item.getConfiguracion().getId().equals(configuracionId))
            .findFirst();

        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + cantidad);
        } else {
            carrito.agregarItem(config, cantidad);
        }

        return carritoRepo.save(carrito);
    }

    public Carrito removerDelCarrito(Usuario usuario, Long itemId) {
        Carrito carrito = obtenerOCrearCarrito(usuario);
        carrito.removerItem(itemId);
        return carritoRepo.save(carrito);
    }

    public Carrito actualizarCantidad(Usuario usuario, Long itemId, Integer nuevaCantidad) {
        Carrito carrito = obtenerOCrearCarrito(usuario);

        if (nuevaCantidad <= 0) {
            carrito.removerItem(itemId);
        } else {
            carrito.actualizarCantidad(itemId, nuevaCantidad);
        }

        return carritoRepo.save(carrito);
    }

    public Carrito vaciarCarrito(Usuario usuario) {
        Carrito carrito = obtenerOCrearCarrito(usuario);
        carrito.vaciar();
        return carritoRepo.save(carrito);
    }

    public List<CarritoItem> listarItems(Usuario usuario) {
        Carrito carrito = obtenerOCrearCarrito(usuario);
        return carrito.getItems();
    }

    public BigDecimal calcularTotal(Usuario usuario) {
        Carrito carrito = obtenerOCrearCarrito(usuario);
        BigDecimal total = BigDecimal.ZERO;

        for (CarritoItem item : carrito.getItems()) {
            ConfigSofa config = item.getConfiguracion().toConfigSofa();
            BigDecimal precioUnitario = precioService.calcularPrecio(config);
            total = total.add(precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        return total.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public DetalleCarrito obtenerDetalleCarrito(Usuario usuario) {
        Carrito carrito = obtenerOCrearCarrito(usuario);
        Integer cantidadItems = carrito.obtenerCantidadItems();
        Integer cantidadTotal = carrito.obtenerCantidadTotal();
        BigDecimal precioTotal = calcularTotal(usuario);

        return new DetalleCarrito(
            cantidadItems,
            cantidadTotal,
            precioTotal,
            carrito.getItems()
        );
    }

    public void eliminarCarrito(Usuario usuario) {
        carritoRepo.deleteByUsuario(usuario);
    }

    public record DetalleCarrito(
        Integer cantidadItems,
        Integer cantidadTotal,
        BigDecimal precioTotal,
        List<CarritoItem> items
    ) {}
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
