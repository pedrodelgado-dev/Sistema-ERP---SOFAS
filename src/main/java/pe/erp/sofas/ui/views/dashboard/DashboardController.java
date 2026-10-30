/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: DashboardController
 * Descripción: Archivo fuente Java de la clase DashboardController.
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
package pe.erp.sofas.ui.views.dashboard;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.Etapa;
import pe.erp.sofas.model.Pedido;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.components.*;

import java.util.List;
import java.util.Map;

@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class DashboardController implements Vista {

    private final DashboardService dashboard;
    private final ProduccionService produccion;
    private final NotificacionService notificaciones;
    private final Navigator navigator;

    @FXML private VBox contenido;

    @FXML
    public void initialize() {
    }

    @Override
    public void onShow(Intent intent) {
        contenido.getChildren().setAll(kpis(), UI.columnas(new double[]{1.75, 1}, ventas12(), porModelo()),
                UI.columnas(new double[]{1.75, 1}, porEtapa(), alertas()),
                UI.columnas(new double[]{1, 1.45, 0.8}, entregas(), ultimosPedidos(), accesos()));
    }

    private Node kpis() {
        DashboardService.Kpis k = dashboard.kpis();
        KpiCard ventas = new KpiCard("POINT_OF_SALE", "blue", "Ventas del mes", Money.fmt0(k.ventasMes()),
                String.format("%+.0f %% frente a septiembre", k.variacionPct())).tendencia(k.variacionPct() >= 0)
                .alClic(() -> navigator.goTo("ventas"));
        KpiCard vendidos = new KpiCard("WEEKEND", "wood", "Sofás vendidos en el mes", String.valueOf(k.sofasVendidos()),
                "En tienda (POS)").alClic(() -> navigator.goTo("ventas"));
        KpiCard fab = new KpiCard("BUILD", "violet", "Sofás en fabricación", String.valueOf(k.sofasFabricacion()),
                k.ordenesFabricacion() + " órdenes en el taller").alClic(() -> navigator.goTo("produccion"));
        KpiCard ped = new KpiCard("ASSIGNMENT", "amber", "Pedidos a medida pendientes", String.valueOf(k.pedidosPendientes()),
                "Por fabricar o entregar").alClic(() -> navigator.goTo("pedidos"));
        KpiCard tela = new KpiCard("PALETTE", "teal", "Metros de tela en stock", Money.numero(k.metrosTela()) + " m",
                k.colores() + " colores activos").alClic(() -> navigator.goTo("telas"));
        return UI.fila(ventas, vendidos, fab, ped, tela);
    }

    private Node ventas12() {
        List<FinanzasService.Mes> meses = dashboard.ventas12();
        var chart = Charts.lineas(List.of(new Charts.Serie("Ventas", meses.stream().map(FinanzasService.Mes::etiqueta).toList(),
                meses.stream().map(FinanzasService.Mes::ventas).toList())), Money::corto, true);
        chart.setPrefHeight(250);
        chart.setMinHeight(230);
        double total = meses.stream().mapToDouble(FinanzasService.Mes::ventas).sum();
        VBox card = UI.card(UI.cardHeader("Ventas de los últimos 12 meses", "Total " + Money.fmt0(total) + " · tienda"), chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return card;
    }

    private Node porModelo() {
        var seg = dashboard.ventasPorModelo().stream().map(s -> new DonutChart.Segmento(s.nombre(), s.valor(), s.color())).toList();
        double total = seg.stream().mapToDouble(DonutChart.Segmento::valor).sum();
        DonutChart d = new DonutChart(seg, 170, Money.corto(total), "octubre");
        return UI.card(UI.cardHeader("Ventas por modelo", "Participación del mes"), d);
    }

    private Node porEtapa() {
        Map<Etapa, Long> m = produccion.porEtapa();
        CategoryAxis x = new CategoryAxis();
        x.setTickMarkVisible(false);
        long max = m.values().stream().mapToLong(Long::longValue).max().orElse(1);
        NumberAxis y = new NumberAxis(0, max + 1, 1);
        y.setMinorTickVisible(false);
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(10);
        chart.setVerticalGridLinesVisible(false);
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (Etapa e : Etapa.values()) s.getData().add(new XYChart.Data<>(corto(e), m.get(e)));
        chart.getData().add(s);
        int i = 0;
        for (XYChart.Data<String, Number> d : s.getData()) {
            Etapa e = Etapa.values()[i++];
            if (d.getNode() != null) {
                d.getNode().setStyle("-fx-bar-fill: " + e.color() + "; -fx-background-color: " + e.color() + ";");
                Tooltip.install(d.getNode(), new Tooltip(e.nombre() + ": " + d.getYValue() + " sofás"));
                d.getNode().setOnMouseClicked(ev -> navigator.goTo("produccion"));
            }
        }
        chart.setPrefHeight(240);
        return UI.card(UI.cardHeader("Producción por etapa", "Sofás en cada etapa del taller (incluye listos)"), chart);
    }

    private static String corto(Etapa e) {
        return switch (e) {
            case CORTE_MADERA -> "C. madera";
            case ARMADO -> "Armado";
            case CINCHADO -> "Cinchado";
            case CORTE_TELA -> "C. tela";
            case ACABADO -> "Acabado";
            case CONTROL_CALIDAD -> "Calidad";
            default -> e.nombre();
        };
    }

    private Node alertas() {
        VBox lista = new VBox();
        var todas = notificaciones.todas();
        for (var n : todas) {
            StackPane ic = new StackPane(Ico.of(n.icono(), 16));
            ic.getStyleClass().addAll("notif-icon", "notif-" + n.tipo());
            VBox t = new VBox(1, UI.wrap(n.titulo(), "popup-item-title"), UI.wrap(n.detalle(), "popup-item-sub"));
            HBox.setHgrow(t, Priority.ALWAYS);
            HBox fila = new HBox(12, ic, t, Ico.of("CHEVRON_RIGHT", 16));
            fila.getStyleClass().add("alert-row");
            fila.setOnMouseClicked(e -> navigator.goTo(n.modulo(), n.dato() == null ? Intent.NONE : Intent.abrir(n.dato())));
            lista.getChildren().add(fila);
        }
        if (todas.isEmpty()) lista.getChildren().add(UI.empty("DONE_ALL", "Sin alertas", "Todo en orden en el taller y el almacén.", null));
        return UI.card(UI.cardHeader("Alertas", todas.size() + " por atender"), lista);
    }

    private Node entregas() {
        VBox lista = new VBox();
        for (var e : dashboard.proximasEntregas(6)) {
            VBox fecha = new VBox(0, UI.label(AppClock.diaMes(e.fecha()).split(" ")[0], "kpi-value"),
                    UI.label(AppClock.diaMes(e.fecha()).split(" ")[1], "cell-sub"));
            fecha.setAlignment(Pos.CENTER);
            fecha.setMinWidth(46);
            ((javafx.scene.control.Label) fecha.getChildren().get(0)).setStyle("-fx-font-size: 17px;");
            VBox t = new VBox(1, UI.label(e.titulo(), "cell-title"), UI.label(e.cliente() + " · " + e.codigo(), "cell-sub"));
            HBox.setHgrow(t, Priority.ALWAYS);
            HBox fila = new HBox(10, fecha, t, new StatusBadge(e.estado()));
            fila.getStyleClass().add("alert-row");
            fila.setOnMouseClicked(ev -> navigator.goTo(e.modulo(), Intent.abrir(e.codigo())));
            lista.getChildren().add(fila);
        }
        return UI.card(UI.cardHeader("Próximas entregas", "Pedidos con fecha comprometida"), lista);
    }

    private Node ultimosPedidos() {
        TableView<Pedido> t = Cells.tabla("Sin pedidos.");
        t.getColumns().addAll(List.of(
                Cells.texto("Pedido", Pedido::getCodigo, 80, "cell-mono"),
                Cells.texto("Cliente", p -> p.getCliente().getNombre(), 150, "cell-strong"),
                Cells.texto("Modelo", Pedido::getProductoNombre, 150),
                Cells.derecha("Total", p -> Money.fmt0(p.getPrecio()), 80),
                Cells.badge("Estado", p -> p.isRetrasado() ? "Retrasado" : p.getEstado().nombre(), 110)));
        t.getItems().setAll(dashboard.ultimosPedidos(6));
        t.setPrefHeight(46 * 6 + 42);
        t.setRowFactory(tv -> {
            var r = new javafx.scene.control.TableRow<Pedido>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty()) navigator.goTo("pedidos", Intent.abrir(r.getItem().getCodigo()));
            });
            return r;
        });
        VBox card = UI.card(UI.cardHeader("Últimos pedidos", null), t);
        card.getStyleClass().add("table-card");
        card.setStyle("-fx-padding: 16 0 0 0;");
        ((HBox) card.getChildren().get(0)).setStyle("-fx-padding: 0 18 0 18;");
        return card;
    }

    private Node accesos() {
        VBox v = new VBox(10,
                acceso("POINT_OF_SALE", "Nueva venta", "Abrir el POS", () -> navigator.goTo("ventas")),
                acceso("ASSIGNMENT", "Nuevo pedido a medida", "Registrar cliente y medidas", () -> navigator.goTo("pedidos", Intent.NEW)),
                acceso("MDOAL_3D_ROTATION", "Configurar sofá", "Diseñar en 3D", () -> navigator.goTo("configurador")),
                acceso("BUILD", "Nueva orden de producción", "Planificar en el taller", () -> navigator.goTo("produccion", Intent.NEW)));
        return UI.card(UI.cardHeader("Accesos rápidos", null), v);
    }

    private Node acceso(String icono, String titulo, String sub, Runnable r) {
        StackPane ic = new StackPane(Ico.of(icono, 18));
        ic.getStyleClass().addAll("kpi-icon", "kpi-blue");
        HBox h = new HBox(10, ic, new VBox(1, UI.label(titulo, "quick-action-title"), UI.label(sub, "cell-sub")));
        h.getStyleClass().add("quick-action");
        h.setFocusTraversable(true);
        h.setOnMouseClicked(e -> r.run());
        h.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) r.run();
        });
        return h;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
