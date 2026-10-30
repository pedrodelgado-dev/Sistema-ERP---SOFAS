/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Codigos
 * Descripción: Genera el siguiente codigo correlativo (SF-012, PE-0250, OP-1031, OC-0324...).
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

import java.util.Collection;
import java.util.function.Function;

/** Genera el siguiente codigo correlativo (SF-012, PE-0250, OP-1031, OC-0324...). */
public final class Codigos {

    private Codigos() {
    }

    public static <T> String siguiente(Collection<T> existentes, Function<T, String> codigo, String prefijo, int digitos) {
        int max = existentes.stream()
                .map(codigo)
                .filter(c -> c != null && c.startsWith(prefijo))
                .map(c -> c.substring(prefijo.length()).replaceAll("\\D", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        return prefijo + String.format("%0" + digitos + "d", max + 1);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
