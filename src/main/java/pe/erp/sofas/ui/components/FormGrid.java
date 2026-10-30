/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: FormGrid
 * Descripción: Formulario en rejilla: etiquetas arriba de cada control, secciones numeradas y mensaje de error.
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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/** Formulario en rejilla: etiquetas arriba de cada control, secciones numeradas y mensaje de error. */
public class FormGrid extends VBox {

    private final GridPane grid = new GridPane();
    private final Label error = UI.label("", "form-error");
    private final int columnas;
    private int col;
    private int fila;

    public FormGrid(int columnas) {
        super(14);
        this.columnas = columnas;
        grid.setHgap(14);
        grid.setVgap(14);
        for (int i = 0; i < columnas; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columnas);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
        error.setGraphic(Ico.of("ERROR", 16));
        error.setWrapText(true);
        error.setMaxWidth(Double.MAX_VALUE);
        error.setVisible(false);
        error.setManaged(false);
        getChildren().addAll(error, grid);
    }

    public FormGrid campo(String etiqueta, Node control) {
        return campo(etiqueta, control, 1);
    }

    public FormGrid campo(String etiqueta, Node control, int span) {
        return nodo(UI.field(etiqueta, control), span);
    }

    public FormGrid nodo(Node n, int span) {
        if (col + span > columnas) {
            col = 0;
            fila++;
        }
        grid.add(n, col, fila, span, 1);
        if (n instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        col += span;
        if (col >= columnas) {
            col = 0;
            fila++;
        }
        return this;
    }

    /** Cabecera de seccion a todo el ancho: "1  Cliente". */
    public FormGrid seccion(String numero, String titulo) {
        if (col != 0) {
            col = 0;
            fila++;
        }
        Label num = UI.label(numero, "form-section-num");
        var h = UI.hbox(10, num, UI.label(titulo, "form-section-title"));
        if (fila > 0) h.setStyle("-fx-padding: 8 0 0 0;");
        grid.add(h, 0, fila++, columnas, 1);
        return this;
    }

    public void error(String mensaje) {
        error.setText(mensaje);
        error.setVisible(true);
        error.setManaged(true);
        UI.entrar(error);
    }

    public void limpiarError() {
        error.setVisible(false);
        error.setManaged(false);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
