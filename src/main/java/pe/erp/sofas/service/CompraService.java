/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CompraService
 * Descripción: Ordenes de compra: Borrador → Solicitada → Aprobada → Recibida (o Cancelada).
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
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Ordenes de compra: Borrador → Solicitada → Aprobada → Recibida (o Cancelada). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompraService {

    public record LineaCompra(Long materialId, Long telaId, double cantidad, BigDecimal precioUnitario) {
    }

    private final OrdenCompraRepository repository;
    private final ProveedorRepository proveedorRepository;
    private final MaterialRepository materialRepository;
    private final TelaRepository telaRepository;
    private final InventarioService inventarioService;
    private final TelaService telaService;

    public List<OrdenCompra> listar() {
        return repository.findAllByOrderByCodigoDesc();
    }

    public OrdenCompra orden(Long id) {
        return repository.findById(id).orElseThrow(() -> new NegocioException("La orden de compra ya no existe."));
    }

    public String siguienteCodigo() {
        return Codigos.siguiente(repository.findAll(), OrdenCompra::getCodigo, "OC-", 4);
    }

    /** Ordenes aprobadas o recibidas aun no pagadas. */
    public List<OrdenCompra> porPagar() {
        return listar().stream()
                .filter(o -> (o.getEstado() == EstadoCompra.APROBADA || o.getEstado() == EstadoCompra.RECIBIDA) && o.getFechaPago() == null)
                .toList();
    }

    @Transactional
    public OrdenCompra crear(Long proveedorId, List<LineaCompra> lineas, boolean enviarSolicitud, String observaciones,
                             String origen, boolean nueva) {
        if (proveedorId == null) throw new NegocioException("Selecciona un proveedor.");
        Proveedor proveedor = proveedorRepository.findById(proveedorId).orElseThrow();
        if (!proveedor.getActivo()) throw new NegocioException("El proveedor está inactivo.");
        List<LineaCompra> validas = lineas.stream().filter(l -> l.materialId() != null && l.cantidad() > 0).toList();
        if (validas.isEmpty()) throw new NegocioException("Agrega al menos un material con cantidad.");
        OrdenCompra orden = OrdenCompra.builder()
                .codigo(siguienteCodigo()).proveedor(proveedor).fecha(AppClock.hoy()).fechaEntrega(AppClock.hoy().plusDays(5))
                .estado(enviarSolicitud ? EstadoCompra.SOLICITADA : EstadoCompra.BORRADOR)
                .origen(origen == null ? "Manual" : origen).nueva(nueva)
                .observaciones(observaciones).build();
        for (LineaCompra l : validas) {
            if (l.precioUnitario() == null || l.precioUnitario().signum() < 0) throw new NegocioException("Revisa los precios unitarios.");
            Material m = materialRepository.findById(l.materialId()).orElseThrow();
            Tela tela = l.telaId() == null ? null : telaRepository.findById(l.telaId()).orElse(null);
            if (tela == null && m.isTela()) tela = colorParaReponer(m);
            orden.agregar(ItemCompra.builder().material(m).tela(tela).cantidad(l.cantidad()).precioUnitario(l.precioUnitario()).build());
        }
        return repository.save(orden);
    }

    /** Color de la tela con menor cobertura (stock / minimo) para reponer. */
    private Tela colorParaReponer(Material m) {
        TipoTela tipo = TipoTela.porMaterial(m.getCodigo());
        return telaService.porTipo(tipo).stream().filter(t -> !t.getDescontinuada())
                .min(Comparator.comparingDouble(t -> t.getStock() / Math.max(1, t.getMinimo()))).orElse(null);
    }

    @Transactional
    public OrdenCompra solicitar(Long id) {
        return transicion(id, EstadoCompra.BORRADOR, EstadoCompra.SOLICITADA);
    }

    @Transactional
    public OrdenCompra aprobar(Long id) {
        return transicion(id, EstadoCompra.SOLICITADA, EstadoCompra.APROBADA);
    }

    @Transactional
    public void marcarVista(Long id) {
        OrdenCompra o = orden(id);
        if (Boolean.TRUE.equals(o.getNueva())) {
            o.setNueva(false);
            repository.save(o);
        }
    }

    /** Recepcion: suma al stock (en las telas, al color del muestrario) y registra "Entrada" en el kardex. */
    @Transactional
    public OrdenCompra recibir(Long id) {
        OrdenCompra orden = orden(id);
        if (orden.getEstado() != EstadoCompra.APROBADA) throw new NegocioException("Solo se pueden recibir órdenes aprobadas.");
        for (ItemCompra item : orden.getItems()) {
            Material m = materialRepository.findById(item.getMaterial().getId()).orElseThrow();
            if (m.isTela() && item.getTela() != null) {
                telaService.entradaRollo(item.getTela().getId(), item.getCantidad(), orden.getCodigo());
            } else {
                inventarioService.registrar(m, TipoMovimiento.ENTRADA, item.getCantidad(), orden.getCodigo(),
                        "Recepción " + orden.getProveedor().getRazonSocial());
            }
        }
        orden.setEstado(EstadoCompra.RECIBIDA);
        orden.setFechaRecepcion(AppClock.hoy());
        orden.setNueva(false);
        Proveedor p = orden.getProveedor();
        p.setTotalComprado(p.getTotalComprado().add(orden.getTotal()));
        proveedorRepository.save(p);
        return repository.save(orden);
    }

    @Transactional
    public OrdenCompra cancelar(Long id) {
        OrdenCompra orden = orden(id);
        if (orden.getEstado() == EstadoCompra.RECIBIDA) throw new NegocioException("No se puede cancelar una orden ya recibida.");
        orden.setEstado(EstadoCompra.CANCELADA);
        orden.setNueva(false);
        return repository.save(orden);
    }

    @Transactional
    public OrdenCompra registrarPago(Long id) {
        OrdenCompra orden = orden(id);
        orden.setFechaPago(AppClock.hoy());
        return repository.save(orden);
    }

    private OrdenCompra transicion(Long id, EstadoCompra desde, EstadoCompra hacia) {
        OrdenCompra orden = orden(id);
        if (orden.getEstado() != desde) throw new NegocioException("La orden está en estado " + orden.getEstado() + ".");
        orden.setEstado(hacia);
        orden.setNueva(false);
        return repository.save(orden);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
