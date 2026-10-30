/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Tabla
 * Descripción: Tabla generica para reportes y exportaciones (PDF y Excel). {@code derecha[i]} alinea la columna a la derecha.
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
package pe.erp.sofas.service;

import java.util.ArrayList;
import java.util.List;

/** Tabla generica para reportes y exportaciones (PDF y Excel). {@code derecha[i]} alinea la columna a la derecha. */
public record Tabla(String titulo, String subtitulo, List<String> columnas, boolean[] derecha, List<List<String>> filas,
                    List<String> totales) {

    public static Tabla de(String titulo, String subtitulo, List<String> columnas, boolean[] derecha) {
        return new Tabla(titulo, subtitulo, columnas, derecha, new ArrayList<>(), null);
    }

    public Tabla fila(Object... valores) {
        List<String> f = new ArrayList<>();
        for (Object v : valores) f.add(v == null ? "—" : v.toString());
        filas.add(f);
        return this;
    }

    public Tabla conTotales(String... valores) {
        return new Tabla(titulo, subtitulo, columnas, derecha, filas, List.of(valores));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
