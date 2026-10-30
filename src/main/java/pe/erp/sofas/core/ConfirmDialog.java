/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConfirmDialog
 * Descripción: Modal centrado con icono, titulo, texto y botones "Cancelar" y la accion. Enter confirma, Esc cancela.
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
package pe.erp.sofas.core;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.ui.components.Ico;
import pe.erp.sofas.ui.components.UI;

/** Modal centrado con icono, titulo, texto y botones "Cancelar" y la accion. Enter confirma, Esc cancela. */
@Component
@Lazy
@RequiredArgsConstructor
public class ConfirmDialog {

    public enum Tipo { PELIGRO, AVISO, INFO }

    private final Overlays overlays;

    public void mostrar(String titulo, String mensaje, String accion, Runnable alConfirmar) {
        mostrar(Tipo.PELIGRO, titulo, mensaje, accion, alConfirmar);
    }

    public void mostrar(Tipo tipo, String titulo, String mensaje, String accion, Runnable alConfirmar) {
        String icono = switch (tipo) {
            case PELIGRO -> "WARNING";
            case AVISO -> "REPORT_PROBLEM";
            case INFO -> "HELP_OUTLINE";
        };
        StackPane ic = new StackPane(Ico.of(icono, 26));
        ic.getStyleClass().addAll("confirm-icon", "confirm-" + tipo.name().toLowerCase());
        VBox cuerpo = new VBox(12, ic, UI.label(titulo, "confirm-title"), UI.wrap(mensaje, "confirm-text"));
        cuerpo.setAlignment(Pos.CENTER);
        cuerpo.getStyleClass().add("confirm-body");

        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = tipo == Tipo.PELIGRO ? UI.danger(accion, null) : UI.primary(accion, null);
        ok.setDefaultButton(true);
        Overlays.Handle h = overlays.modal(null, null, cuerpo, 440, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            h.cerrar();
            alConfirmar.run();
        });
        cancelar.setCancelButton(true);
        javafx.application.Platform.runLater(ok::requestFocus);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
