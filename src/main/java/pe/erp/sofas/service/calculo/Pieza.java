/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Pieza
 * Descripción: Pieza a cortar: ancho (a lo ancho de la plancha o del rollo) × alto (a lo largo), en cm.
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

import pe.erp.sofas.model.Componente;

/**
 * Pieza a cortar: ancho (a lo ancho de la plancha o del rollo) × alto (a lo largo), en cm.
 * En madera solo cuenta el largo ({@code ancho}).
 */
public record Pieza(String nombre, Componente componente, double ancho, double alto, int cantidad,
                    boolean girable, String origen) {

    public Pieza conCantidad(int n) {
        return new Pieza(nombre, componente, ancho, alto, n, girable, origen);
    }

    public Pieza conOrigen(String o) {
        return new Pieza(nombre, componente, ancho, alto, cantidad, girable, o);
    }

    public Pieza conGiro(boolean g) {
        return new Pieza(nombre, componente, ancho, alto, cantidad, g, origen);
    }

    public double area() {
        return ancho * alto;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
