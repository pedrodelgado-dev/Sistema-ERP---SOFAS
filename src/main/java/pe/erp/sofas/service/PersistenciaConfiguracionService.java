/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PersistenciaConfiguracionService
 * Descripción: Archivo fuente Java de la clase PersistenciaConfiguracionService.
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
import pe.erp.sofas.model.ConfigSofa;
import pe.erp.sofas.model.ConfiguracionGuardada;
import pe.erp.sofas.model.Usuario;
import pe.erp.sofas.repository.ConfiguracionGuardadaRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersistenciaConfiguracionService {

    private final ConfiguracionGuardadaRepository repo;

    public ConfiguracionGuardada guardar(Usuario usuario, ConfigSofa config, String nombre, String descripcion) {
        ConfiguracionGuardada guardada = ConfiguracionGuardada.fromConfigSofa(config, usuario, nombre, descripcion);
        return repo.save(guardada);
    }

    public ConfigSofa cargar(Long id, Usuario usuario) {
        ConfiguracionGuardada guardada = repo.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        return guardada.toConfigSofa();
    }

    public Optional<ConfiguracionGuardada> obtener(Long id, Usuario usuario) {
        return repo.findByIdAndUsuario(id, usuario);
    }

    public List<ConfiguracionGuardada> listarPorUsuario(Usuario usuario) {
        return repo.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    public List<ConfiguracionGuardada> listarFavoritos(Usuario usuario) {
        return repo.findByUsuarioAndEsFavoritoTrueOrderByFechaCreacionDesc(usuario);
    }

    public List<ConfiguracionGuardada> listarPorEtiqueta(Usuario usuario, String etiqueta) {
        return repo.findByUsuarioAndEtiquetaOrderByFechaCreacionDesc(usuario, etiqueta);
    }

    public ConfiguracionGuardada marcarFavorito(Long id, Usuario usuario, boolean esFavorito) {
        ConfiguracionGuardada guardada = repo.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        guardada.setEsFavorito(esFavorito);
        return repo.save(guardada);
    }

    public ConfiguracionGuardada actualizar(Long id, Usuario usuario, String nombre, String descripcion, String etiqueta) {
        ConfiguracionGuardada guardada = repo.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        guardada.setNombre(nombre);
        guardada.setDescripcion(descripcion);
        guardada.setEtiqueta(etiqueta);
        return repo.save(guardada);
    }

    public void eliminar(Long id, Usuario usuario) {
        ConfiguracionGuardada guardada = repo.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        repo.delete(guardada);
    }

    public long contarPorUsuario(Usuario usuario) {
        return repo.countByUsuario(usuario);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
