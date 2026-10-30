/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SearchField
 * Descripción: Buscador con icono de lupa y boton para limpiar.
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

import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** Buscador con icono de lupa y boton para limpiar. */
public class SearchField extends HBox {

    private final TextField campo = new TextField();

    public SearchField(String prompt) {
        this(prompt, "SEARCH");
    }

    public SearchField(String prompt, String icono) {
        super(8);
        getStyleClass().add("search-field");
        campo.setPromptText(prompt);
        campo.getStyleClass().add("search-input");
        HBox.setHgrow(campo, Priority.ALWAYS);
        Button limpiar = UI.iconButton("CLOSE", "Limpiar búsqueda", "btn-ghost", "btn-xs");
        limpiar.visibleProperty().bind(campo.textProperty().isNotEmpty());
        limpiar.managedProperty().bind(limpiar.visibleProperty());
        limpiar.setOnAction(e -> {
            campo.clear();
            campo.requestFocus();
        });
        limpiar.setFocusTraversable(false);
        getChildren().addAll(Ico.of(icono, 18), campo, limpiar);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        setOnMouseClicked(e -> campo.requestFocus());
    }

    public StringProperty textProperty() {
        return campo.textProperty();
    }

    public String getText() {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    public void setText(String t) {
        campo.setText(t);
    }

    public TextField campo() {
        return campo;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
