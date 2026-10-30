/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: UnidadCompra
 * Descripción: Redondeo de compra segun la unidad (5.8): madera de 50 en 50 pt, plancha 1, metro de 10 en 10,
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

/**
 * Redondeo de compra segun la unidad (5.8): madera de 50 en 50 pt, plancha 1, metro de 10 en 10,
 * unidad de 10 en 10, caja 1, galon 1, kg de 5 en 5.
 */
public final class UnidadCompra {

    private UnidadCompra() {
    }

    public static double paso(String unidad) {
        if (unidad == null) return 1;
        return switch (unidad) {
            case "pt" -> 50;
            case "m" -> 10;
            case "unidad" -> 10;
            case "kg" -> 5;
            default -> 1; // plancha, caja, galón, juego, par
        };
    }

    public static double redondear(String unidad, double cantidad) {
        if (cantidad <= 0) return 0;
        double paso = paso(unidad);
        return Math.ceil(cantidad / paso - 1e-9) * paso;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
