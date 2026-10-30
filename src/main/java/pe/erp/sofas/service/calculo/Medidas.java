/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Medidas
 * Descripción: Largo, fondo y alto en cm y numero de cuerpos.
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

import pe.erp.sofas.model.FichaTecnica;

/** Largo, fondo y alto en cm y numero de cuerpos. */
public record Medidas(double largo, double fondo, double alto, int cuerpos) {

    public static Medidas de(FichaTecnica f) {
        return new Medidas(nz(f.getLargo()), nz(f.getFondo()), nz(f.getAlto()), f.getCuerpos() == null ? 1 : f.getCuerpos());
    }

    private static double nz(Integer v) {
        return v == null ? 0 : v;
    }

    public boolean igual(Medidas o) {
        return o != null && Math.abs(largo - o.largo) < 0.01 && Math.abs(fondo - o.fondo) < 0.01
                && Math.abs(alto - o.alto) < 0.01 && cuerpos == o.cuerpos;
    }

    public String texto() {
        return Math.round(largo) + " × " + Math.round(fondo) + " × " + Math.round(alto) + " cm";
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
