/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Corte
 * Descripción: Optimizador de cortes.
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

import java.util.*;

/**
 * Optimizador de cortes.
 * <ul>
 *   <li>2D (espuma, tablero, tela): guillotina con First Fit Decreasing. Prueba dos ordenes (por alto y por
 *       area) y se queda con el que usa menos planchas o menos metros.</li>
 *   <li>1D (madera): FFD con 5 mm de kerf en tablas de 300 cm.</li>
 * </ul>
 */
public final class Corte {

    public static final double KERF = 0.5;
    /** Sobrante minimo (cm) en ambos lados para considerarlo reutilizable. */
    public static final double MIN_RETAZO = 30;

    public enum Estrategia { MEJOR_LADO, MEJOR_AREA;

        public Estrategia otra() {
            return this == MEJOR_LADO ? MEJOR_AREA : MEJOR_LADO;
        }

        public String nombre() {
            return this == MEJOR_LADO ? "Mejor ajuste de lado" : "Mejor ajuste de área";
        }
    }

    public record Rect(double x, double y, double w, double h) {
        double area() {
            return w * h;
        }
    }

    /** Pieza colocada: numero correlativo, lamina (0..), posicion y si se giro. */
    public record Colocacion(int numero, Pieza pieza, int lamina, double x, double y, double w, double h, boolean girada) {
    }

    public record Resultado(TipoCorte tipo, double ancho, double alto, int laminas, List<Colocacion> colocaciones,
                            List<List<Rect>> sobrantes, double areaPiezas, double largoUsado, String orden,
                            Estrategia estrategia) {

        /** Area de material usada (planchas completas o metros de rollo). */
        public double areaMaterial() {
            if (tipo == TipoCorte.TELA) return ancho * largoUsado;
            return laminas * ancho * alto;
        }

        public double aprovechamiento() {
            double a = areaMaterial();
            return a == 0 ? 0 : areaPiezas / a * 100;
        }

        public double aprovechamientoLamina(int i) {
            double usado = colocaciones.stream().filter(c -> c.lamina() == i).mapToDouble(c -> c.w() * c.h()).sum();
            return usado / (ancho * alto) * 100;
        }

        /** Sobrante reutilizable en m² (rectangulos libres con ambos lados ≥ 30 cm). */
        public double sobranteReutilizable() {
            return sobrantes.stream().flatMap(List::stream)
                    .filter(r -> r.w() >= MIN_RETAZO && r.h() >= MIN_RETAZO)
                    .mapToDouble(Rect::area).sum() / 10000.0;
        }

        /** Metros de rollo para tela. */
        public double metros() {
            return largoUsado / 100.0;
        }
    }

    /** Tabla de madera con sus cortes y el sobrante en cm. */
    public record Tabla(int indice, List<Colocacion> cortes, double sobrante) {
    }

    public record Resultado1D(double largoTabla, List<Tabla> tablas, double largoPiezas) {
        public double aprovechamiento() {
            double total = tablas.size() * largoTabla;
            return total == 0 ? 0 : largoPiezas / total * 100;
        }

        public double sobranteReutilizable() {
            return tablas.stream().mapToDouble(Tabla::sobrante).filter(s -> s >= 40).sum();
        }
    }

    private Corte() {
    }

    // ================================================================ 2D

    public static Resultado optimizar(TipoCorte tipo, List<Pieza> piezas, Estrategia estrategia) {
        Comparator<Pieza> alto = Comparator.comparingDouble((Pieza p) -> Math.max(p.alto(), p.girable() ? p.ancho() : 0))
                .thenComparingDouble(Pieza::area).reversed();
        Comparator<Pieza> area = Comparator.comparingDouble(Pieza::area).reversed();
        Resultado mejor = null;
        // Dos ordenes (por alto y por area), con y sin giro de las piezas que lo permiten.
        for (boolean giro : new boolean[]{true, false}) {
            List<Pieza> lista = giro ? piezas : piezas.stream().map(p -> p.conGiro(false)).toList();
            String sufijo = giro ? "" : " sin giro";
            Resultado a = empacar(tipo, lista, alto, "por alto" + sufijo, estrategia);
            Resultado b = empacar(tipo, lista, area, "por área" + sufijo, estrategia);
            Resultado m = mejor(tipo, a, b);
            mejor = mejor == null ? m : mejor(tipo, mejor, m);
        }
        return mejor;
    }

