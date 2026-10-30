/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CatalogoService
 * Descripción: Implementacion del catalogo de la explosion sobre los repositorios.
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
import pe.erp.sofas.model.EstadoFicha;
import pe.erp.sofas.model.FichaTecnica;
import pe.erp.sofas.model.Material;
import pe.erp.sofas.repository.FichaTecnicaRepository;
import pe.erp.sofas.repository.MaterialRepository;
import pe.erp.sofas.service.calculo.Catalogo;

/** Implementacion del catalogo de la explosion sobre los repositorios. */
@Service
@RequiredArgsConstructor
public class CatalogoService implements Catalogo {

    private final FichaTecnicaRepository fichaRepository;
    private final MaterialRepository materialRepository;

    @Override
    public FichaTecnica fichaAprobada(String codigo) {
        return fichaRepository.findByCodigoOrderByVersionDesc(codigo).stream()
                .filter(f -> f.getEstado() == EstadoFicha.APROBADA).findFirst().orElse(null);
    }

    @Override
    public Material material(String codigo) {
        return materialRepository.findByCodigo(codigo).orElse(null);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
