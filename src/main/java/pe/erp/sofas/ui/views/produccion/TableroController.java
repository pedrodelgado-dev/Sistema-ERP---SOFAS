/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: TableroController
 * Descripción: Tablero del taller: KPI, filtros y Kanban de las 11 etapas con drag & drop (o tabla).
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.EmpleadoService;
import pe.erp.sofas.service.ProduccionService;
import pe.erp.sofas.ui.components.*;

import java.util.*;

/** Tablero del taller: KPI, filtros y Kanban de las 11 etapas con drag & drop (o tabla). */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class TableroController implements Vista {

    private final ProduccionService produccion;
    private final EmpleadoService empleados;
    private final OrdenesUI ordenesUI;
    private final CalculadoraMateriales calculadora;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private final VBox kpis = new VBox();
    private ComboBox<Empleado> cmbResp;
    private ComboBox<Prioridad> cmbPrio;
    private SegmentedControl vista;
    private final StackPane contenido = new StackPane();

    @FXML
    public void initialize() {
        List<Empleado> resp = new ArrayList<>();
        resp.add(null);
        resp.addAll(empleados.taller());
        cmbResp = Forms.combo(resp, null, e -> e == null ? "Todos los responsables" : e.getNombre());
        cmbResp.setPrefWidth(230);
        cmbResp.valueProperty().addListener((o, a, b) -> render());
        List<Prioridad> pr = new ArrayList<>();
        pr.add(null);
        pr.addAll(List.of(Prioridad.values()));
        cmbPrio = Forms.combo(pr, null, p -> p == null ? "Todas las prioridades" : "Prioridad " + p.nombre().toLowerCase());
        cmbPrio.setPrefWidth(200);
        cmbPrio.valueProperty().addListener((o, a, b) -> render());
        vista = SegmentedControl.conIconos(new String[]{"Kanban", "Tabla"}, new String[]{"VIEW_WEEK", "TABLE_VIEW"});
        vista.setValor(Prefs.str(Prefs.VISTA_TABLERO, "Kanban"));
        vista.valorProperty().addListener((o, a, b) -> {
            Prefs.setStr(Prefs.VISTA_TABLERO, b);
            render();
        });
        Button nueva = UI.primary("Nueva orden", "ADD");
        nueva.setOnAction(e -> ordenesUI.nueva(this::render));
        HBox barra = UI.hbox(10, cmbResp, cmbPrio, UI.label("Arrastra las tarjetas entre etapas o usa el detalle.", "cell-sub"), UI.spacer(), vista, nueva);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabs(Modulo.PRODUCCION, navigator, calculadora::abrir), kpis, barra, contenido);
    }

    @Override
    public void onShow(Intent intent) {
        render();
        if (intent.es(Intent.ACCION_NUEVO)) ordenesUI.nueva(this::render);
        else if (intent.es(Intent.ACCION_ABRIR) && intent.dato() != null) {
            produccion.porCodigo(intent.dato().toString()).ifPresent(op -> ordenesUI.detalle(op, this::render));
        }
    }

    private void render() {
        if (vista == null) return;
        var k = produccion.kpis();
        kpis.getChildren().setAll(UI.fila(
                new KpiCard("BUILD", "blue", "Órdenes activas", String.valueOf(k.activas()), "Pendientes y en producción"),
                new KpiCard("SCHEDULE", "green", "En tiempo", String.valueOf(k.enTiempo()), "Dentro de la fecha de entrega"),
                new KpiCard("ALARM", "red", "Retrasadas", String.valueOf(k.retrasadas()), "Con la entrega vencida"),
                new KpiCard("DONE_ALL", "teal", "Terminadas este mes", String.valueOf(k.terminadasMes()), "Pasaron a Listo en octubre"),
                new KpiCard("WEEKEND", "wood", "Sofás en proceso", String.valueOf(k.sofasEnProceso()), k.ordenesEnProceso() + " órdenes en etapas del taller")));
        List<OrdenProduccion> ops = produccion.ordenes().stream()
                .filter(o -> cmbResp.getValue() == null || (o.getResponsable() != null && o.getResponsable().getId().equals(cmbResp.getValue().getId())))
                .filter(o -> cmbPrio.getValue() == null || o.getPrioridad() == cmbPrio.getValue())
                .toList();
        if ("Tabla".equals(vista.getValor())) contenido.getChildren().setAll(tabla(ops));
        else contenido.getChildren().setAll(kanban(ops));
    }

    private Node kanban(List<OrdenProduccion> ops) {
        HBox board = new HBox(14);
        board.getStyleClass().add("kanban-board");
        for (Etapa e : Etapa.values()) {
            List<OrdenProduccion> enEtapa = ops.stream().filter(o -> o.getEtapa() == e)
                    .sorted(Comparator.comparing(OrdenProduccion::getPrioridad).thenComparing(o -> o.getFechaEntrega() == null ? AppClock.hoy().plusYears(1) : o.getFechaEntrega()))
                    .toList();
            List<KanbanCard> cards = enEtapa.stream().map(o -> new KanbanCard(o, produccion.indicador(o), () -> ordenesUI.detalle(o, this::render))).toList();
            board.getChildren().add(new KanbanColumn(e, cards, enEtapa.stream().mapToInt(OrdenProduccion::getCantidad).sum(),
                    (id, destino) -> ordenesUI.mover(produccion.op(id), destino, this::render)));
        }
        ScrollPane sp = new ScrollPane(board);
        sp.setFitToHeight(true);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("page-scroll");
        return sp;
    }

    private Node tabla(List<OrdenProduccion> ops) {
        TableView<OrdenProduccion> t = Cells.tabla("Sin órdenes con estos filtros.");
        TableColumn<OrdenProduccion, OrdenProduccion> etapa = Cells.nodo("Etapa", o -> {
            Region punto = new Region();
            punto.getStyleClass().add("legend-dot");
            punto.setStyle("-fx-background-color: " + o.getEtapa().color() + ";");
            return UI.hbox(8, punto, UI.label(o.getEtapa().nombre()));
        }, 170);
        etapa.setSortable(true);
        etapa.setComparator(Comparator.comparing(OrdenProduccion::getEtapa));
        t.getColumns().addAll(List.of(
                Cells.texto("Código", OrdenProduccion::getCodigo, 80, "cell-mono"),
                Cells.texto("Producto", o -> o.getProductoNombre() + " ×" + o.getCantidad(), 220, "cell-strong"),
                Cells.texto("Cliente", OrdenProduccion::getClienteNombre, 180),
                Cells.nodo("Tela", o -> o.getTela() == null ? UI.label("Según ficha", "cell-sub")
                        : UI.hbox(6, new ColorSwatch(o.getTela().getHex(), o.getTela().getTipo(), 16), UI.label(o.getTela().getNombre())), 170),
                etapa,
                Cells.texto("Responsable", o -> o.getResponsable() == null ? "—" : o.getResponsable().getNombre(), 170),
                Cells.texto("Entrega", o -> AppClock.fmt(o.getFechaEntrega()), 90),
                Cells.badge("Prioridad", o -> o.getPrioridad().nombre(), 80),
                Cells.nodo("Materiales", o -> {
                    var i = produccion.indicador(o);
                    return new StatusBadge(i.texto(), i.variante());
                }, 170),
                Cells.badge("Estado", OrdenProduccion::getEstado, 110)));
        t.getItems().setAll(ops);
        t.setRowFactory(tv -> {
            TableRow<OrdenProduccion> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty()) ordenesUI.detalle(r.getItem(), this::render);
            });
            return r;
        });
        VBox card = new VBox(t);
        VBox.setVgrow(t, Priority.ALWAYS);
        card.getStyleClass().addAll("card", "table-card");
        return card;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
