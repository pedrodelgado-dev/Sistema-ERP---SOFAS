/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: EstadoPedido
 * Descripción: Registrado → Cotizado → Confirmado → En produccion → Listo para entrega → Entregado (o Cancelado).
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

/** Registrado → Cotizado → Confirmado → En produccion → Listo para entrega → Entregado (o Cancelado). */
public enum EstadoPedido {
    REGISTRADO("Registrado", "EDIT"),
    COTIZADO("Cotizado", "REQUEST_QUOTE"),
    CONFIRMADO("Confirmado", "VERIFIED"),
    EN_PRODUCCION("En producción", "BUILD"),
    LISTO("Listo para entrega", "ARCHIVE"),
    ENTREGADO("Entregado", "LOCAL_SHIPPING"),
    CANCELADO("Cancelado", "CANCEL");

    private final String nombre;
    private final String icono;

    EstadoPedido(String nombre, String icono) {
        this.nombre = nombre;
        this.icono = icono;
    }

    public String nombre() {
        return nombre;
    }

    public String icono() {
        return icono;
    }

    public boolean cerrado() {
        return this == ENTREGADO || this == CANCELADO;
    }

    public EstadoPedido siguiente() {
        return switch (this) {
            case REGISTRADO -> COTIZADO;
            case COTIZADO -> CONFIRMADO;
            case CONFIRMADO -> EN_PRODUCCION;
            case EN_PRODUCCION -> LISTO;
            case LISTO, ENTREGADO -> ENTREGADO;
            case CANCELADO -> CANCELADO;
        };
    }

    /** Estados de la linea de tiempo (sin Cancelado). */
    public static EstadoPedido[] flujo() {
        return new EstadoPedido[]{REGISTRADO, COTIZADO, CONFIRMADO, EN_PRODUCCION, LISTO, ENTREGADO};
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
