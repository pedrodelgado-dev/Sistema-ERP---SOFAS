/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConfiguracionService
 * Descripción: Empresa, usuarios, roles y matriz de permisos.
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
import pe.erp.sofas.repository.EmpresaRepository;
import pe.erp.sofas.repository.PermisoRepository;
import pe.erp.sofas.repository.UsuarioRepository;

import java.util.List;

/** Empresa, usuarios, roles y matriz de permisos. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;

    public Empresa empresa() {
        return empresaRepository.findById(1L).orElseGet(() -> Empresa.builder().id(1L).razonSocial("SISTEMA ERP SOFAS S.A.C.").build());
    }

    @Transactional
    public Empresa guardarEmpresa(Empresa e) {
        if (e.getRazonSocial() == null || e.getRazonSocial().isBlank()) throw new NegocioException("La razón social es obligatoria.");
        if (e.getRuc() == null || !e.getRuc().matches("\\d{11}")) throw new NegocioException("El RUC debe tener 11 dígitos.");
        e.setId(1L);
        return empresaRepository.save(e);
    }

    public List<Usuario> usuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario guardarUsuario(Usuario u, String password) {
        if (u.getUsername() == null || !u.getUsername().matches("[a-zA-Z0-9._]{3,30}")) throw new NegocioException("Usuario de 3 a 30 caracteres, sin espacios.");
        if (u.getNombre() == null || u.getNombre().isBlank()) throw new NegocioException("El nombre es obligatorio.");
        boolean duplicado = usuarios().stream().anyMatch(x -> x.getUsername().equalsIgnoreCase(u.getUsername()) && !x.getId().equals(u.getId()));
        if (duplicado) throw new NegocioException("El usuario " + u.getUsername() + " ya existe.");
        if (u.getId() == null && (password == null || password.length() < 6)) throw new NegocioException("La contraseña debe tener al menos 6 caracteres.");
        if (password != null && !password.isEmpty()) u.setPasswordHash(AuthService.hash(password));
        return usuarioRepository.save(u);
    }

    public List<Permiso> permisos(Rol rol) {
        return permisoRepository.findByRol(rol);
    }

    @Transactional
    public Permiso fijarPermiso(Rol rol, String modulo, boolean ver, boolean editar) {
        Permiso p = permisoRepository.findByRolAndModulo(rol, modulo).orElseGet(() -> Permiso.builder().rol(rol).modulo(modulo).build());
        p.setVer(ver || editar);
        p.setEditar(editar);
        return permisoRepository.save(p);
    }

    public boolean puedeVer(Rol rol, String modulo) {
        if (rol == Rol.GERENTE) return true;
        return permisoRepository.findByRolAndModulo(rol, modulo).map(Permiso::getVer).orElse(false);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
