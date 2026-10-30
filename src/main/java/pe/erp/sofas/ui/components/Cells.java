/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Cells
 * Descripción: Columnas de tabla con celdas personalizadas (texto, monto, badge, nodo libre).
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

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.util.function.Function;

/** Columnas de tabla con celdas personalizadas (texto, monto, badge, nodo libre). */
public final class Cells {

    private Cells() {
    }

    /** Columna de texto simple. */
    public static <T> TableColumn<T, String> texto(String titulo, Function<T, String> valor, double ancho, String... clases) {
        TableColumn<T, String> c = new TableColumn<>(titulo.toUpperCase(pe.erp.sofas.core.AppClock.PERU));
        c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(valor.apply(cd.getValue())));
        c.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll(clases);
                if (!empty) getStyleClass().addAll(clases);
            }
        });
        if (ancho > 0) {
            c.setPrefWidth(ancho);
            c.setMinWidth(Math.min(ancho, 72));
        }
        return c;
    }

    /** Columna numerica alineada a la derecha. */
    public static <T> TableColumn<T, String> derecha(String titulo, Function<T, String> valor, double ancho, String... clases) {
        TableColumn<T, String> c = texto(titulo, valor, ancho, clases);
        c.getStyleClass().add("col-right");
        return c;
    }

    /** Columna cuyo contenido es un nodo construido por fila. */
    public static <T> TableColumn<T, T> nodo(String titulo, Function<T, Node> fabrica, double ancho) {
        TableColumn<T, T> c = new TableColumn<>(titulo.toUpperCase(pe.erp.sofas.core.AppClock.PERU));
        c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue()));
        c.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : fabrica.apply(item));
            }
        });
        if (ancho > 0) c.setPrefWidth(ancho);
        c.setSortable(false);
        return c;
    }

    public static <T> TableColumn<T, T> badge(String titulo, Function<T, String> estado, double ancho) {
        return nodo(titulo, t -> new StatusBadge(estado.apply(t)), ancho);
    }

    /** Botones de accion alineados a la derecha. */
    public static <T> TableColumn<T, T> acciones(String titulo, Function<T, Node[]> botones, double ancho) {
        TableColumn<T, T> c = nodo(titulo, t -> {
            HBox h = new HBox(4, botones.apply(t));
            h.setAlignment(Pos.CENTER_RIGHT);
            return h;
        }, ancho);
        c.getStyleClass().add("col-right");
        return c;
    }

    /** Tabla con las clases y comportamiento comun (sin columnas verticales, ancho ajustado). */
    public static <T> TableView<T> tabla(String vacio) {
        TableView<T> t = new TableView<>();
        t.getStyleClass().add("is-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        t.setPlaceholder(UI.empty("SEARCH", "Sin resultados", vacio, null));
        return t;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
