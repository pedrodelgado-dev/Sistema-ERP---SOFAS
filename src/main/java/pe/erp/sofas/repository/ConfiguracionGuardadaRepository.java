/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConfiguracionGuardadaRepository
 * Descripción: Archivo fuente Java de la clase ConfiguracionGuardadaRepository.
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
package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.ConfiguracionGuardada;
import pe.erp.sofas.model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionGuardadaRepository extends JpaRepository<ConfiguracionGuardada, Long> {

    List<ConfiguracionGuardada> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

    List<ConfiguracionGuardada> findByUsuarioAndEsFavoritoTrueOrderByFechaCreacionDesc(Usuario usuario);

    Optional<ConfiguracionGuardada> findByIdAndUsuario(Long id, Usuario usuario);

    List<ConfiguracionGuardada> findByUsuarioAndEtiquetaOrderByFechaCreacionDesc(Usuario usuario, String etiqueta);

    long countByUsuario(Usuario usuario);
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
