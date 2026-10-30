/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Catalogo
 * Descripción: Lo que la explosion necesita consultar: fichas aprobadas (para las compuestas) y materiales.
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
package pe.erp.sofas.service.calculo;

import pe.erp.sofas.model.FichaTecnica;
import pe.erp.sofas.model.Material;

/** Lo que la explosion necesita consultar: fichas aprobadas (para las compuestas) y materiales. */
public interface Catalogo {

    /** Version aprobada de la ficha con ese codigo, o null. */
    FichaTecnica fichaAprobada(String codigo);

    Material material(String codigo);
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
