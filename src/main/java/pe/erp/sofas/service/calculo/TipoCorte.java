/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: TipoCorte
 * Descripción: Tipo de plano de corte y su formato de material.
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
package pe.erp.sofas.service.calculo;

/** Tipo de plano de corte y su formato de material. */
public enum TipoCorte {
    ESPUMA("Espuma", 200, 100, "plancha 200 × 100 cm"),
    TELA("Tela", 140, 0, "rollo de 140 cm de ancho"),
    MADERA("Madera", 300, 0, "tabla de 300 cm · kerf 5 mm"),
    TABLERO("Tablero", 244, 122, "plancha de triplay 244 × 122 cm");

    private final String nombre;
    private final double ancho;
    private final double alto;
    private final String formato;

    TipoCorte(String nombre, double ancho, double alto, String formato) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.alto = alto;
        this.formato = formato;
    }

    public String nombre() {
        return nombre;
    }

    public double ancho() {
        return ancho;
    }

    public double alto() {
        return alto;
    }

    public String formato() {
        return formato;
    }

    public static TipoCorte paraCategoria(String categoriaMaterial, String codigo) {
        if (pe.erp.sofas.model.TipoTela.porMaterial(codigo) != null) return TELA;
        return switch (categoriaMaterial) {
            case "Espuma" -> ESPUMA;
            case "Madera" -> MADERA;
            case "Tableros" -> TABLERO;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