    private static Resultado mejor(TipoCorte tipo, Resultado a, Resultado b) {
        if (tipo == TipoCorte.TELA) {
            return b.largoUsado() < a.largoUsado() - 0.01 ? b : a;
        }
        if (b.laminas() != a.laminas()) return b.laminas() < a.laminas() ? b : a;
        return b.sobranteReutilizable() > a.sobranteReutilizable() ? b : a;
    }

    private static Resultado empacar(TipoCorte tipo, List<Pieza> piezas, Comparator<Pieza> orden, String nombreOrden,
                                     Estrategia estrategia) {
        double W = tipo.ancho();
        boolean rollo = tipo == TipoCorte.TELA;
        double H = rollo ? 100_000 : tipo.alto();
        List<Pieza> unidades = new ArrayList<>();
        for (Pieza p : piezas) for (int i = 0; i < p.cantidad(); i++) unidades.add(p.conCantidad(1));
        unidades.sort(orden);

        List<List<Rect>> libres = new ArrayList<>();
        List<Colocacion> col = new ArrayList<>();
        Map<Pieza, Integer> numeros = new LinkedHashMap<>();
        int numero = 0;
        double area = 0;
        for (Pieza p : unidades) {
            numero++;
            area += p.ancho() * p.alto();
            Colocacion c = null;
            for (int s = 0; s < libres.size() && c == null; s++) c = colocar(libres.get(s), s, p, numero, estrategia, rollo);
            if (c == null) {
                List<Rect> nueva = new ArrayList<>(List.of(new Rect(0, 0, W, H)));
                libres.add(nueva);
                c = colocar(nueva, libres.size() - 1, p, numero, estrategia, rollo);
                if (c == null) { // pieza mas grande que la lamina: se coloca recortada a la lamina
                    c = new Colocacion(numero, p, libres.size() - 1, 0, 0, Math.min(p.ancho(), W), Math.min(p.alto(), H), false);
                    nueva.clear();
                }
            }
            col.add(c);
        }
        double largo = rollo ? col.stream().mapToDouble(c -> c.y() + c.h()).max().orElse(0) : 0;
        if (rollo) {
            // Los rectangulos libres del rollo se recortan al largo realmente usado.
            for (List<Rect> l : libres) {
                l.replaceAll(r -> new Rect(r.x(), r.y(), r.w(), Math.max(0, Math.min(r.y() + r.h(), largo) - r.y())));
                l.removeIf(r -> r.h() <= 0.01);
            }
        }
        renumerar(col);
        return new Resultado(tipo, W, rollo ? largo : H, Math.max(rollo ? 1 : 0, libres.size()), col, libres, area, largo, nombreOrden, estrategia);
    }

    /** Numera las piezas por su posicion en el plano para que las piezas iguales queden en rangos "1–6". */
    private static void renumerar(List<Colocacion> col) {
        col.sort(Comparator.comparing((Colocacion c) -> c.pieza().origen() == null ? "" : c.pieza().origen())
                .thenComparing(c -> c.pieza().nombre())
                .thenComparingInt(Colocacion::lamina).thenComparingDouble(Colocacion::y).thenComparingDouble(Colocacion::x));
        for (int i = 0; i < col.size(); i++) {
            Colocacion c = col.get(i);
            col.set(i, new Colocacion(i + 1, c.pieza(), c.lamina(), c.x(), c.y(), c.w(), c.h(), c.girada()));
        }
    }

