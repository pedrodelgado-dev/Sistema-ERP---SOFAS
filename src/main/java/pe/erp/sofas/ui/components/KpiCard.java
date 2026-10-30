/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: KpiCard
 * Descripción: Tarjeta de indicador: icono en chip de color, etiqueta, valor grande y linea secundaria.
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Tarjeta de indicador: icono en chip de color, etiqueta, valor grande y linea secundaria.
 * Variantes de color: blue, wood, green, amber, red, navy, teal, violet.
 */
public class KpiCard extends VBox {

    private final Label valor;
    private final Label sub;

    public KpiCard(String icono, String variante, String etiqueta, String valor, String sub) {
        super(10);
        getStyleClass().addAll("card", "kpi-card");
        StackPane chip = new StackPane(Ico.of(icono, 19));
        chip.getStyleClass().addAll("kpi-icon", "kpi-" + variante);
        Label lbl = UI.wrap(etiqueta, "kpi-label");
        HBox top = new HBox(10, lbl, UI.spacer(), chip);
        top.setAlignment(Pos.TOP_LEFT);
        this.valor = UI.label(valor, "kpi-value");
        this.sub = UI.label(sub == null ? "" : sub, "kpi-sub");
        getChildren().addAll(top, this.valor, this.sub);
        HBox.setHgrow(this, Priority.ALWAYS);
        setMaxWidth(Double.MAX_VALUE);
        setMinWidth(150);
    }

    /** Sub-linea con tendencia (verde si sube, roja si baja). */
    public KpiCard tendencia(boolean positiva) {
        sub.getStyleClass().add(positiva ? "trend-up" : "trend-down");
        sub.setGraphic(Ico.of(positiva ? "TRENDING_UP" : "TRENDING_DOWN", 15));
        return this;
    }

    /** Borde superior de color (tarjetas de Finanzas). */
    public KpiCard bordeSuperior(String color) {
        setStyle("-fx-border-color: " + color + " transparent transparent transparent; -fx-border-width: 3 0 0 0;");
        return this;
    }

    public KpiCard alClic(Runnable accion) {
        UI.onClick(this, e -> accion.run());
        getStyleClass().add("kpi-clickable");
        return this;
    }

    public void setValor(String v) {
        valor.setText(v);
    }

    public void setSub(String s) {
        sub.setText(s);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
