/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: EtapaMO
 * Descripción: Etapas de mano de obra de una ficha tecnica con su costo por hora por defecto (S/).
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

/** Etapas de mano de obra de una ficha tecnica con su costo por hora por defecto (S/). */
public enum EtapaMO {
    CORTE("Corte", 8),
    CARPINTERIA("Carpintería", 8),
    COSTURA("Costura", 8),
    TAPIZADO("Tapizado", 9),
    ACABADO("Acabado", 7),
    CONTROL_CALIDAD("Control de calidad", 7);

    private final String nombre;
    private final double costoHora;

    EtapaMO(String nombre, double costoHora) {
        this.nombre = nombre;
        this.costoHora = costoHora;
    }

    public String nombre() {
        return nombre;
    }

    public double costoHora() {
        return costoHora;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