    private static Colocacion colocar(List<Rect> libres, int lamina, Pieza p, int numero, Estrategia e, boolean rollo) {
        Rect mejor = null;
        boolean mejorGiro = false;
        double puntaje = Double.MAX_VALUE, desempate = Double.MAX_VALUE;
        for (Rect r : libres) {
            for (int g = 0; g < (p.girable() ? 2 : 1); g++) {
                double w = g == 0 ? p.ancho() : p.alto();
                double h = g == 0 ? p.alto() : p.ancho();
                if (w > r.w() + 1e-6 || h > r.h() + 1e-6) continue;
                double s1, s2;
                if (rollo) {
                    // En rollo se prioriza no avanzar en el largo.
                    s1 = r.y() + h;
                    s2 = Math.min(r.w() - w, r.h() - h);
                } else if (e == Estrategia.MEJOR_LADO) {
                    s1 = Math.min(r.w() - w, r.h() - h);
                    s2 = Math.max(r.w() - w, r.h() - h);
                } else {
                    s1 = r.area() - w * h;
                    s2 = Math.min(r.w() - w, r.h() - h);
                }
                if (s1 < puntaje - 1e-9 || (Math.abs(s1 - puntaje) < 1e-9 && s2 < desempate)) {
                    puntaje = s1;
                    desempate = s2;
                    mejor = r;
                    mejorGiro = g == 1;
                }
            }
        }
        if (mejor == null) return null;
        double w = mejorGiro ? p.alto() : p.ancho();
        double h = mejorGiro ? p.ancho() : p.alto();
        libres.remove(mejor);
        // Corte guillotina: se divide por el eje mas corto del sobrante.
        double restoW = mejor.w() - w, restoH = mejor.h() - h;
        if (restoW < restoH) {
            agregar(libres, new Rect(mejor.x() + w, mejor.y(), restoW, h));
            agregar(libres, new Rect(mejor.x(), mejor.y() + h, mejor.w(), restoH));
        } else {
            agregar(libres, new Rect(mejor.x() + w, mejor.y(), restoW, mejor.h()));
            agregar(libres, new Rect(mejor.x(), mejor.y() + h, w, restoH));
        }
        return new Colocacion(numero, p, lamina, mejor.x(), mejor.y(), w, h, mejorGiro);
    }

    private static void agregar(List<Rect> libres, Rect r) {
        if (r.w() > 0.5 && r.h() > 0.5) libres.add(r);
    }

    // ================================================================ 1D

    public static Resultado1D optimizar1D(List<Pieza> piezas, double largoTabla) {
        List<Pieza> unidades = new ArrayList<>();
        for (Pieza p : piezas) for (int i = 0; i < p.cantidad(); i++) unidades.add(p.conCantidad(1));
        unidades.sort(Comparator.comparingDouble(Pieza::ancho).reversed());
        List<List<Colocacion>> tablas = new ArrayList<>();
        List<Double> usado = new ArrayList<>();
        double total = 0;
        int n = 0;
        for (Pieza p : unidades) {
            double len = Math.min(p.ancho(), largoTabla);
            total += len;
            n++;
            int elegida = -1;
            for (int t = 0; t < tablas.size(); t++) {
                double necesario = len + (tablas.get(t).isEmpty() ? 0 : KERF);
                if (usado.get(t) + necesario <= largoTabla + 1e-6) {
                    elegida = t;
                    break;
                }
            }
            if (elegida < 0) {
                tablas.add(new ArrayList<>());
                usado.add(0d);
                elegida = tablas.size() - 1;
            }
            double x = usado.get(elegida) + (tablas.get(elegida).isEmpty() ? 0 : KERF);
            tablas.get(elegida).add(new Colocacion(n, p, elegida, x, 0, len, 0, false));
            usado.set(elegida, x + len);
        }
        List<Tabla> r = new ArrayList<>();
        int num = 0;
        for (int t = 0; t < tablas.size(); t++) {
            List<Colocacion> renum = new ArrayList<>();
            for (Colocacion c : tablas.get(t)) {
                renum.add(new Colocacion(++num, c.pieza(), t, c.x(), 0, c.w(), 0, false));
            }
            r.add(new Tabla(t, renum, Math.max(0, largoTabla - usado.get(t))));
        }
        return new Resultado1D(largoTabla, r, total);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
