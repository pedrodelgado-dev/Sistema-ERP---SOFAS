/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConsumoController
 * Descripción: Consumo real frente al estimado: KPI, rankings, ajuste de fichas con el promedio real y tabla editable.
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
import pe.erp.sofas.model.ConsumoReal;
import pe.erp.sofas.model.FichaTecnica;
import pe.erp.sofas.service.ConsumoService;
import pe.erp.sofas.ui.components.*;

import java.util.*;

/** Consumo real frente al estimado: KPI, rankings, ajuste de fichas con el promedio real y tabla editable. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConsumoController implements Vista {

    private final ConsumoService consumo;
    private final CalculadoraMateriales calculadora;
    private final Toast toast;
    private final ConfirmDialog confirm;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private final VBox kpis = new VBox();
    private ComboBox<String> cmbOp;
    private ComboBox<String> cmbProducto;
    private Button ajustar;
    private final HBarChart trabajadores = new HBarChart().centrado(true);
    private final HBarChart productos = new HBarChart().centrado(true);
    private final TableView<ConsumoReal> tabla = Cells.tabla("Sin consumos registrados con estos filtros.");

    @FXML
    public void initialize() {
        cmbOp = Forms.combo(List.of(), null, s -> s == null ? "Todas las OP" : s);
        cmbOp.setPrefWidth(180);
        cmbProducto = Forms.combo(List.of(), null, s -> s == null ? "Todos los productos" : s);
        cmbProducto.setPrefWidth(240);
        cmbOp.valueProperty().addListener((o, a, b) -> render());
        cmbProducto.valueProperty().addListener((o, a, b) -> render());
        ajustar = UI.primary("Ajustar ficha con el promedio real", "TUNE");
        ajustar.setOnAction(e -> ajustarFicha());
        HBox barra = UI.hbox(10, cmbOp, cmbProducto, UI.label("Edita la columna «Real» para corregir un registro.", "cell-sub"), UI.spacer(), ajustar);
        construirTabla();
        VBox tablaCard = new VBox(tabla);
        tablaCard.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(tabla, Priority.ALWAYS);
        tablaCard.setMinHeight(300);
        VBox ranking1 = UI.card(UI.cardHeader("Eficiencia por trabajador", "Desvío del consumo real · verde si consume menos, rojo si supera +10 %"), trabajadores);
        VBox ranking2 = UI.card(UI.cardHeader("Productos con mayor desvío", "Desvío valorizado por modelo"), productos);
        VBox contenido = new VBox(14, kpis, UI.columnas(new double[]{1, 1}, ranking1, ranking2), barra, tablaCard);
        ScrollPane sp = UI.scroll(contenido);
        VBox.setVgrow(sp, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabs(Modulo.CONSUMO, navigator, calculadora::abrir), sp);
    }

    @Override
    public void onShow(Intent intent) {
        List<ConsumoReal> todos = consumo.registros();
        List<String> ops = new ArrayList<>();
        ops.add(null);
        todos.stream().map(ConsumoReal::getOpCodigo).distinct().sorted(Comparator.reverseOrder()).forEach(ops::add);
        List<String> prods = new ArrayList<>();
        prods.add(null);
        todos.stream().map(ConsumoReal::getProducto).distinct().sorted().forEach(prods::add);
        String op = cmbOp.getValue(), pr = cmbProducto.getValue();
        cmbOp.getItems().setAll(ops);
        cmbProducto.getItems().setAll(prods);
        cmbOp.setValue(op);
        cmbProducto.setValue(pr);
        render();
    }

    private List<ConsumoReal> filtrados() {
        return consumo.filtrar(cmbOp.getValue(), cmbProducto.getValue());
    }

    private void render() {
        List<ConsumoReal> l = filtrados();
        ConsumoService.Kpis k = consumo.kpis(l);
        kpis.getChildren().setAll(UI.fila(
                new KpiCard("COMPARE_ARROWS", k.desvioPromedioPct() > 10 ? "red" : "amber", "Desvío promedio valorizado", String.format("%+.1f %%", k.desvioPromedioPct()),
                        "Real frente a la ficha"),
                new KpiCard("CONTENT_CUT", "red", "Costo de sobremerma", Money.fmt0(k.costoSobremerma()), "Consumo por encima del estimado"),
                new KpiCard("BUILD", "blue", "Órdenes registradas", String.valueOf(k.ordenes()), "Con consumo real cerrado"),
                new KpiCard("WARNING", "wood", "Material con mayor desvío", k.materialMayorDesvio(), String.format("%+.1f %% sobre la ficha", k.desvioMaterialPct()))));
        trabajadores.setFilas(consumo.porTrabajador(consumo.registros()).stream()
                .map(r -> new HBarChart.Fila(r.nombre(), r.desvioPct(), 0, String.format("%+.1f %%", r.desvioPct()), r.desvioPct() > 10)).toList());
        productos.setFilas(consumo.porProducto(consumo.registros()).stream().limit(8)
                .map(r -> new HBarChart.Fila(r.nombre(), r.desvioPct(), 0, String.format("%+.1f %%", r.desvioPct()), r.desvioPct() > 10)).toList());
        tabla.getItems().setAll(l);
        Optional<String> ficha = consumo.fichaComun(l);
        ajustar.setDisable(ficha.isEmpty());
        UI.tooltip(ajustar, ficha.map(f -> "Crea una nueva versión en Borrador de " + f + " con las cantidades × (real / estimado).")
                .orElse("Filtra por un producto u OP para ajustar su ficha."));
    }

    private void construirTabla() {
        TableColumn<ConsumoReal, ConsumoReal> real = Cells.nodo("Real", c -> {
            TextField t = Forms.decimal(c.getRealConsumido());
            t.getStyleClass().add("cell-field");
            t.setPrefWidth(84);
            Runnable commit = () -> {
                double v = Validacion.valor(t);
                if (Math.abs(v - c.getRealConsumido()) > 1e-9) {
                    if (toast.intentar(() -> consumo.editarReal(c.getId(), v))) {
                        toast.exito("Consumo real de " + c.getMaterial().getNombre() + " en " + c.getOpCodigo() + " actualizado.");
                        render();
                    }
                }
            };
            t.setOnAction(e -> commit.run());
            t.focusedProperty().addListener((o, a, b) -> {
                if (!b) commit.run();
            });
            return t;
        }, 100);
        tabla.getColumns().addAll(List.of(
                Cells.texto("OP", ConsumoReal::getOpCodigo, 76, "cell-mono"),
                Cells.texto("Producto", ConsumoReal::getProducto, 170, "cell-strong"),
                Cells.texto("Responsable", ConsumoReal::getResponsable, 150),
                Cells.nodo("Material y fecha", c -> new VBox(0, UI.label(c.getMaterial().getNombre(), "cell-title"),
                        UI.label(c.getMaterial().getCodigo() + " · " + AppClock.fmt(c.getFecha()), "cell-sub")), 190),
                Cells.derecha("Estimado", c -> Money.numero(c.getEstimado()) + " " + c.getMaterial().getUnidad(), 100, "cell-muted"),
                real,
                Cells.derecha("Diferencia", c -> String.format("%+.2f", c.getDiferencia()), 80),
                Cells.nodo("Dif. %", c -> {
                    double p = c.getDiferenciaPct();
                    return new StatusBadge(String.format("%+.1f %%", p), p > 10 ? "red" : p < 0 ? "green" : p > 5 ? "amber" : "gray");
                }, 80)));
        tabla.setFixedCellSize(48);
        tabla.setPrefHeight(48 * 12 + 44);
    }

    private void ajustarFicha() {
        List<ConsumoReal> l = filtrados();
        Optional<String> ficha = consumo.fichaComun(l);
        if (ficha.isEmpty()) return;
        confirm.mostrar(ConfirmDialog.Tipo.INFO, "¿Ajustar " + ficha.get() + " con el promedio real?",
                "Se creará una nueva versión en Borrador con cada cantidad × (real / estimado) de " + l.stream().map(ConsumoReal::getOpCodigo).distinct().count()
                        + " órdenes. La versión aprobada no cambia hasta que apruebes la nueva.", "Crear versión", () -> {
                    FichaTecnica[] f = new FichaTecnica[1];
                    if (toast.intentar(() -> f[0] = consumo.ajustarFicha(ficha.get(), consumo.registros()))) {
                        toast.exito(f[0].getEtiqueta() + " creada en Borrador con el consumo real promedio.");
                        navigator.goTo("fichas", Intent.abrir(f[0].getUid()));
                    }
                });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
