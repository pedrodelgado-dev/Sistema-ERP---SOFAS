/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ProgressBarMini
 * Descripción: Barra de progreso fina con color propio (etapas, stock, margen, % pagado).
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

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/** Barra de progreso fina con color propio (etapas, stock, margen, % pagado). */
public class ProgressBarMini extends StackPane {

    private final Region relleno = new Region();
    private double valor;

    public ProgressBarMini(double valor, String color, double alto) {
        getStyleClass().add("progress-track");
        relleno.getStyleClass().add("progress-fill");
        setMinHeight(alto);
        setPrefHeight(alto);
        setMaxHeight(alto);
        relleno.setMaxHeight(alto);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        getChildren().add(relleno);
        setStyle("-fx-background-radius: " + alto + ";");
        setColor(color);
        setValor(valor);
        widthProperty().addListener((o, a, n) -> ajustar());
    }

    public void setColor(String color) {
        relleno.setStyle("-fx-background-color: " + color + "; -fx-background-radius: " + getPrefHeight() + ";");
    }

    /** Valor entre 0 y 1. */
    public void setValor(double v) {
        this.valor = Math.max(0, Math.min(1, v));
        ajustar();
    }

    private void ajustar() {
        double w = getWidth() * valor;
        relleno.setMinWidth(w);
        relleno.setPrefWidth(w);
        relleno.setMaxWidth(w);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
