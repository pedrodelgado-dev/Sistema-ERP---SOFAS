/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: EstadoRetapizado
 * Descripción: Por cotizar → Cotizado → Aprobado → Recojo programado → En taller → Listo → Entregado.
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

/** Por cotizar → Cotizado → Aprobado → Recojo programado → En taller → Listo → Entregado. */
public enum EstadoRetapizado {
    POR_COTIZAR("Por cotizar", "Cotizar"),
    COTIZADO("Cotizado", "Aprobar"),
    APROBADO("Aprobado", "Programar recojo"),
    RECOJO_PROGRAMADO("Recojo programado", "Ingresar a taller"),
    EN_TALLER("En taller", "Marcar listo"),
    LISTO("Listo", "Entregar"),
    ENTREGADO("Entregado", null);

    private final String nombre;
    private final String accion;

    EstadoRetapizado(String nombre, String accion) {
        this.nombre = nombre;
        this.accion = accion;
    }

    public String nombre() {
        return nombre;
    }

    /** Texto del boton que lleva al siguiente estado. */
    public String accion() {
        return accion;
    }

    public EstadoRetapizado siguiente() {
        return this == ENTREGADO ? ENTREGADO : values()[ordinal() + 1];
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
