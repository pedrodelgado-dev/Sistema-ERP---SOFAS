/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Texturas
 * Descripción: Texturas procedurales (tejido por tipo de tela, sombra del piso) generadas una vez y cacheadas.
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
package pe.erp.sofas.ui.three;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.*;
import pe.erp.sofas.model.TipoTela;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Texturas procedurales (tejido por tipo de tela, sombra del piso) generadas una vez y cacheadas. */
public final class Texturas {

    private static final Map<String, Image> CACHE = new HashMap<>();

    private Texturas() {
    }

    /** Textura del tejido (gris neutro modulado; se tine con el diffuseColor del material). */
    public static Image tejido(TipoTela tipo) {
        String k = "tejido-" + (tipo == null ? "x" : tipo.name());
        return CACHE.computeIfAbsent(k, x -> {
            int s = 128;
            WritableImage img = new WritableImage(s, s);
            var pw = img.getPixelWriter();
            Random r = new Random(7);
            for (int y = 0; y < s; y++) {
                for (int xx = 0; xx < s; xx++) {
                    double v = 0.93;
                    if (tipo != null) {
                        switch (tipo) {
                            case LINO -> v = 0.86 + 0.08 * (((xx / 2) % 2 == 0) ^ ((y / 2) % 2 == 0) ? 1 : 0) + r.nextDouble() * 0.06;
                            case CHENILLE -> v = 0.84 + 0.1 * Math.abs(Math.sin((xx + y) * 0.35)) + r.nextDouble() * 0.05;
                            case BOUCLE -> {
                                double cx = (xx % 8) - 4, cy = (y % 8) - 4;
                                v = 0.80 + 0.16 * Math.max(0, 1 - Math.sqrt(cx * cx + cy * cy) / 4) + r.nextDouble() * 0.04;
                            }
                            case TERCIOPELO -> v = 0.9 + r.nextDouble() * 0.06;
                            case CUERO -> v = 0.93 + r.nextDouble() * 0.04 - (r.nextDouble() > 0.985 ? 0.08 : 0);
                            case MICROFIBRA -> v = 0.9 + r.nextDouble() * 0.05;
                        }
                    }
                    v = Math.max(0, Math.min(1, v));
                    pw.setColor(xx, y, Color.gray(v));
                }
            }
            return img;
        });
    }

    /** Sombra suave para el piso: degradado radial oscuro con alfa. */
    public static Image sombra() {
        return CACHE.computeIfAbsent("sombra", x -> {
            Canvas c = new Canvas(256, 256);
            GraphicsContext g = c.getGraphicsContext2D();
            g.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(15, 23, 42, 0.38)), new Stop(0.6, Color.rgb(15, 23, 42, 0.16)), new Stop(1, Color.rgb(15, 23, 42, 0))));
            g.fillRect(0, 0, 256, 256);
            var sp = new javafx.scene.SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            return c.snapshot(sp, null);
        });
    }

    /** Piso de madera clara en tablas. */
    public static Image piso(boolean madera) {
        return CACHE.computeIfAbsent("piso-" + madera, x -> {
            int s = 256;
            WritableImage img = new WritableImage(s, s);
            var pw = img.getPixelWriter();
            Random r = new Random(3);
            for (int y = 0; y < s; y++) {
                for (int xx = 0; xx < s; xx++) {
                    Color c;
                    if (madera) {
                        int tabla = y / 32;
                        double tono = 0.78 + (tabla % 3) * 0.03 + Math.sin(xx * 0.05 + tabla) * 0.02 + r.nextDouble() * 0.02;
                        c = y % 32 == 0 ? Color.color(0.62, 0.5, 0.38) : Color.color(Math.min(1, tono + 0.08), tono, tono * 0.82);
                    } else {
                        double v = 0.95 + r.nextDouble() * 0.02;
                        c = Color.color(v, v, v * 0.99);
                    }
                    pw.setColor(xx, y, c);
                }
            }
            return img;
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
