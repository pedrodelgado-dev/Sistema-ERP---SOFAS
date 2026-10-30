/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Validacion
 * Descripción: Validacion en linea de formularios: cada regla devuelve el mensaje de error (o null si es valido),
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

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Validacion en linea de formularios: cada regla devuelve el mensaje de error (o null si es valido),
 * se evalua al editar y el boton de guardar queda deshabilitado mientras haya errores.
 */
public class Validacion {

    private record Regla(Node control, Label error, Supplier<String> mensaje) {
    }

    private final List<Regla> reglas = new ArrayList<>();
    private final BooleanProperty valido = new SimpleBooleanProperty(true);
    private boolean tocado;

    /** Campo con etiqueta arriba, control y mensaje de error debajo. */
    public VBox campo(String etiqueta, Node control, Supplier<String> regla) {
        Label error = UI.label("", "field-error");
        error.setVisible(false);
        error.setManaged(false);
        error.setWrapText(true);
        VBox v = new VBox(5, UI.label(etiqueta, "form-label"), control, error);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.HBox.setHgrow(v, Priority.ALWAYS);
        if (regla != null) agregar(control, error, regla);
        return v;
    }

    public Validacion regla(Node control, Supplier<String> regla) {
        agregar(control, null, regla);
        return this;
    }

    private void agregar(Node control, Label error, Supplier<String> regla) {
        reglas.add(new Regla(control, error, regla));
        Observable o = observable(control);
        if (o != null) o.addListener(x -> {
            tocado = true;
            evaluar();
        });
        evaluar();
    }

    /** Re-evalua cuando cambia otro valor del que depende una regla. */
    public void escuchar(Observable... dependencias) {
        for (Observable o : dependencias) o.addListener(x -> evaluar());
    }

    private static Observable observable(Node c) {
        if (c instanceof TextInputControl t) return t.textProperty();
        if (c instanceof ComboBoxBase<?> cb) return cb.valueProperty();
        if (c instanceof ChoiceBox<?> ch) return ch.valueProperty();
        if (c instanceof CheckBox ck) return ck.selectedProperty();
        if (c instanceof Spinner<?> sp) return sp.valueProperty();
        return null;
    }

    public boolean evaluar() {
        boolean ok = true;
        for (Regla r : reglas) {
            String msg;
            try {
                msg = r.mensaje().get();
            } catch (RuntimeException e) {
                msg = "Valor inválido.";
            }
            boolean error = msg != null;
            ok &= !error;
            boolean mostrar = error && (tocado || r.control().getProperties().containsKey("tocado"));
            r.control().getStyleClass().remove("field-invalid");
            if (mostrar) r.control().getStyleClass().add("field-invalid");
            if (r.error() != null) {
                r.error().setText(mostrar ? msg : "");
                r.error().setVisible(mostrar);
                r.error().setManaged(mostrar);
            }
        }
        valido.set(ok);
        return ok;
    }

    public BooleanProperty validoProperty() {
        return valido;
    }

    /** Deshabilita el boton mientras el formulario no sea valido. */
    public void enlazar(Button guardar) {
        guardar.disableProperty().bind(valido.not());
    }

    // ---------------------------------------------------------------- reglas comunes

    public static String requerido(TextInputControl t, String nombre) {
        return t.getText() == null || t.getText().isBlank() ? "Ingresa " + nombre + "." : null;
    }

    public static String requerido(ComboBoxBase<?> c, String nombre) {
        return c.getValue() == null ? "Elige " + nombre + "." : null;
    }

    public static String numero(TextInputControl t, double min, String nombre) {
        try {
            double v = Double.parseDouble(t.getText().replace(",", "").replace("S/", "").trim());
            return v < min ? nombre + " debe ser " + (min == 0 ? "cero o más" : "mayor o igual a " + pe.erp.sofas.core.Money.numero(min)) + "." : null;
        } catch (NumberFormatException | NullPointerException e) {
            return "Ingresa " + nombre.toLowerCase() + " válido.";
        }
    }

    public static double valor(TextInputControl t) {
        try {
            return Double.parseDouble(t.getText().replace(",", "").replace("S/", "").trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
