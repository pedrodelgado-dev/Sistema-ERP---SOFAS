/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Rol
 * Descripción: Roles del sistema; la matriz de permisos por modulo esta en {@link Permiso}.
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

/** Roles del sistema; la matriz de permisos por modulo esta en {@link Permiso}. */
public enum Rol {
    GERENTE("Gerente"),
    ADMINISTRADOR("Administrador"),
    VENDEDOR("Vendedor"),
    SUPERVISOR("Supervisor de taller"),
    CARPINTERO("Carpintero"),
    COSTURERA("Costurera"),
    TAPICERO("Tapicero"),
    ALMACENERO("Almacenero");

    private final String nombre;

    Rol(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    /** Puede autorizar que una OP avance sin ficha aprobada. */
    public boolean autoriza() {
        return this == GERENTE || this == SUPERVISOR || this == ADMINISTRADOR;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
