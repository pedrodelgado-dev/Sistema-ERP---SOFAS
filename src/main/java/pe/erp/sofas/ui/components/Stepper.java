/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Stepper
 * Descripción: Stepper de las 11 etapas del taller con su color e icono; cada paso es clicable.
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import pe.erp.sofas.model.Etapa;

import java.util.function.Consumer;

/** Stepper de las 11 etapas del taller con su color e icono; cada paso es clicable. */
public class Stepper extends HBox {

    public Stepper(Etapa actual, Consumer<Etapa> alElegir) {
        getStyleClass().add("stepper");
        setAlignment(Pos.TOP_CENTER);
        Etapa[] etapas = Etapa.values();
        for (int i = 0; i < etapas.length; i++) {
            Etapa e = etapas[i];
            boolean hecho = e.ordinal() < actual.ordinal();
            boolean activo = e == actual;
            StackPane circulo = new StackPane(hecho ? Ico.of("CHECK", 13) : Ico.of(e.icono(), 13));
            circulo.getStyleClass().add("step-circle");
            if (hecho || activo) {
                circulo.setStyle("-fx-background-color: " + e.color() + "; -fx-border-color: " + e.color() + ";");
                circulo.getStyleClass().add("step-filled");
            }
            if (activo) circulo.getStyleClass().add("step-active");
            var nombre = UI.wrap(e.nombre(), "step-label");
            nombre.setAlignment(Pos.CENTER);
            nombre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            if (activo) nombre.getStyleClass().add("step-label-active");
            VBox paso = new VBox(5, circulo, nombre);
            paso.setAlignment(Pos.TOP_CENTER);
            paso.setPrefWidth(56);
            paso.setMinWidth(44);
            Tooltip.install(paso, new Tooltip((i + 1) + ". " + e.nombre()));
            if (alElegir != null) UI.onClick(paso, ev -> alElegir.accept(e));
            getChildren().add(paso);
            if (i < etapas.length - 1) {
                Region linea = new Region();
                linea.getStyleClass().add("step-line");
                if (e.ordinal() < actual.ordinal()) linea.setStyle("-fx-background-color: " + etapas[i + 1].color() + ";");
                HBox.setHgrow(linea, Priority.ALWAYS);
                linea.setTranslateY(13);
                getChildren().add(linea);
            }
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
