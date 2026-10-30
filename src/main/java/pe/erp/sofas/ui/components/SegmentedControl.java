/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SegmentedControl
 * Descripción: Control segmentado (Hoy | Semana | Mes, Kanban | Tabla | Calendario...).
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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

/** Control segmentado (Hoy | Semana | Mes, Kanban | Tabla | Calendario...). */
public class SegmentedControl extends HBox {

    private final ToggleGroup grupo = new ToggleGroup();
    private final ObjectProperty<String> valor = new SimpleObjectProperty<>();

    public SegmentedControl(String... opciones) {
        getStyleClass().add("segmented");
        setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        for (String op : opciones) {
            agregar(op, null);
        }
        grupo.selectedToggleProperty().addListener((o, a, n) -> {
            if (n == null) {
                grupo.selectToggle(a);
                return;
            }
            valor.set((String) n.getUserData());
        });
        if (!grupo.getToggles().isEmpty()) grupo.selectToggle(grupo.getToggles().get(0));
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    }

    /** Agrega un segmento con icono opcional. */
    public SegmentedControl agregar(String texto, String icono) {
        ToggleButton t = new ToggleButton(texto);
        t.setUserData(texto);
        t.setToggleGroup(grupo);
        t.getStyleClass().add("segment");
        t.setMnemonicParsing(false);
        t.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        if (icono != null) t.setGraphic(Ico.of(icono, 16));
        getChildren().add(t);
        return this;
    }

    public static SegmentedControl conIconos(String[] textos, String[] iconos) {
        SegmentedControl s = new SegmentedControl();
        for (int i = 0; i < textos.length; i++) s.agregar(textos[i], iconos[i]);
        s.grupo.selectToggle(s.grupo.getToggles().get(0));
        return s;
    }

    public ObjectProperty<String> valorProperty() {
        return valor;
    }

    public String getValor() {
        return valor.get();
    }

    public void setValor(String v) {
        grupo.getToggles().stream().filter(t -> v.equals(t.getUserData())).findFirst().ifPresent(grupo::selectToggle);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
