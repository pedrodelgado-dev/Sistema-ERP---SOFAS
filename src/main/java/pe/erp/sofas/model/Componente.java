/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Componente
 * Descripción: Componente del sofa al que pertenece una linea de la lista de materiales.
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

/** Componente del sofa al que pertenece una linea de la lista de materiales. */
public enum Componente {
    ESTRUCTURA("Estructura", "#A47148"),
    ASIENTO("Asiento", "#0EA5E9"),
    RESPALDO("Respaldo", "#6366F1"),
    BRAZOS("Brazos", "#8B5CF6"),
    TAPIZADO("Tapizado", "#EC4899"),
    COJINES("Cojines", "#F59E0B"),
    ENSAMBLAJE("Ensamblaje", "#64748B"),
    PATAS("Patas", "#C69A6D"),
    ACABADO("Acabado", "#14B8A6"),
    EMBALAJE("Embalaje", "#94A3B8"),
    MECANISMO("Mecanismo", "#2563EB");

    private final String nombre;
    private final String color;

    Componente(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public String nombre() {
        return nombre;
    }

    public String color() {
        return color;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
