/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ProductoService
 * Descripción: Costo estandar de la ficha aprobada del modelo (null si no tiene).
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
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {

    /** Costo estandar de la ficha aprobada del modelo (null si no tiene). */
    public record CostoEstandar(FichaTecnica ficha, double costo, double margenPct) {
    }

    private final ProductoRepository repository;
    private final FichaService fichaService;
    private final OrdenProduccionRepository opRepository;
    private final PedidoRepository pedidoRepository;

    public List<Producto> listar() {
        return repository.findAllByOrderByCodigoAsc().stream().filter(Producto::getActivo).toList();
    }

    public Producto producto(Long id) {
        return repository.findById(id).orElseThrow(() -> new NegocioException("El modelo ya no existe."));
    }

    public Optional<Producto> porCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public String siguienteCodigo() {
        return Codigos.siguiente(repository.findAll(), Producto::getCodigo, "SF-", 3);
    }

    public CostoEstandar costoEstandar(Producto p) {
        FichaTecnica f = fichaService.aprobadaDe(p);
        if (f == null) return null;
        double costo = fichaService.costoEstandar(f);
        double precio = p.getPrecio().doubleValue();
        return new CostoEstandar(f, costo, precio == 0 ? 0 : (precio - costo) * 100 / precio);
    }

    @Transactional
    public Producto guardar(Producto p) {
        validar(p);
        if (p.getId() == null) p.setCodigo(siguienteCodigo());
        return repository.save(p);
    }

    public static void validar(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) throw new NegocioException("El nombre del modelo es obligatorio.");
        if (p.getPrecio() == null || p.getPrecio().signum() <= 0) throw new NegocioException("Ingresa el precio de venta.");
        if (p.getCosto() == null || p.getCosto().signum() < 0) throw new NegocioException("Ingresa el costo.");
        if (p.getPrecio().compareTo(p.getCosto()) <= 0) throw new NegocioException("El precio debe ser mayor que el costo.");
        if (p.getStock() == null || p.getStock() < 0) throw new NegocioException("El stock no puede ser negativo.");
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = producto(id);
        boolean enUso = opRepository.findAll().stream().anyMatch(o -> o.getProducto() != null && o.getProducto().getId().equals(id) && o.isActiva())
                || pedidoRepository.findAll().stream().anyMatch(x -> x.getProducto() != null && x.getProducto().getId().equals(id) && !x.getEstado().cerrado());
        if (enUso) throw new NegocioException("El modelo tiene pedidos u órdenes activas; no se puede eliminar.");
        // Baja logica: se conserva para el historial de ventas.
        p.setActivo(false);
        repository.save(p);
    }

    @Transactional
    public Producto fijarImagen(Long id, int indice, String ruta) {
        Producto p = producto(id);
        p.setImagen(indice, ruta);
        return repository.save(p);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
