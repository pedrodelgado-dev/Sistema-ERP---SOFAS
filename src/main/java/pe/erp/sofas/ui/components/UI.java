/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: UI
 * Descripción: Fabrica de nodos con las clases de estilo del sistema de diseño.
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

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.function.Consumer;

/** Fabrica de nodos con las clases de estilo del sistema de diseño. */
public final class UI {

    private UI() {
    }

    public static Label label(String texto, String... clases) {
        Label l = new Label(texto);
        l.getStyleClass().addAll(clases);
        return l;
    }

    public static Label wrap(String texto, String... clases) {
        Label l = label(texto, clases);
        l.setWrapText(true);
        l.setMinHeight(Region.USE_PREF_SIZE);
        return l;
    }

    public static HBox hbox(double spacing, Node... nodos) {
        HBox h = new HBox(spacing, nodos);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    public static VBox vbox(double spacing, Node... nodos) {
        return new VBox(spacing, nodos);
    }

    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    public static <T extends Node> T grow(T nodo) {
        HBox.setHgrow(nodo, Priority.ALWAYS);
        VBox.setVgrow(nodo, Priority.ALWAYS);
        if (nodo instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
        }
        return nodo;
    }

    public static Button button(String texto, String icono, String... clases) {
        Button b = new Button(texto);
        if (icono != null) b.setGraphic(Ico.of(icono, 17));
        b.getStyleClass().add("btn");
        b.getStyleClass().addAll(clases);
        b.setMnemonicParsing(false);
        b.setMinWidth(Region.USE_PREF_SIZE);
        return b;
    }

    public static Button primary(String texto, String icono) {
        return button(texto, icono, "btn-primary");
    }

    public static Button secondary(String texto, String icono) {
        return button(texto, icono, "btn-secondary");
    }

    public static Button danger(String texto, String icono) {
        return button(texto, icono, "btn-danger");
    }

    public static Button success(String texto, String icono) {
        return button(texto, icono, "btn-success");
    }

    /** Boton cuadrado solo con icono y tooltip (accesible por teclado). */
    public static Button iconButton(String icono, String tooltip, String... clases) {
        Button b = new Button();
        b.setGraphic(Ico.of(icono, 18));
        b.getStyleClass().addAll("btn", "btn-icon");
        b.getStyleClass().addAll(clases);
        tooltip(b, tooltip);
        b.setAccessibleText(tooltip);
        return b;
    }

    public static void tooltip(Control c, String texto) {
        Tooltip t = new Tooltip(texto);
        t.setShowDelay(Duration.millis(250));
        c.setTooltip(t);
    }

    public static VBox card(Node... contenido) {
        VBox v = new VBox(14, contenido);
        v.getStyleClass().add("card");
        v.setPadding(new Insets(18, 20, 18, 20));
        return v;
    }

    /** Cabecera de tarjeta: titulo + subtitulo opcional a la izquierda, acciones a la derecha. */
    public static HBox cardHeader(String titulo, String subtitulo, Node... acciones) {
        VBox textos = new VBox(2, label(titulo, "card-title"));
        if (subtitulo != null) textos.getChildren().add(label(subtitulo, "card-subtitle"));
        HBox h = hbox(8, textos, spacer());
        h.getChildren().addAll(acciones);
        return h;
    }

    /** Campo de formulario: etiqueta arriba + control + mensaje de ayuda opcional. */
    public static VBox field(String etiqueta, Node control) {
        VBox v = new VBox(6, label(etiqueta, "form-label"), control);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    public static TextField text(String valor, String prompt) {
        TextField t = new TextField(valor);
        t.setPromptText(prompt);
        return t;
    }

    public static GridPane grid(int columnas) {
        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(14);
        for (int i = 0; i < columnas; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columnas);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
        }
        return g;
    }

    /** Par etiqueta / valor de una ficha de detalle. */
    public static VBox dato(String etiqueta, String valor) {
        Label v = wrap(valor == null || valor.isBlank() ? "—" : valor, "dato-valor");
        return new VBox(3, label(etiqueta, "dato-label"), v);
    }

    public static VBox dato(String etiqueta, Node valor) {
        return new VBox(3, label(etiqueta, "dato-label"), valor);
    }

    public static Separator separator() {
        Separator s = new Separator();
        s.getStyleClass().add("sep");
        return s;
    }

    public static ScrollPane scroll(Node contenido) {
        ScrollPane sp = new ScrollPane(contenido);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("page-scroll");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    /** Estado vacio con icono, titulo, texto y accion opcional. */
    public static VBox empty(String icono, String titulo, String texto, Button accion) {
        StackPane circulo = new StackPane(Ico.of(icono, 26));
        circulo.getStyleClass().add("empty-icon");
        VBox v = new VBox(8, circulo, label(titulo, "empty-title"), wrap(texto, "empty-text"));
        if (accion != null) {
            VBox.setMargin(accion, new Insets(6, 0, 0, 0));
            v.getChildren().add(accion);
        }
        v.setAlignment(Pos.CENTER);
        v.getStyleClass().add("empty-state");
        v.setMaxWidth(420);
        return v;
    }

    /** Animacion de entrada: fade + desplazamiento vertical de 8 px. */
    public static void entrar(Node nodo) {
        nodo.setOpacity(0);
        FadeTransition f = new FadeTransition(Duration.millis(240), nodo);
        f.setToValue(1);
        TranslateTransition t = new TranslateTransition(Duration.millis(240), nodo);
        t.setFromY(8);
        t.setToY(0);
        new ParallelTransition(f, t).play();
    }

    public static void onClick(Node nodo, Consumer<javafx.scene.input.MouseEvent> accion) {
        nodo.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) accion.accept(e);
        });
        nodo.getStyleClass().add("clickable");
    }

    public static ToggleButton toggle(String texto, ToggleGroup grupo, String... clases) {
        ToggleButton t = new ToggleButton(texto);
        t.setToggleGroup(grupo);
        t.getStyleClass().addAll(clases);
        t.setMnemonicParsing(false);
        return t;
    }

    /** Fila de tarjetas de igual ancho (KPI). */
    public static GridPane fila(Node... nodos) {
        GridPane g = new GridPane();
        g.setHgap(14);
        g.setMinHeight(Region.USE_PREF_SIZE);
        for (int i = 0; i < nodos.length; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / nodos.length);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
            g.add(nodos[i], i, 0);
            if (nodos[i] instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
            GridPane.setFillHeight(nodos[i], true);
        }
        return g;
    }

    /** Fila con anchos proporcionales (pesos). */
    public static GridPane columnas(double[] pesos, Node... nodos) {
        GridPane g = new GridPane();
        g.setHgap(14);
        g.setMinHeight(Region.USE_PREF_SIZE);
        double total = 0;
        for (double p : pesos) total += p;
        for (int i = 0; i < nodos.length; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(pesos[i] * 100 / total);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
            g.add(nodos[i], i, 0);
            if (nodos[i] instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
            GridPane.setFillHeight(nodos[i], true);
            GridPane.setValignment(nodos[i], javafx.geometry.VPos.TOP);
        }
        return g;
    }

    /** Dato chico con valor grande (resumenes en paneles). */
    public static VBox miniStat(String etiqueta, String valor) {
        VBox v = new VBox(2, label(valor, "mini-stat-value"), label(etiqueta, "mini-stat-label"));
        v.getStyleClass().add("mini-stat");
        HBox.setHgrow(v, Priority.ALWAYS);
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }

    /** Banner de aviso (lock / danger / ok) con icono y texto. */
    public static HBox banner(String clase, String icono, String texto) {
        Label l = wrap(texto);
        HBox.setHgrow(l, Priority.ALWAYS);
        HBox h = new HBox(8, Ico.of(icono, 17), l);
        h.getStyleClass().add(clase);
        return h;
    }

    public static Label codigo(String texto) {
        return label(texto, "code");
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
