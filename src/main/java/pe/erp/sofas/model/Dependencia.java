/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Dependencia
 * Descripción: Formula de escalado de una linea de la ficha segun las medidas pedidas (L largo, F fondo, A alto,
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

/**
 * Formula de escalado de una linea de la ficha segun las medidas pedidas (L largo, F fondo, A alto,
 * n cuerpos) frente a las medidas base de la ficha (L0, F0, A0, n0). La expresion se evalua con exp4j.
 */
public enum Dependencia {
    F("Fija", "1"),
    L("× largo", "L / L0"),
    LF("× largo·fondo", "(L * F) / (L0 * F0)"),
    LA("× largo·alto", "(L * A) / (L0 * A0)"),
    T("× superficie tapizada", "(L * (F + A)) / (L0 * (F0 + A0))"),
    N("× cuerpos", "n / n0"),
    P("Patas 4/6 según largo", "patas(L) / patas(L0)");

    private final String nombre;
    private final String expresion;

    Dependencia(String nombre, String expresion) {
        this.nombre = nombre;
        this.expresion = expresion;
    }

    public String nombre() {
        return nombre;
    }

    public String expresion() {
        return expresion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
