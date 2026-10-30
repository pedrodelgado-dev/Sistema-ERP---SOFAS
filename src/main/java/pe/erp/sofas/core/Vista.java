/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Vista
 * Descripción: Controlador de modulo: se refresca cada vez que el usuario entra al modulo.
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

/** Controlador de modulo: se refresca cada vez que el usuario entra al modulo. */
public interface Vista {

    /** Se invoca al mostrar el modulo; {@code intent} puede pedir abrir un formulario o un registro. */
    void onShow(Intent intent);

    /** Vistas con recursos que deben pausarse al salir (p. ej. el AnimationTimer del 3D). */
    interface ConCiclo extends Vista {
        void onHide();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
