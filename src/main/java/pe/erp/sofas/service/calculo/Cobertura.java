/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Cobertura
 * Descripción: Regla de garantia: pata floja, estructura quebrada y mecanismo trabado tienen 12 meses (estructura);
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

import java.time.LocalDate;
import java.util.Set;

/**
 * Regla de garantia: pata floja, estructura quebrada y mecanismo trabado tienen 12 meses (estructura);
 * el resto 6 meses (tapiz y espuma).
 */
public final class Cobertura {

    private static final Set<String> ESTRUCTURA = Set.of("Pata floja", "Estructura quebrada", "Mecanismo trabado");

    public record Resultado(boolean cubierto, int meses, LocalDate vence, String tipo) {
        public String texto() {
            return cubierto ? "En garantía hasta " + pe.erp.sofas.core.AppClock.fmt(vence) : "Fuera de garantía";
        }
    }

    private Cobertura() {
    }

    public static int meses(String falla) {
        return ESTRUCTURA.contains(falla) ? 12 : 6;
    }

    public static Resultado evaluar(String falla, LocalDate fechaCompra, LocalDate fechaReclamo) {
        int meses = meses(falla);
        if (fechaCompra == null) return new Resultado(false, meses, null, tipo(meses));
        LocalDate vence = fechaCompra.plusMonths(meses);
        LocalDate ref = fechaReclamo == null ? pe.erp.sofas.core.AppClock.hoy() : fechaReclamo;
        return new Resultado(!ref.isAfter(vence), meses, vence, tipo(meses));
    }

    private static String tipo(int meses) {
        return meses == 12 ? "Estructura · 12 meses" : "Tapiz y espuma · 6 meses";
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
