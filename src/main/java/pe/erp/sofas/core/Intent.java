/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Intent
 * Descripción: "Intencion" de navegacion: ademas de abrir un modulo, le pide que haga algo al mostrarse
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
package pe.erp.sofas.core;

/**
 * "Intencion" de navegacion: ademas de abrir un modulo, le pide que haga algo al mostrarse
 * (p. ej. abrir el formulario de nuevo registro o el detalle de un codigo).
 */
public record Intent(String accion, Object dato) {

    public static final String ACCION_NUEVO = "NEW";
    public static final String ACCION_ABRIR = "OPEN";
    public static final String ACCION_PRESELECCIONAR = "PRESELECT";

    public static final Intent NONE = new Intent(null, null);
    public static final Intent NEW = new Intent(ACCION_NUEVO, null);

    public static Intent abrir(Object dato) {
        return new Intent(ACCION_ABRIR, dato);
    }

    public static Intent preseleccionar(Object dato) {
        return new Intent(ACCION_PRESELECCIONAR, dato);
    }

    public boolean es(String accion) {
        return accion.equals(this.accion);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
