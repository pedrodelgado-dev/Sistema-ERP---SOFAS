/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Money
 * Descripción: Formato de moneda peruana: {@code S/ 85,450.00}.
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
package pe.erp.sofas.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formato de moneda peruana: {@code S/ 85,450.00}. */
public final class Money {

    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.US);

    private Money() {
    }

    public static String fmt(BigDecimal monto) {
        return "S/ " + df("#,##0.00").format(monto == null ? BigDecimal.ZERO : monto);
    }

    public static String fmt(double monto) {
        return fmt(BigDecimal.valueOf(monto));
    }

    /** Sin decimales: {@code S/ 85,450}. */
    public static String fmt0(BigDecimal monto) {
        return "S/ " + df("#,##0").format(monto == null ? BigDecimal.ZERO : monto.setScale(0, RoundingMode.HALF_UP));
    }

    public static String fmt0(double monto) {
        return fmt0(BigDecimal.valueOf(monto));
    }

    /** Abreviado para ejes de graficos: {@code S/ 85.4k}. */
    public static String corto(double monto) {
        if (Math.abs(monto) >= 1000) return "S/ " + df("#,##0.#").format(monto / 1000) + "k";
        return "S/ " + df("#,##0").format(monto);
    }

    public static String numero(double n) {
        return df("#,##0.##").format(n);
    }

    public static String entero(long n) {
        return df("#,##0").format(n);
    }

    public static BigDecimal of(String valor) {
        return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Interpreta texto escrito por el usuario ("1,250.50", "S/ 300"); devuelve null si no es valido. */
    public static BigDecimal parse(String texto) {
        if (texto == null) return null;
        String limpio = texto.replace("S/", "").replace(",", "").trim();
        if (limpio.isEmpty()) return null;
        try {
            return new BigDecimal(limpio).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static DecimalFormat df(String patron) {
        return new DecimalFormat(patron, SIMBOLOS);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
