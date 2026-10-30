/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Despiece
 * Descripción: Despiece de un sofa para la hoja de corte (L largo, F fondo, A alto, n cuerpos). Con chaise, el cuerpo
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

import java.util.*;

/**
 * Despiece de un sofa para la hoja de corte (L largo, F fondo, A alto, n cuerpos). Con chaise, el cuerpo
 * principal tiene F − 65 de fondo y la chaise lleva su propio cojin. Las medidas incluyen 1.5 cm de costura
 * por lado en la tela.
 */
public final class Despiece {

    /** Codigos de material genericos (los de la ficha); la OP puede sustituirlos. */
    public static final String D23 = "MP-019";
    public static final String D18_4 = "MP-018";
    public static final String D18_2 = "MP-017";
    public static final String MADERA = "MP-001";
    public static final String TRIPLAY = "MP-002";
    public static final String TELA = "MP-003";

    private static final double COSTURA = 3;
    private static final double ANCHO_CHAISE = 90;
    private static final double ROLLO = 140;
    private static final double TABLA = 300;

    public record Sofa(double largo, double fondo, double alto, int cuerpos, boolean chaise, boolean puff,
                       boolean girarTela, String origen) {
    }

    private Despiece() {
    }

    /** Piezas por codigo de material generico. */
    public static Map<String, List<Pieza>> piezas(Sofa s) {
        Map<String, List<Pieza>> m = new LinkedHashMap<>();
        if (s.puff()) {
            puff(s, m);
        } else {
            sofa(s, m);
        }
        return m;
    }

    private static void sofa(Sofa s, Map<String, List<Pieza>> m) {
        int n = Math.max(1, s.cuerpos());
        double L = s.largo(), F = s.fondo(), A = s.alto();
        double Fm = s.chaise() ? F - 65 : F;           // fondo del cuerpo principal
        double Lm = s.chaise() ? L - ANCHO_CHAISE : L; // largo del cuerpo principal
        double brazos = s.chaise() ? 16 : 32;          // con chaise queda un solo brazo
        double anchoCojin = (Lm - brazos) / n;
        double fondoCojin = Fm - 22;
        double altoRespaldo = A - 40;
        String o = s.origen();
        boolean g = s.girarTela();

        // ---- Espuma D23 5" (asientos)
        List<Pieza> d23 = lista(m, D23);
        d23.add(new Pieza("Cojín de asiento", Componente.ASIENTO, r(anchoCojin), r(fondoCojin), n, true, o));
        if (s.chaise()) d23.add(new Pieza("Cojín de chaise", Componente.ASIENTO, ANCHO_CHAISE - 16, r(F - 22), 1, true, o));

        // ---- Espuma D18 4" (respaldos)
        lista(m, D18_4).add(new Pieza("Respaldo", Componente.RESPALDO, r(anchoCojin), r(altoRespaldo), n, true, o));

        // ---- Espuma D18 2" (brazos interiores, tapas y frentes)
        List<Pieza> d18 = lista(m, D18_2);
        int nBrazos = s.chaise() ? 1 : 2;
        d18.add(new Pieza("Brazo interior", Componente.BRAZOS, r(Fm - 10), r(A - 35), nBrazos, true, o));
        d18.add(new Pieza("Tapa de brazo", Componente.BRAZOS, r(Fm - 10), 16, nBrazos, true, o));
        d18.add(new Pieza("Frente de asiento", Componente.ASIENTO, Math.min(r(Lm - brazos), 200), 18, 1, true, o));
        if (s.chaise()) d18.add(new Pieza("Frente de chaise", Componente.ASIENTO, ANCHO_CHAISE - 16, 18, 1, true, o));

        // ---- Tela (rollo de 140 cm)
        List<Pieza> tela = lista(m, TELA);
        double espesorAsiento = 12.7 + COSTURA, espesorRespaldo = 10.2 + COSTURA;
        tela.add(new Pieza("Cara de cojín de asiento", Componente.ASIENTO, r(anchoCojin + COSTURA), r(fondoCojin + COSTURA), 2 * n, g, o));
        banda(tela, "Banda de cojín de asiento", Componente.ASIENTO, 2 * (anchoCojin + fondoCojin) + COSTURA, espesorAsiento, n, g, o);
        if (s.chaise()) {
            tela.add(new Pieza("Cara de cojín de chaise", Componente.ASIENTO, ANCHO_CHAISE - 16 + COSTURA, r(F - 22 + COSTURA), 2, g, o));
            banda(tela, "Banda de cojín de chaise", Componente.ASIENTO, 2 * (ANCHO_CHAISE - 16 + F - 22) + COSTURA, espesorAsiento, 1, g, o);
        }
        tela.add(new Pieza("Cara de respaldo", Componente.RESPALDO, r(anchoCojin + COSTURA), r(altoRespaldo + COSTURA), 2 * n, g, o));
        banda(tela, "Banda de respaldo", Componente.RESPALDO, 2 * (anchoCojin + altoRespaldo) + COSTURA, espesorRespaldo, n, g, o);
        tela.add(new Pieza("Brazo interior", Componente.BRAZOS, r(Fm - 10 + COSTURA), r(A - 35 + 16 + COSTURA), nBrazos, g, o));
        tela.add(new Pieza("Brazo exterior", Componente.BRAZOS, r(Fm + COSTURA), r(A - 15 + COSTURA), nBrazos, g, o));
        tela.add(new Pieza("Frente de brazo", Componente.BRAZOS, 16 + COSTURA, r(A - 15 + COSTURA), nBrazos, g, o));
        banda(tela, "Frente de asiento", Componente.TAPIZADO, Lm - brazos + COSTURA, 25 + COSTURA, 1, g, o);
        banda(tela, "Respaldo exterior", Componente.TAPIZADO, L + COSTURA, A - 15 + COSTURA, 1, g, o);
        banda(tela, "Faldón", Componente.TAPIZADO, L + 2 * F + COSTURA, 18 + COSTURA, 1, g, o);

        // ---- Madera (tablas de 300 cm)
        List<Pieza> madera = lista(m, MADERA);
        liston(madera, "Larguero de asiento", Componente.ESTRUCTURA, L - 4, 2, o);
        liston(madera, "Travesaño", Componente.ESTRUCTURA, F - 10, n + 1 + (s.chaise() ? 2 : 0), o);
        madera.add(new Pieza("Parante de respaldo", Componente.ESTRUCTURA, r(A - 15), 0, n + 1, false, o));
        madera.add(new Pieza("Larguero de brazo", Componente.BRAZOS, r(Fm - 8), 0, 2 * nBrazos, false, o));
        liston(madera, "Refuerzo", Componente.ESTRUCTURA, L / 2 - 10, 2, o);
        madera.add(new Pieza("Taco", Componente.ENSAMBLAJE, 12, 0, 8 + (s.chaise() ? 2 : 0), false, o));

        // ---- Triplay 4 mm (planchas de 244 × 122)
        List<Pieza> tri = lista(m, TRIPLAY);
        tablero(tri, "Fondo de asiento", Componente.ASIENTO, Lm - brazos, Fm - 22, o);
        if (s.chaise()) tablero(tri, "Fondo de chaise", Componente.ASIENTO, ANCHO_CHAISE - 16, F - 22, o);
        tablero(tri, "Tapa trasera", Componente.RESPALDO, L - 4, A - 40, o);
        tri.add(new Pieza("Lateral", Componente.BRAZOS, r(Fm - 5), r(A - 20), nBrazos, true, o));
    }

