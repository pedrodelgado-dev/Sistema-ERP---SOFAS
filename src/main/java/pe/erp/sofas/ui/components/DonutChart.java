/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: DonutChart
 * Descripción: Dona con leyenda; al pasar el mouse por un segmento el centro muestra su nombre y porcentaje.
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

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.List;

/** Dona con leyenda; al pasar el mouse por un segmento el centro muestra su nombre y porcentaje. */
public class DonutChart extends HBox {

    public record Segmento(String nombre, double valor, String color) {
    }

    private final Label centroValor = UI.label("", "donut-value");
    private final Label centroTexto = UI.label("", "donut-caption");

    public DonutChart(List<Segmento> segmentos, double diametro, String tituloCentro, String sufijo) {
        this(segmentos, diametro, tituloCentro, sufijo, false);
    }

    /** Con {@code vertical} la leyenda va debajo de la dona (columnas angostas). */
    public DonutChart(List<Segmento> segmentos, double diametro, String tituloCentro, String sufijo, boolean vertical) {
        super(22);
        setAlignment(Pos.CENTER_LEFT);
        double total = segmentos.stream().mapToDouble(Segmento::valor).sum();
        double grosor = diametro * 0.16;
        double r = (diametro - grosor) / 2;
        Pane lienzo = new Pane();
        lienzo.setMinSize(diametro, diametro);
        lienzo.setPrefSize(diametro, diametro);
        lienzo.setMaxSize(diametro, diametro);
        Circle fondo = new Circle(diametro / 2, diametro / 2, r);
        fondo.getStyleClass().add("donut-track");
        fondo.setStrokeWidth(grosor);
        lienzo.getChildren().add(fondo);

        Runnable restablecer = () -> {
            centroValor.setText(tituloCentro);
            centroTexto.setText(sufijo);
        };
        restablecer.run();

        List<HBox> leyendas = new ArrayList<>();
        double inicio = 90;
        for (Segmento s : segmentos) {
            double angulo = total == 0 ? 0 : s.valor() / total * 360;
            Arc arc = new Arc(diametro / 2, diametro / 2, r, r, inicio, -Math.max(0, angulo - 1.6));
            arc.setType(ArcType.OPEN);
            arc.setFill(null);
            arc.setStrokeWidth(grosor);
            arc.setStrokeLineCap(StrokeLineCap.BUTT);
            arc.setStyle("-fx-stroke: " + s.color() + ";");
            arc.getStyleClass().add("donut-arc");
            String pct = String.format("%.0f %%", total == 0 ? 0 : s.valor() * 100 / total);
            arc.setOnMouseEntered(e -> {
                arc.setStrokeWidth(grosor + 5);
                centroValor.setText(pct);
                centroTexto.setText(s.nombre());
            });
            arc.setOnMouseExited(e -> {
                arc.setStrokeWidth(grosor);
                restablecer.run();
            });
            lienzo.getChildren().add(arc);
            inicio -= angulo;

            javafx.scene.layout.Region punto = new javafx.scene.layout.Region();
            punto.getStyleClass().add("legend-dot");
            punto.setStyle("-fx-background-color: " + s.color() + ";");
            HBox fila = new HBox(8, punto, UI.label(s.nombre(), "legend-text"), UI.spacer(), UI.label(pct, "legend-value"));
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setOnMouseEntered(e -> arc.getOnMouseEntered().handle(null));
            fila.setOnMouseExited(e -> arc.getOnMouseExited().handle(null));
            leyendas.add(fila);
        }
        VBox centro = new VBox(0, centroValor, centroTexto);
        centro.setAlignment(Pos.CENTER);
        centro.setMouseTransparent(true);
        centro.setMaxWidth(r * 1.6);
        centroTexto.setWrapText(true);
        centroTexto.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        StackPane dona = new StackPane(lienzo, centro);
        dona.setMinSize(diametro, diametro);
        VBox leyenda = new VBox(9);
        leyenda.getChildren().addAll(leyendas);
        leyenda.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leyenda, javafx.scene.layout.Priority.ALWAYS);
        if (vertical) {
            VBox col = new VBox(14, dona, leyenda);
            col.setAlignment(Pos.TOP_CENTER);
            HBox.setHgrow(col, javafx.scene.layout.Priority.ALWAYS);
            leyenda.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(col);
        } else {
            getChildren().addAll(dona, leyenda);
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
