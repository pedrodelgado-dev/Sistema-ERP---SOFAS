/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PlanoCorte
 * Descripción: Dibuja el plano de corte en un Canvas: planchas (espuma 200 × 100, triplay 244 × 122) con piezas numeradas y
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
package pe.erp.sofas.ui.views.produccion;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import pe.erp.sofas.service.calculo.Corte;
import pe.erp.sofas.service.calculo.TipoCorte;

import java.util.List;
import java.util.Set;

/**
 * Dibuja el plano de corte en un Canvas: planchas (espuma 200 × 100, triplay 244 × 122) con piezas numeradas y
 * coloreadas por componente y el sobrante rayado; rollo de tela de 140 cm en tramos de 4 m con escala en metros;
 * tablas de madera de 300 cm con los cortes y el sobrante en cm.
 */
public final class PlanoCorte {

    private static final double MARGEN = 24;
    private static final Font NUM = Font.font("Inter", FontWeight.BOLD, 11);
    private static final Font TXT = Font.font("Inter", 11);
    private static final Font TIT = Font.font("Inter", FontWeight.BOLD, 12);

    private PlanoCorte() {
    }

    public static Canvas dibujar2D(Corte.Resultado r, double ancho, Set<Integer> cortadas, boolean oscuro) {
        if (r.tipo() == TipoCorte.TELA) return rollo(r, ancho, cortadas, oscuro);
        double escala = Math.min(3.2, (ancho - 2 * MARGEN) / r.ancho());
        double altoLam = r.alto() * escala;
        double h = MARGEN + r.laminas() * (altoLam + 42) + 10;
        Canvas c = new Canvas(ancho, h);
        GraphicsContext g = c.getGraphicsContext2D();
        Color texto = Color.web(oscuro ? "#E5E7EB" : "#0F172A");
        Color muted = Color.web(oscuro ? "#94A3B8" : "#64748B");
        for (int i = 0; i < r.laminas(); i++) {
            double y0 = MARGEN + i * (altoLam + 42) + 18;
            g.setFont(TIT);
            g.setFill(texto);
            g.setTextAlign(TextAlignment.LEFT);
            g.setTextBaseline(VPos.BOTTOM);
            String nombre = r.tipo() == TipoCorte.ESPUMA ? "Plancha de espuma" : "Plancha de triplay";
            g.fillText(nombre + " " + (i + 1) + " · " + (int) r.ancho() + " × " + (int) r.alto() + " cm", MARGEN, y0 - 4);
            g.setFill(muted);
            g.setTextAlign(TextAlignment.RIGHT);
            g.fillText(String.format("aprovechamiento %.0f %%", r.aprovechamientoLamina(i)), MARGEN + r.ancho() * escala, y0 - 4);
            g.setFill(Color.web(oscuro ? "#1E2A44" : "#FFFFFF"));
            g.fillRect(MARGEN, y0, r.ancho() * escala, altoLam);
            // sobrante rayado
            for (Corte.Rect s : r.sobrantes().get(i)) rayado(g, MARGEN + s.x() * escala, y0 + s.y() * escala, s.w() * escala, s.h() * escala,
                    s.w() >= Corte.MIN_RETAZO && s.h() >= Corte.MIN_RETAZO, oscuro);
            for (Corte.Colocacion col : r.colocaciones()) {
                if (col.lamina() != i) continue;
                pieza(g, col, MARGEN + col.x() * escala, y0 + col.y() * escala, col.w() * escala, col.h() * escala, cortadas.contains(col.numero()));
            }
            g.setStroke(Color.web(oscuro ? "#475569" : "#94A3B8"));
            g.setLineWidth(1.2);
            g.strokeRect(MARGEN, y0, r.ancho() * escala, altoLam);
        }
        return c;
    }

