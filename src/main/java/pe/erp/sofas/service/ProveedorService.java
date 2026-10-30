/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ProveedorService
 * Descripción: Archivo fuente Java de la clase ProveedorService.
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
import pe.erp.sofas.model.Material;
import pe.erp.sofas.model.OrdenCompra;
import pe.erp.sofas.model.Proveedor;
import pe.erp.sofas.repository.MaterialRepository;
import pe.erp.sofas.repository.OrdenCompraRepository;
import pe.erp.sofas.repository.ProveedorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProveedorService {

    private final ProveedorRepository repository;
    private final MaterialRepository materialRepository;
    private final OrdenCompraRepository compraRepository;

    public List<Proveedor> listar() {
        return repository.findAllByOrderByCodigoAsc();
    }

    public List<Proveedor> activos() {
        return listar().stream().filter(Proveedor::getActivo).toList();
    }

    public Proveedor proveedor(Long id) {
        return repository.findById(id).orElseThrow(() -> new NegocioException("El proveedor ya no existe."));
    }

    public List<Material> materiales(Proveedor p) {
        return materialRepository.findAllByOrderByCodigoAsc().stream()
                .filter(m -> m.getProveedor() != null && m.getProveedor().getId().equals(p.getId())).toList();
    }

    public List<OrdenCompra> compras(Proveedor p) {
        return compraRepository.findAllByOrderByCodigoDesc().stream().filter(o -> o.getProveedor().getId().equals(p.getId())).toList();
    }

    @Transactional
    public Proveedor guardar(Proveedor p) {
        if (p.getRazonSocial() == null || p.getRazonSocial().isBlank()) throw new NegocioException("La razón social es obligatoria.");
        if (p.getRuc() == null || !p.getRuc().matches("\\d{11}")) throw new NegocioException("El RUC debe tener 11 dígitos.");
        boolean duplicado = listar().stream().anyMatch(x -> x.getRuc() != null && x.getRuc().equals(p.getRuc()) && !x.getId().equals(p.getId()));
        if (duplicado) throw new NegocioException("Ya existe un proveedor con ese RUC.");
        if (p.getId() == null) p.setCodigo(Codigos.siguiente(repository.findAll(), Proveedor::getCodigo, "PR-", 2));
        return repository.save(p);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
