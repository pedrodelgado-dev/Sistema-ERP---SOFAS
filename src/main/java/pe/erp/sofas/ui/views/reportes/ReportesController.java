/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ReportesController
 * Descripción: Reportes con rango de fechas, vista previa en tabla y exportacion a PDF y Excel (en segundo plano).
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
package pe.erp.sofas.ui.views.reportes;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.service.ExportService;
import pe.erp.sofas.service.ReporteService;
import pe.erp.sofas.service.Tabla;
import pe.erp.sofas.ui.AppWindow;
import pe.erp.sofas.ui.components.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/** Reportes con rango de fechas, vista previa en tabla y exportacion a PDF y Excel (en segundo plano). */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ReportesController implements Vista {

    private final ReporteService reportes;
    private final ExportService export;
    private final Overlays overlays;
    private final Toast toast;
    private final AppWindow appWindow;

    @FXML private VBox raiz;

    private final TilePane tarjetas = new TilePane(16, 16);

    @FXML
    public void initialize() {
        tarjetas.setPrefTileWidth(360);
        tarjetas.setTileAlignment(Pos.TOP_LEFT);
        ScrollPane sp = UI.scroll(tarjetas);
        VBox.setVgrow(sp, Priority.ALWAYS);
        raiz.getChildren().setAll(UI.label("Elige el rango de fechas de cada reporte. Vista previa antes de exportar; la exportación corre en segundo plano.", "cell-sub"), sp);
    }

    @Override
    public void onShow(Intent intent) {
        tarjetas.getChildren().clear();
        for (ReporteService.Tipo t : ReporteService.Tipo.values()) tarjetas.getChildren().add(tarjeta(t));
    }

    private Node tarjeta(ReporteService.Tipo t) {
        LocalDate hoy = AppClock.hoy();
        boolean historico = t == ReporteService.Tipo.METROS_TELA || t == ReporteService.Tipo.COSTO_ESTANDAR_REAL || t == ReporteService.Tipo.MERMAS
                || t == ReporteService.Tipo.PRODUCCION_TAPICERO;
        DatePicker desde = Forms.fecha(historico ? hoy.minusMonths(1).withDayOfMonth(1) : hoy.withDayOfMonth(1));
        DatePicker hasta = Forms.fecha(hoy);
        StackPane ic = new StackPane(Ico.of(t.icono(), 20));
        ic.getStyleClass().addAll("kpi-icon", "kpi-blue");
        Button ver = UI.button("Vista previa", "VISIBILITY", "btn-sm", "btn-secondary");
        Button pdf = UI.button("PDF", "PICTURE_AS_PDF", "btn-sm", "btn-secondary");
        Button xls = UI.button("Excel", "TABLE_VIEW", "btn-sm", "btn-secondary");
        ver.setOnAction(e -> vista(t, desde.getValue(), hasta.getValue()));
        pdf.setOnAction(e -> exportar(t, desde.getValue(), hasta.getValue(), true));
        xls.setOnAction(e -> exportar(t, desde.getValue(), hasta.getValue(), false));
        GridPane fechas = UI.grid(2);
        fechas.add(UI.field("Desde", desde), 0, 0);
        fechas.add(UI.field("Hasta", hasta), 1, 0);
        VBox v = UI.card(UI.hbox(12, ic, new VBox(2, UI.label(t.nombre(), "card-title"), UI.wrap(t.descripcion(), "cell-sub"))), fechas, UI.hbox(8, ver, UI.spacer(), pdf, xls));
        v.setPrefWidth(360);
        return v;
    }

    private Tabla generar(ReporteService.Tipo t, LocalDate d, LocalDate h) {
        if (d == null || h == null || h.isBefore(d)) throw new NegocioException("Revisa el rango de fechas.");
        return reportes.generar(t, d, h);
    }

    private void vista(ReporteService.Tipo t, LocalDate d, LocalDate h) {
        Tabla[] tabla = new Tabla[1];
        if (!toast.intentar(() -> tabla[0] = generar(t, d, h))) return;
        TableView<List<String>> tv = new TableView<>();
        tv.getStyleClass().add("is-table");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tv.setPlaceholder(UI.empty("INSERT_CHART", "Sin datos", "No hay registros en el rango elegido.", null));
        for (int i = 0; i < tabla[0].columnas().size(); i++) {
            int idx = i;
            TableColumn<List<String>, String> c = new TableColumn<>(tabla[0].columnas().get(i).toUpperCase(AppClock.PERU));
            c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(idx < cd.getValue().size() ? cd.getValue().get(idx) : ""));
            if (tabla[0].derecha() != null && tabla[0].derecha()[i]) c.getStyleClass().add("col-right");
            tv.getColumns().add(c);
        }
        tv.getItems().setAll(tabla[0].filas());
        if (tabla[0].totales() != null) tv.getItems().add(tabla[0].totales());
        tv.setPrefHeight(Math.min(520, 44 * (tv.getItems().size() + 1) + 10));
        Button pdf = UI.secondary("Exportar PDF", "PICTURE_AS_PDF");
        Button xls = UI.secondary("Exportar Excel", "TABLE_VIEW");
        Button cerrar = UI.primary("Cerrar", null);
        var hnd = overlays.modal(tabla[0].titulo(), tabla[0].subtitulo() + " · " + tabla[0].filas().size() + " filas", tv, 1000, pdf, xls, UI.spacer(), cerrar);
        pdf.setOnAction(e -> exportar(t, d, h, true));
        xls.setOnAction(e -> exportar(t, d, h, false));
        cerrar.setOnAction(e -> hnd.cerrar());
    }

    private void exportar(ReporteService.Tipo t, LocalDate d, LocalDate h, boolean pdf) {
        File f = Archivos.guardar(tarjetas, t.nombre().replace(" ", "-") + "-" + AppClock.hoy(), pdf ? "PDF" : "Excel", pdf ? "pdf" : "xlsx");
        if (f == null) return;
        Tareas.ejecutar(overlays, toast, pdf ? "Generando PDF…" : "Generando Excel…", () -> {
            Tabla tabla = generar(t, d, h);
            if (pdf) export.pdf(tabla, f);
            else export.excel(tabla, f);
            return f;
        }, a -> {
            toast.exito(t.nombre() + " exportado: " + a.getName());
            appWindow.hostServices().showDocument(a.toURI().toString());
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
