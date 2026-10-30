/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Igv
 * Descripción: Los precios de venta incluyen IGV (18 %): subtotal = total / 1.18.
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

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Los precios de venta incluyen IGV (18 %): subtotal = total / 1.18. */
public final class Igv {

    public static final BigDecimal TASA = new BigDecimal("0.18");
    private static final BigDecimal FACTOR = BigDecimal.ONE.add(TASA);

    private Igv() {
    }

    public static BigDecimal subtotal(BigDecimal total) {
        return total.divide(FACTOR, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal igv(BigDecimal total) {
        return total.setScale(2, RoundingMode.HALF_UP).subtract(subtotal(total));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
