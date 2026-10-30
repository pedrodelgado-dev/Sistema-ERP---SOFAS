/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ProduccionTabs
 * Descripción: Pestanas de Produccion: Tablero · Fichas tecnicas · Requerimiento de materiales · Hojas de corte · Consumo y mermas,
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

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import pe.erp.sofas.core.Modulo;
import pe.erp.sofas.core.Navigator;

/**
 * Pestanas de Produccion: Tablero · Fichas tecnicas · Requerimiento de materiales · Hojas de corte · Consumo y mermas,
 * mas el boton "Calculadora de materiales". Cada pestana es un modulo propio del menu.
 */
public class ProduccionTabs extends HBox {

    public ProduccionTabs(Modulo actual, Navigator navigator, Runnable calculadora) {
        getStyleClass().add("prod-tabs");
        SegmentedControl tabs = new SegmentedControl();
        tabs.getStyleClass().add("tabs");
        String[][] defs = {{"produccion", "Tablero", "VIEW_WEEK"}, {"fichas", "Fichas técnicas", "DESCRIPTION"},
                {"mrp", "Requerimiento de materiales", "FACT_CHECK"}, {"corte", "Hojas de corte", "CONTENT_CUT"},
                {"consumo", "Consumo y mermas", "ANALYTICS"}};
        for (String[] d : defs) tabs.agregar(d[1], d[2]);
        for (String[] d : defs) if (d[0].equals(actual.clave())) tabs.setValor(d[1]);
        tabs.valorProperty().addListener((o, a, b) -> {
            for (String[] d : defs) if (d[1].equals(b) && !d[0].equals(actual.clave())) navigator.goTo(d[0]);
        });
        Button calc = UI.secondary("Calculadora de materiales", "CALCULATE");
        calc.setOnAction(e -> calculadora.run());
        getChildren().addAll(tabs, UI.spacer(), calc);
        setMinHeight(USE_PREF_SIZE);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