    private static Canvas rollo(Corte.Resultado r, double ancho, Set<Integer> cortadas, boolean oscuro) {
        double tramo = 400;
        int tramos = Math.max(1, (int) Math.ceil(r.largoUsado() / tramo));
        double escala = (ancho - 2 * MARGEN) / tramo;
        double altoRollo = r.ancho() * escala;
        double h = MARGEN + tramos * (altoRollo + 52) + 6;
        Canvas c = new Canvas(ancho, h);
        GraphicsContext g = c.getGraphicsContext2D();
        Color texto = Color.web(oscuro ? "#E5E7EB" : "#0F172A");
        Color muted = Color.web(oscuro ? "#94A3B8" : "#64748B");
        for (int k = 0; k < tramos; k++) {
            double y0 = MARGEN + k * (altoRollo + 52) + 18;
            double desde = k * tramo, hasta = Math.min(r.largoUsado(), desde + tramo);
            g.setFont(TIT);
            g.setFill(texto);
            g.setTextAlign(TextAlignment.LEFT);
            g.setTextBaseline(VPos.BOTTOM);
            g.fillText(String.format("Rollo de 140 cm · tramo %d (%.1f – %.1f m)", k + 1, desde / 100, hasta / 100), MARGEN, y0 - 4);
            g.setFill(Color.web(oscuro ? "#1E2A44" : "#FFFFFF"));
            g.fillRect(MARGEN, y0, (hasta - desde) * escala, altoRollo);
            g.save();
            g.beginPath();
            g.rect(MARGEN, y0, (hasta - desde) * escala, altoRollo);
            g.clip();
            for (Corte.Colocacion col : r.colocaciones()) {
                if (col.y() + col.h() <= desde || col.y() >= hasta) continue;
                double x = MARGEN + (col.y() - desde) * escala;
                double y = y0 + col.x() * escala;
                pieza(g, col, x, y, col.h() * escala, col.w() * escala, cortadas.contains(col.numero()));
            }
            g.restore();
            g.setStroke(Color.web(oscuro ? "#475569" : "#94A3B8"));
            g.setLineWidth(1.2);
            g.strokeRect(MARGEN, y0, (hasta - desde) * escala, altoRollo);
            // escala en metros
            g.setStroke(muted);
            g.setFill(muted);
            g.setFont(TXT);
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.TOP);
            for (double m = desde; m <= hasta + 0.1; m += 50) {
                double x = MARGEN + (m - desde) * escala;
                boolean metro = Math.abs(m % 100) < 0.1;
                g.strokeLine(x, y0 + altoRollo, x, y0 + altoRollo + (metro ? 7 : 4));
                if (metro) g.fillText(String.format("%.0f m", m / 100), x, y0 + altoRollo + 8);
            }
        }
        return c;
    }

    public static Canvas dibujar1D(Corte.Resultado1D r, double ancho, Set<Integer> cortadas, boolean oscuro) {
        double escala = (ancho - 2 * MARGEN - 70) / r.largoTabla();
        double altoTabla = 30;
        double h = MARGEN + r.tablas().size() * (altoTabla + 30) + 10;
        Canvas c = new Canvas(ancho, h);
        GraphicsContext g = c.getGraphicsContext2D();
        Color texto = Color.web(oscuro ? "#E5E7EB" : "#0F172A");
        Color muted = Color.web(oscuro ? "#94A3B8" : "#64748B");
        int i = 0;
        for (Corte.Tabla t : r.tablas()) {
            double y0 = MARGEN + i * (altoTabla + 30) + 12;
            g.setFont(TIT);
            g.setFill(texto);
            g.setTextAlign(TextAlignment.LEFT);
            g.setTextBaseline(VPos.BOTTOM);
            g.fillText("Tabla " + (i + 1) + " · 300 cm", MARGEN, y0 - 3);
            g.setFill(Color.web("#E4C9A4"));
            g.fillRect(MARGEN, y0, r.largoTabla() * escala, altoTabla);
            for (Corte.Colocacion col : t.cortes()) {
                pieza(g, col, MARGEN + col.x() * escala, y0, col.w() * escala, altoTabla, cortadas.contains(col.numero()));
            }
            double usado = r.largoTabla() - t.sobrante();
            rayado(g, MARGEN + usado * escala, y0, t.sobrante() * escala, altoTabla, t.sobrante() >= 40, oscuro);
            g.setStroke(Color.web("#A47148"));
            g.strokeRect(MARGEN, y0, r.largoTabla() * escala, altoTabla);
            g.setFont(TXT);
            g.setFill(muted);
            g.setTextAlign(TextAlignment.LEFT);
            g.setTextBaseline(VPos.CENTER);
            g.fillText(String.format("sobra %.1f cm", t.sobrante()), MARGEN + r.largoTabla() * escala + 8, y0 + altoTabla / 2);
            i++;
        }
        return c;
    }

    private static void pieza(GraphicsContext g, Corte.Colocacion col, double x, double y, double w, double h, boolean cortada) {
        Color base = Color.web(col.pieza().componente().color());
        g.setFill(Color.color(base.getRed(), base.getGreen(), base.getBlue(), cortada ? 0.18 : 0.78));
        g.fillRect(x + 0.5, y + 0.5, Math.max(1, w - 1), Math.max(1, h - 1));
        g.setStroke(Color.WHITE);
        g.setLineWidth(1);
        g.strokeRect(x + 0.5, y + 0.5, Math.max(1, w - 1), Math.max(1, h - 1));
        if (w > 14 && h > 12) {
            g.setFill(cortada ? Color.web("#64748B") : Color.WHITE);
            g.setFont(NUM);
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText(String.valueOf(col.numero()), x + w / 2, y + h / 2 - (h > 34 && w > 50 ? 6 : 0));
            if (h > 34 && w > 50) {
                g.setFont(Font.font("Inter", 9.5));
                g.fillText(Math.round(col.pieza().ancho()) + "×" + Math.round(col.pieza().alto()) + (col.girada() ? " ↻" : ""), x + w / 2, y + h / 2 + 8);
            }
        }
    }

    private static void rayado(GraphicsContext g, double x, double y, double w, double h, boolean reutilizable, boolean oscuro) {
        if (w <= 0 || h <= 0) return;
        g.save();
        g.beginPath();
        g.rect(x, y, w, h);
        g.clip();
        g.setFill(Color.web(reutilizable ? (oscuro ? "#2A2119" : "#F5ECE1") : (oscuro ? "#17223A" : "#F1F5F9")));
        g.fillRect(x, y, w, h);
        g.setStroke(Color.web(reutilizable ? "#C69A6D" : "#CBD5E1"));
        g.setLineWidth(1);
        for (double d = -h; d < w; d += 8) g.strokeLine(x + d, y + h, x + d + h, y);
        g.restore();
    }

    /** Texto de rango "1–6" para numeros consecutivos. */
    public static String rango(List<Integer> nums) {
        if (nums.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int ini = nums.get(0), prev = ini;
        for (int i = 1; i <= nums.size(); i++) {
            int n = i < nums.size() ? nums.get(i) : Integer.MIN_VALUE;
            if (n == prev + 1) {
                prev = n;
                continue;
            }
            if (sb.length() > 0) sb.append(", ");
            sb.append(ini == prev ? String.valueOf(ini) : ini + "–" + prev);
            ini = prev = n;
        }
        return sb.toString();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
