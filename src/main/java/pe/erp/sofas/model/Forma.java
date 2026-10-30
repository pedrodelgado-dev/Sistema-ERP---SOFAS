/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Forma
 * Descripción: Forma del modelo de sofa: decide como se dibuja en 3D y que despiece usa la hoja de corte.
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
package pe.erp.sofas.model;

/** Forma del modelo de sofa: decide como se dibuja en 3D y que despiece usa la hoja de corte. */
public enum Forma {
    MODERNO("Sofá Moderno"),
    SECCIONAL_L("Seccional L"),
    SILLON("Sillón"),
    JUEGO("Juego de sala"),
    CHESTERFIELD("Chesterfield"),
    SOFA_CAMA("Sofá cama"),
    RECLINABLE("Reclinable"),
    ESCANDINAVO("Escandinavo"),
    PUFF("Puff"),
    SECCIONAL_U("Seccional U");

    private final String nombre;

    Forma(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