    private static void puff(Sofa s, Map<String, List<Pieza>> m) {
        double L = s.largo(), A = s.alto();
        String o = s.origen();
        boolean g = s.girarTela();
        lista(m, D23).add(new Pieza("Tapa de puff", Componente.ASIENTO, L - 4, L - 4, 1, true, o));
        lista(m, D18_2).add(new Pieza("Lateral de puff", Componente.TAPIZADO, L - 4, A - 12, 4, true, o));
        List<Pieza> tela = lista(m, TELA);
        tela.add(new Pieza("Tapa de puff", Componente.ASIENTO, L + COSTURA, L + COSTURA, 1, g, o));
        tela.add(new Pieza("Lateral de puff", Componente.TAPIZADO, L + COSTURA, A + COSTURA, 4, g, o));
        List<Pieza> mad = lista(m, MADERA);
        mad.add(new Pieza("Marco de puff", Componente.ESTRUCTURA, L - 4, 0, 4, false, o));
        mad.add(new Pieza("Parante de puff", Componente.ESTRUCTURA, A - 14, 0, 4, false, o));
        List<Pieza> tri = lista(m, TRIPLAY);
        tri.add(new Pieza("Base y tapa de puff", Componente.ESTRUCTURA, L - 4, L - 4, 2, true, o));
    }

    /** Bandas: si son mas largas que el rollo y no se pueden girar, se dividen en dos con empalme. */
    private static void banda(List<Pieza> l, String nombre, Componente c, double largo, double ancho, int n,
                              boolean girable, String o) {
        if (largo > ROLLO && !girable) {
            int partes = (int) Math.ceil(largo / (ROLLO - COSTURA));
            l.add(new Pieza(nombre + " (empalme)", c, r(largo / partes + COSTURA), r(ancho), partes * n, false, o));
        } else {
            l.add(new Pieza(nombre, c, r(largo), r(ancho), n, girable, o));
        }
    }

    private static void liston(List<Pieza> l, String nombre, Componente c, double largo, int n, String o) {
        if (largo > TABLA) {
            l.add(new Pieza(nombre + " (empalme)", c, r(largo / 2 + 10), 0, 2 * n, false, o));
        } else {
            l.add(new Pieza(nombre, c, r(largo), 0, n, false, o));
        }
    }

    private static void tablero(List<Pieza> l, String nombre, Componente c, double w, double h, String o) {
        double ww = w, hh = h;
        int partes = 1;
        while (Math.max(ww, hh) > 240 || Math.min(ww, hh) > 120) {
            partes++;
            ww = w / partes;
            hh = h;
        }
        l.add(new Pieza(nombre + (partes > 1 ? " (1/" + partes + ")" : ""), c, r(ww), r(hh), partes, true, o));
    }

    private static List<Pieza> lista(Map<String, List<Pieza>> m, String codigo) {
        return m.computeIfAbsent(codigo, k -> new ArrayList<>());
    }

    private static double r(double v) {
        return Math.round(v * 10) / 10.0;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
