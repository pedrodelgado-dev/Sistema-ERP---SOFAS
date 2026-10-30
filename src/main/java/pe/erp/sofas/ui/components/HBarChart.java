/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: HBarChart
 * Descripción: Barras horizontales dibujadas a mano en Canvas. Cada fila tiene hasta dos barras (p. ej. requerido frente a
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
package pe.erp.sofas.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.List;

/**
 * Barras horizontales dibujadas a mano en Canvas. Cada fila tiene hasta dos barras (p. ej. requerido frente a
 * stock + transito) y una etiqueta a la derecha ("falta 12 m" / "cubierto").
 */
public class HBarChart extends Region {

    public record Fila(String etiqueta, double a, double b, String nota, boolean alerta) {
    }

    private final Canvas canvas = new Canvas();
    private List<Fila> filas = List.of();
    private String colorA = "#14213D";
    private String colorB = "#16A34A";
    private String leyendaA, leyendaB;
    private boolean centrado;
    private boolean porFila;
    private double alturaFila = 30;

    public HBarChart() {
        getChildren().add(canvas);
        getStyleClass().add("hbar-chart");
        widthProperty().addListener((o, a, b) -> dibujar());
        heightProperty().addListener((o, a, b) -> dibujar());
        sceneProperty().addListener((o, a, b) -> dibujar());
    }

    public HBarChart colores(String a, String b) {
        this.colorA = a;
        this.colorB = b;
        return this;
    }

    public HBarChart leyenda(String a, String b) {
        this.leyendaA = a;
        this.leyendaB = b;
        return this;
    }

    /** Modo divergente: barras desde el centro (negativo a la izquierda, verde; positivo a la derecha, rojo). */
    public HBarChart centrado(boolean c) {
        this.centrado = c;
        return this;
    }

    /** Escala cada fila por su propio maximo (filas con unidades distintas). */
    public HBarChart porFila(boolean p) {
        this.porFila = p;
        return this;
    }

    public void setFilas(List<Fila> filas) {
        this.filas = filas;
        double alto = filas.size() * alturaFila + (leyendaA != null ? 28 : 6);
        setMinHeight(alto);
        setPrefHeight(alto);
        dibujar();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        dibujar();
    }

    private boolean oscuro() {
        return getScene() != null && getScene().getRoot() != null && getScene().getRoot().getStyleClass().contains("dark");
    }

    private void dibujar() {
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        boolean dark = oscuro();
        Color texto = Color.web(dark ? "#E5E7EB" : "#0F172A");
        Color muted = Color.web(dark ? "#94A3B8" : "#64748B");
        Color pista = Color.web(dark ? "#17223A" : "#F1F5F9");
        Font normal = Font.font("Inter", 12);
        Font bold = Font.font("Inter", FontWeight.BOLD, 11.5);
        double etiquetaW = Math.min(190, w * 0.34);
        double notaW = 92;
        double x0 = etiquetaW + 10, x1 = w - notaW - 8;
        double max = 0;
        for (Fila f : filas) max = Math.max(max, centrado ? Math.max(Math.abs(f.a()), Math.abs(f.b())) : Math.max(f.a(), f.b()));
        if (max <= 0) max = 1;
        double y = 4;
        for (Fila f : filas) {
            g.setFont(normal);
            g.setFill(texto);
            g.setTextAlign(TextAlignment.LEFT);
            g.setTextBaseline(VPos.CENTER);
            g.fillText(recortar(f.etiqueta(), normal, etiquetaW), 0, y + alturaFila / 2 - 2);
            double ancho = x1 - x0;
            g.setFill(pista);
            g.fillRoundRect(x0, y + 6, ancho, alturaFila - 14, 6, 6);
            if (centrado) {
                double mid = x0 + ancho / 2;
                double len = Math.abs(f.a()) / max * ancho / 2;
                g.setFill(Color.web(f.a() > 10 ? "#DC2626" : f.a() > 0 ? "#F59E0B" : "#16A34A"));
                if (f.a() >= 0) g.fillRoundRect(mid, y + 6, len, alturaFila - 14, 6, 6);
                else g.fillRoundRect(mid - len, y + 6, len, alturaFila - 14, 6, 6);
                g.setStroke(muted);
                g.setLineWidth(1);
                g.strokeLine(mid, y + 3, mid, y + alturaFila - 5);
            } else {
                double maxFila = porFila ? Math.max(1e-9, Math.max(f.a(), f.b())) : max;
                double hb = (alturaFila - 14) / 2;
                g.setFill(Color.web(colorA));
                g.fillRoundRect(x0, y + 6, Math.max(2, f.a() / maxFila * ancho), hb, 4, 4);
                g.setFill(Color.web(colorB));
                g.fillRoundRect(x0, y + 6 + hb, Math.max(2, f.b() / maxFila * ancho), hb, 4, 4);
            }
            g.setFont(bold);
            g.setFill(f.alerta() ? Color.web("#DC2626") : Color.web(dark ? "#4ADE80" : "#15803D"));
            g.setTextAlign(TextAlignment.RIGHT);
            g.fillText(f.nota(), w - 2, y + alturaFila / 2 - 2);
            y += alturaFila;
        }
        if (leyendaA != null) {
            y += 6;
            g.setFont(normal);
            g.setTextAlign(TextAlignment.LEFT);
            g.setFill(Color.web(colorA));
            g.fillRoundRect(x0, y + 4, 10, 10, 3, 3);
            g.setFill(muted);
            g.fillText(leyendaA, x0 + 16, y + 9);
            double off = x0 + 30 + textoAncho(leyendaA, normal);
            g.setFill(Color.web(colorB));
            g.fillRoundRect(off, y + 4, 10, 10, 3, 3);
            g.setFill(muted);
            g.fillText(leyendaB, off + 16, y + 9);
        }
    }

    private static double textoAncho(String s, Font f) {
        Text t = new Text(s);
        t.setFont(f);
        return t.getLayoutBounds().getWidth();
    }

    private static String recortar(String s, Font f, double max) {
        if (textoAncho(s, f) <= max) return s;
        String r = s;
        while (r.length() > 3 && textoAncho(r + "…", f) > max) r = r.substring(0, r.length() - 1);
        return r + "…";
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
