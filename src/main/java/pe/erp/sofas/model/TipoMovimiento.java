/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: TipoMovimiento
 * Descripción: Tipos de movimiento del kardex. Reserva, Liberacion y Retazo no alteran el saldo fisico.
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

/** Tipos de movimiento del kardex. Reserva, Liberacion y Retazo no alteran el saldo fisico. */
public enum TipoMovimiento {
    ENTRADA("Entrada", "MOVE_TO_INBOX", "green", 1),
    SALIDA("Salida", "UNARCHIVE", "amber", -1),
    AJUSTE("Ajuste", "TUNE", "blue", 0),
    RESERVA("Reserva", "LOCK", "blue", 0),
    LIBERACION("Liberación", "LOCK_OPEN", "gray", 0),
    CONSUMO_OP("Consumo OP", "BUILD", "red", -1),
    RETAZO("Retazo", "CONTENT_CUT", "wood", 0);

    private final String nombre;
    private final String icono;
    private final String variante;
    /** +1 suma, -1 resta, 0 no cambia el saldo (el ajuste usa el signo de la cantidad). */
    private final int signo;

    TipoMovimiento(String nombre, String icono, String variante, int signo) {
        this.nombre = nombre;
        this.icono = icono;
        this.variante = variante;
        this.signo = signo;
    }

    public String nombre() {
        return nombre;
    }

    public String icono() {
        return icono;
    }

    public String variante() {
        return variante;
    }

    public int signo() {
        return signo;
    }

    /** true si el movimiento cambia el stock fisico. */
    public boolean afectaSaldo() {
        return this == ENTRADA || this == SALIDA || this == AJUSTE || this == CONSUMO_OP;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
