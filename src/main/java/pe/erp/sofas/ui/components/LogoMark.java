/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: LogoMark
 * Descripción: Logotipo: cuadrado azul marino (radio 11 en 42 px) con la silueta de un sofa en madera y beige,
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Logotipo: cuadrado azul marino (radio 11 en 42 px) con la silueta de un sofa en madera y beige,
 * y el texto "SISTEMA ERP SOFAS" con un subtitulo ("SOFÁS · JULIACA" o "ERP · SOFÁS").
 */
public class LogoMark extends HBox {

    public LogoMark(double size, String subtitulo) {
        super(size * 0.3);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("logo-mark");
        getChildren().add(icono(size));
        if (subtitulo != null) {
            var titulo = UI.label("SISTEMA ERP SOFAS", "logo-title");
            titulo.setStyle("-fx-font-size: " + Math.round(size * 0.40) + "px;");
            var sub = UI.label(subtitulo, "logo-subtitle");
            sub.setStyle("-fx-font-size: " + Math.max(9.5, Math.round(size * 0.24)) + "px;");
            VBox textos = new VBox(Math.max(0, size * 0.02), titulo, sub);
            textos.setAlignment(Pos.CENTER_LEFT);
            textos.getStyleClass().add("logo-texts");
            getChildren().add(textos);
        }
    }

    public static StackPane icono(double size) {
        StackPane box = new StackPane();
        box.getStyleClass().add("logo-box");
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        box.setStyle("-fx-background-radius: " + size * 11 / 42 + ";");
        double u = size / 42.0;
        Color madera = Color.web("#C69A6D"), beige = Color.web("#E4C9A4"), oscura = Color.web("#A47148");
        Pane sofa = new Pane();
        sofa.setMinSize(30 * u, 22 * u);
        sofa.setPrefSize(30 * u, 22 * u);
        sofa.setMaxSize(30 * u, 22 * u);
        sofa.getChildren().addAll(
                rect(4 * u, 2 * u, 22 * u, 10 * u, 3 * u, beige),        // respaldo
                rect(0, 7 * u, 6 * u, 11 * u, 3 * u, madera),             // brazo izquierdo
                rect(24 * u, 7 * u, 6 * u, 11 * u, 3 * u, madera),        // brazo derecho
                rect(5 * u, 11 * u, 20 * u, 6 * u, 2.5 * u, madera),      // asiento
                rect(14.6 * u, 11.5 * u, 0.8 * u, 5 * u, 0, oscura),       // separacion de cojines
                rect(2 * u, 18 * u, 2.2 * u, 3.5 * u, 1 * u, beige),      // patas
                rect(25.8 * u, 18 * u, 2.2 * u, 3.5 * u, 1 * u, beige));
        box.getChildren().add(sofa);
        return box;
    }

    private static Rectangle rect(double x, double y, double w, double h, double r, Color c) {
        Rectangle re = new Rectangle(x, y, w, h);
        re.setArcWidth(r * 2);
        re.setArcHeight(r * 2);
        re.setFill(c);
        return re;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
