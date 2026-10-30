/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Etapa
 * Descripción: Las 11 etapas del taller de sofas, en orden. El avance de una orden es indice / 10.
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

/** Las 11 etapas del taller de sofas, en orden. El avance de una orden es indice / 10. */
public enum Etapa {
    PENDIENTE("Pendiente", "#94A3B8", "SCHEDULE"),
    CORTE_MADERA("Corte de madera", "#C69A6D", "CARPENTER"),
    ARMADO("Armado de estructura", "#A47148", "HANDYMAN"),
    CINCHADO("Cinchado / resortes", "#8B5CF6", "GRID_ON"),
    ESPUMADO("Espumado", "#0EA5E9", "LAYERS"),
    CORTE_TELA("Corte de tela", "#EC4899", "CONTENT_CUT"),
    COSTURA("Costura", "#F59E0B", "GESTURE"),
    TAPIZADO("Tapizado", "#2563EB", "WEEKEND"),
    ACABADO("Acabado y patas", "#14B8A6", "BRUSH"),
    CONTROL_CALIDAD("Control de calidad", "#64748B", "FACT_CHECK"),
    LISTO("Listo", "#16A34A", "CHECK_CIRCLE");

    private final String nombre;
    private final String color;
    private final String icono;

    Etapa(String nombre, String color, String icono) {
        this.nombre = nombre;
        this.color = color;
        this.icono = icono;
    }

    public String nombre() {
        return nombre;
    }

    public String color() {
        return color;
    }

    public String icono() {
        return icono;
    }

    /** 0..10 */
    public int paso() {
        return ordinal();
    }

    public double avance() {
        return ordinal() / 10.0;
    }

    public boolean productiva() {
        return this != PENDIENTE && this != LISTO;
    }

    public Etapa siguiente() {
        return this == LISTO ? LISTO : values()[ordinal() + 1];
    }

    public Etapa anterior() {
        return this == PENDIENTE ? PENDIENTE : values()[ordinal() - 1];
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
