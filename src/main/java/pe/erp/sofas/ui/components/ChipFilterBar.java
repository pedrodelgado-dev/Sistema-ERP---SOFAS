/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ChipFilterBar
 * Descripción: Fila de chips de filtro con contador; siempre hay uno seleccionado.
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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

import java.util.List;

/** Fila de chips de filtro con contador; siempre hay uno seleccionado. */
public class ChipFilterBar extends FlowPane {

    public record Chip(String clave, String texto, long conteo) {
    }

    private final ToggleGroup grupo = new ToggleGroup();
    private final ObjectProperty<String> seleccion = new SimpleObjectProperty<>();
    private boolean reconstruyendo;

    public ChipFilterBar() {
        super(8, 8);
        getStyleClass().add("chip-bar");
        setPrefWrapLength(1150);
        setMinWidth(0);
        setMinHeight(USE_PREF_SIZE);
        grupo.selectedToggleProperty().addListener((o, a, n) -> {
            if (reconstruyendo) return;
            if (n == null) {
                if (a != null) grupo.selectToggle(a);
                return;
            }
            seleccion.set((String) n.getUserData());
        });
    }

    /** Reemplaza los chips conservando la seleccion actual si sigue existiendo. */
    public void setChips(List<Chip> chips) {
        String actual = seleccion.get();
        reconstruyendo = true;
        getChildren().clear();
        grupo.getToggles().clear();
        ToggleButton aSeleccionar = null;
        for (Chip c : chips) {
            ToggleButton t = new ToggleButton(c.texto());
            t.setUserData(c.clave());
            t.setToggleGroup(grupo);
            t.getStyleClass().add("chip");
            t.setMnemonicParsing(false);
            if (c.conteo() >= 0) {
                Label n = UI.label(String.valueOf(c.conteo()), "chip-count");
                t.setGraphic(n);
                t.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
            }
            getChildren().add(t);
            if (aSeleccionar == null || c.clave().equals(actual)) aSeleccionar = t;
        }
        if (aSeleccionar != null) grupo.selectToggle(aSeleccionar);
        reconstruyendo = false;
        // Solo notifica si la clave seleccionada cambio (evita recursion cuando se llama desde un render).
        if (aSeleccionar != null) seleccion.set((String) aSeleccionar.getUserData());
    }

    public ObjectProperty<String> seleccionProperty() {
        return seleccion;
    }

    public String getSeleccion() {
        return seleccion.get();
    }

    public void seleccionar(String clave) {
        grupo.getToggles().stream().filter(t -> clave.equals(t.getUserData())).findFirst().ifPresent(grupo::selectToggle);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
