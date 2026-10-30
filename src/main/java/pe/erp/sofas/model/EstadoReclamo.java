/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: EstadoReclamo
 * Descripción: Archivo fuente Java de la clase EstadoReclamo.
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

public enum EstadoReclamo {
    ABIERTO("Abierto"),
    EN_DIAGNOSTICO("En diagnóstico"),
    EN_REPARACION("En reparación"),
    RESUELTO("Resuelto"),
    RECHAZADO("Rechazado");

    private final String nombre;

    EstadoReclamo(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    public boolean cerrado() {
        return this == RESUELTO || this == RECHAZADO;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
