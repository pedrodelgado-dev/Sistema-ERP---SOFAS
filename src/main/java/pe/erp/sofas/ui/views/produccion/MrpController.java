/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: MrpController
 * Descripción: Requerimiento de materiales: requerido, stock, transito y faltante neto, con generacion de OC por proveedor.
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
package pe.erp.sofas.ui.views.produccion;

import javafx.concurrent.Task;
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
import pe.erp.sofas.model.Material;
import pe.erp.sofas.model.Proveedor;
import pe.erp.sofas.service.MrpService;
import pe.erp.sofas.service.ProveedorService;
import pe.erp.sofas.ui.components.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Requerimiento de materiales: requerido, stock, transito y faltante neto, con generacion de OC por proveedor. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class MrpController implements Vista {

    private final MrpService mrp;
    private final ProveedorService proveedores;
    private final CalculadoraMateriales calculadora;
    private final Toast toast;
    private final ConfirmDialog confirm;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private final VBox kpis = new VBox();
    private DatePicker desde, hasta;
    private ComboBox<String> categoria;
    private ComboBox<Proveedor> proveedor;
    private CheckBox soloFaltantes;
    private final Label nota = UI.label("", "cell-sub");
    private final HBarChart grafico = new HBarChart().colores("#14213D", "#16A34A").leyenda("Requerido", "Stock + tránsito").porFila(true);
    private final TableView<MrpService.Fila> tabla = Cells.tabla("Sin materiales requeridos con estos filtros.");
    private final StackPane tablaHost = new StackPane();
    private MrpService.Resultado ultimo;
    private int consulta;

    @FXML
    public void initialize() {
        desde = Forms.fecha(null);
        desde.setPromptText("Entrega desde");
        hasta = Forms.fecha(null);
        hasta.setPromptText("Entrega hasta");
        desde.setPrefWidth(150);
        hasta.setPrefWidth(150);
        List<String> cats = new ArrayList<>();
        cats.add(null);
        cats.addAll(Material.CATEGORIAS);
        categoria = Forms.combo(cats, null, c -> c == null ? "Todas las categorías" : c);
        categoria.setPrefWidth(190);
        List<Proveedor> ps = new ArrayList<>();
        ps.add(null);
        ps.addAll(proveedores.listar());
        proveedor = Forms.combo(ps, null, p -> p == null ? "Todos los proveedores" : p.getRazonSocial());
        proveedor.setPrefWidth(220);
        soloFaltantes = new CheckBox("Solo faltantes");
        for (var p : List.of(desde.valueProperty(), hasta.valueProperty(), categoria.valueProperty(), proveedor.valueProperty())) p.addListener((o, a, b) -> calcular());
        soloFaltantes.selectedProperty().addListener((o, a, b) -> calcular());
        Button generar = UI.primary("Generar órdenes de compra", "SHOPPING_CART");
        generar.setOnAction(e -> generar());
        HBox barra = UI.hbox(10, desde, hasta, categoria, proveedor, soloFaltantes, UI.spacer(), generar);
        construirTabla();
        VBox tablaCard = new VBox(tablaHost);
        tablaCard.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(tablaHost, Priority.ALWAYS);
        VBox graf = UI.card(UI.cardHeader("Materiales críticos", "Los 10 de mayor faltante valorizado"), grafico);
        HBox cuerpo = new HBox(14, graf, tablaCard);
        graf.setPrefWidth(350);
        graf.setMinWidth(340);
        graf.setMaxWidth(350);
        HBox.setHgrow(tablaCard, Priority.ALWAYS);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabs(Modulo.MRP, navigator, calculadora::abrir), kpis, barra, nota, cuerpo);
    }

    @Override
    public void onShow(Intent intent) {
        calcular();
    }

    /** El calculo corre en un Task; la tabla muestra un indicador mientras tanto. */
    private void calcular() {
        int token = ++consulta;
        MrpService.Filtro f = new MrpService.Filtro(desde.getValue(), hasta.getValue(), categoria.getValue(),
                proveedor.getValue() == null ? null : proveedor.getValue().getId(), soloFaltantes.isSelected());
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(40, 40);
        tablaHost.getChildren().setAll(tabla, cubierta(pi));
        Task<Object[]> t = new Task<>() {
            @Override
            protected Object[] call() {
                return new Object[]{mrp.calcular(f), mrp.comprasEnTransito()};
            }
        };
        t.setOnSucceeded(e -> {
            if (token != consulta) return;
            mostrar((MrpService.Resultado) t.getValue()[0], (Long) t.getValue()[1]);
        });
        t.setOnFailed(e -> toast.error("No se pudo calcular el requerimiento: " + t.getException().getMessage()));
        Thread h = new Thread(t, "mrp");
        h.setDaemon(true);
        h.start();
    }

    private Node cubierta(Node n) {
        StackPane s = new StackPane(n);
        s.getStyleClass().add("overlay-busy");
        return s;
    }

    private void mostrar(MrpService.Resultado r, long transito) {
        ultimo = r;
        kpis.getChildren().setAll(UI.fila(
                new KpiCard("FACT_CHECK", "blue", "Materiales requeridos", String.valueOf(r.filas().size()), "Por las órdenes y pedidos"),
                new KpiCard("REPORT_PROBLEM", "red", "Con faltante", String.valueOf(r.conFaltante()), "Faltante neto mayor a cero"),
                new KpiCard("ACCOUNT_BALANCE_WALLET", "amber", "Valor del faltante", Money.fmt0(r.valorFaltante()), "A costo unitario"),
                new KpiCard("LOCAL_SHIPPING", "teal", "Compras en tránsito", String.valueOf(transito), "OC solicitadas y aprobadas")));
        nota.setText(r.ops() + " OP activas + " + r.pedidos() + (r.pedidos() == 1 ? " pedido confirmado" : " pedidos confirmados")
                + " sin orden · faltante = máx(0, requerido − stock − tránsito)");
        List<HBarChart.Fila> top = r.filas().stream().sorted(Comparator.comparingDouble(MrpService.Fila::valorFaltante).reversed()
                        .thenComparing(f -> -f.requerido() / Math.max(0.01, f.stock() + f.transito()))).limit(10)
                .map(f -> new HBarChart.Fila(f.material().getNombre(), f.requerido(), f.stock() + f.transito(),
                        f.faltante() > 1e-6 ? "falta " + Money.numero(f.faltante()) + " " + f.material().getUnidad() : "cubierto", f.faltante() > 1e-6))
                .toList();
        grafico.setFilas(top);
        tabla.getItems().setAll(r.filas());
        tablaHost.getChildren().setAll(tabla);
    }

    private void construirTabla() {
        TableColumn<MrpService.Fila, String> falt = Cells.derecha("Faltante neto", f -> Money.numero(f.faltante()), 100);
        falt.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                MrpService.Fila f = getTableRow().getItem();
                setGraphic(f.faltante() > 1e-6 ? new StatusBadge(Money.numero(f.faltante()) + " " + f.material().getUnidad(), "red")
                        : new StatusBadge("Cubierto", "green"));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        falt.setComparator(Comparator.comparingDouble(s -> Double.parseDouble(s.replace(",", ""))));
        tabla.getColumns().addAll(List.of(
                Cells.texto("Código", f -> f.material().getCodigo(), 66, "cell-mono"),
                Cells.texto("Material", f -> f.material().getNombre(), 200, "cell-strong"),
                num("Requerido", MrpService.Fila::requerido),
                num("Stock", MrpService.Fila::stock),
                num("Reservado", MrpService.Fila::reservado),
                num("Disponible", MrpService.Fila::disponible),
                num("Tránsito", MrpService.Fila::transito),
                falt,
                Cells.texto("Se necesita", f -> AppClock.fmt(f.necesitaEl()), 90),
                Cells.texto("Proveedor sugerido", f -> f.proveedor() == null ? "—" : f.proveedor().getRazonSocial(), 170, "cell-muted")));
        tabla.setRowFactory(tv -> {
            TableRow<MrpService.Fila> r = new TableRow<>();
            Tooltip tip = new Tooltip();
            r.itemProperty().addListener((o, a, b) -> {
                if (b != null) {
                    tip.setText("Origen: " + String.join(", ", b.origenes()));
                    r.setTooltip(tip);
                }
            });
            r.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !r.isEmpty()) navigator.goTo("inventario", Intent.abrir(r.getItem().material().getCodigo()));
            });
            return r;
        });
    }

    private TableColumn<MrpService.Fila, String> num(String titulo, java.util.function.ToDoubleFunction<MrpService.Fila> f) {
        TableColumn<MrpService.Fila, String> c = Cells.derecha(titulo, x -> Money.numero(f.applyAsDouble(x)), 74);
        c.setComparator(Comparator.comparingDouble(s -> Double.parseDouble(s.replace(",", ""))));
        return c;
    }

    private void generar() {
        if (ultimo == null) return;
        List<MrpService.Fila> conFaltante = ultimo.filas().stream().filter(f -> f.faltante() > 1e-6).toList();
        if (conFaltante.isEmpty()) {
            toast.info("No hay faltantes: el requerimiento está cubierto por el stock y las compras en tránsito.");
            return;
        }
        long proveedoresN = conFaltante.stream().map(MrpService.Fila::proveedor).distinct().count();
        confirm.mostrar(ConfirmDialog.Tipo.INFO, "¿Generar " + proveedoresN + " órdenes de compra?",
                "Se creará una OC en Borrador por proveedor con " + conFaltante.size() + " materiales, redondeando a la unidad de compra "
                        + "(madera de 50 en 50 pt, metros de 10 en 10, unidades de 10 en 10, kg de 5 en 5).", "Generar", () -> {
                    List<String>[] c = new List[1];
                    if (toast.intentar(() -> c[0] = mrp.generarOrdenes(conFaltante, "MRP"))) {
                        toast.exito("Órdenes de compra creadas en Borrador: " + String.join(", ", c[0]) + ".");
                        navigator.datosCambiaron();
                        calcular();
                    }
                });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
