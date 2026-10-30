/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ComprasController
 * Descripción: Ordenes de compra: Borrador → Solicitada → Aprobada → Recibida (suma al stock y al kardex) o Cancelada.
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
package pe.erp.sofas.ui.views.compras;

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
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.components.*;

import java.math.BigDecimal;
import java.util.*;

/** Ordenes de compra: Borrador → Solicitada → Aprobada → Recibida (suma al stock y al kardex) o Cancelada. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ComprasController implements Vista {

    private final CompraService compras;
    private final ProveedorService proveedores;
    private final InventarioService inventario;
    private final TelaService telas;
    private final Overlays overlays;
    private final Toast toast;
    private final ConfirmDialog confirm;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private final VBox kpis = new VBox();
    private final ChipFilterBar chips = new ChipFilterBar();
    private final TableView<OrdenCompra> tabla = Cells.tabla("Sin órdenes de compra con este filtro.");
    private Overlays.Handle panel;

    @FXML
    public void initialize() {
        chips.seleccionProperty().addListener((o, a, b) -> render());
        Button nueva = UI.primary("Nueva orden de compra", "ADD");
        nueva.setOnAction(e -> nueva());
        TableColumn<OrdenCompra, String> total = Cells.derecha("Total", o -> Money.fmt(o.getTotal()), 110, "cell-strong");
        total.setComparator(Comparator.comparing(Money::parse));
        tabla.getColumns().addAll(List.of(
                Cells.nodo("Orden", o -> UI.hbox(6, UI.label(o.getCodigo(), "mono"), Boolean.TRUE.equals(o.getNueva()) ? new StatusBadge("Nueva") : new Region()), 150),
                Cells.texto("Proveedor", o -> o.getProveedor().getRazonSocial(), 210, "cell-strong"),
                Cells.texto("Fecha", o -> AppClock.fmt(o.getFecha()), 90),
                Cells.texto("Materiales", o -> o.getItems().stream().map(i -> i.getMaterial().getNombre()).reduce((a, b) -> a + ", " + b).orElse(""), 260, "cell-muted"),
                Cells.nodo("Origen", o -> new StatusBadge(o.getOrigen(), o.getOrigen().startsWith("OP-") ? "wood" : StatusBadge.variante(o.getOrigen())), 110),
                total,
                Cells.badge("Estado", o -> o.getEstado() == EstadoCompra.APROBADA ? "Aprobada OC" : o.getEstado().nombre(), 100)));
        tabla.setRowFactory(tv -> {
            TableRow<OrdenCompra> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty()) detalle(r.getItem());
            });
            return r;
        });
        VBox card = new VBox(tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        card.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        raiz.getChildren().setAll(kpis, UI.hbox(10, chips, UI.spacer(), nueva), card);
    }

    @Override
    public void onShow(Intent intent) {
        render();
        if (intent.es(Intent.ACCION_ABRIR) && intent.dato() != null) {
            compras.listar().stream().filter(o -> o.getCodigo().equals(intent.dato().toString())).findFirst().ifPresent(this::detalle);
        } else if (intent.es(Intent.ACCION_NUEVO)) nueva();
    }

    private void render() {
        List<OrdenCompra> todas = compras.listar();
        List<ChipFilterBar.Chip> cs = new ArrayList<>();
        cs.add(new ChipFilterBar.Chip("*", "Todas", todas.size()));
        for (EstadoCompra e : EstadoCompra.values()) cs.add(new ChipFilterBar.Chip(e.name(), e.nombre(), todas.stream().filter(o -> o.getEstado() == e).count()));
        chips.setChips(cs);
        double transito = todas.stream().filter(o -> o.getEstado().enTransito()).mapToDouble(o -> o.getTotal().doubleValue()).sum();
        double porPagar = compras.porPagar().stream().mapToDouble(o -> o.getTotal().doubleValue()).sum();
        kpis.getChildren().setAll(UI.fila(
                new KpiCard("SHOPPING_CART", "blue", "Órdenes del mes", String.valueOf(todas.stream().filter(o -> o.getFecha().getMonthValue() == AppClock.hoy().getMonthValue()).count()), "Octubre"),
                new KpiCard("EDIT", "amber", "Por enviar", String.valueOf(todas.stream().filter(o -> o.getEstado() == EstadoCompra.BORRADOR).count()), "En borrador"),
                new KpiCard("LOCAL_SHIPPING", "teal", "En tránsito", Money.fmt0(transito), "Solicitadas y aprobadas"),
                new KpiCard("PAYMENTS", "red", "Por pagar", Money.fmt0(porPagar), "Aprobadas o recibidas sin pago")));
        String sel = chips.getSeleccion();
        tabla.getItems().setAll(todas.stream().filter(o -> sel == null || sel.equals("*") || o.getEstado().name().equals(sel)).toList());
    }

    private void detalle(OrdenCompra o0) {
        compras.marcarVista(o0.getId());
        OrdenCompra o = compras.orden(o0.getId());
        if (panel != null) panel.cerrar();
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(4);
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0, new ColumnConstraints(90), new ColumnConstraints(80), new ColumnConstraints(90));
        g.addRow(0, UI.label("MATERIAL", "dato-label"), UI.label("CANTIDAD", "dato-label"), UI.label("PRECIO", "dato-label"), UI.label("SUBTOTAL", "dato-label"));
        int i = 1;
        for (ItemCompra it : o.getItems()) {
            VBox mat = new VBox(0, UI.label(it.getMaterial().getNombre(), "cell-title"), UI.label(it.getMaterial().getCodigo()
                    + (it.getTela() != null ? " · color " + it.getTela().getColor() : ""), "cell-sub"));
            g.addRow(i++, mat, UI.label(Money.numero(it.getCantidad()) + " " + it.getMaterial().getUnidad(), "cell-sub"),
                    UI.label(Money.fmt(it.getPrecioUnitario()), "cell-sub"), UI.label(Money.fmt(it.getSubtotal()), "cell-title"));
        }
        VBox totales = new VBox(4,
                UI.hbox(8, UI.label("Subtotal", "muted"), UI.spacer(), UI.label(Money.fmt(o.getSubtotal()), "cell-title")),
                UI.hbox(8, UI.label("IGV 18 %", "muted"), UI.spacer(), UI.label(Money.fmt(o.getIgv()), "cell-title")),
                UI.hbox(8, UI.label("Total", "card-title"), UI.spacer(), UI.label(Money.fmt(o.getTotal()), "price-value")));
        VBox cuerpo = new VBox(14, UI.hbox(8, new StatusBadge(o.getEstado().nombre()), new StatusBadge(o.getOrigen(), "wood"), UI.spacer(),
                        UI.label("Emitida " + AppClock.fmt(o.getFecha()) + (o.getFechaRecepcion() != null ? " · recibida " + AppClock.fmt(o.getFechaRecepcion()) : ""), "cell-sub")),
                UI.dato("Proveedor", o.getProveedor().getRazonSocial() + " · RUC " + o.getProveedor().getRuc() + " · " + o.getProveedor().getCondicionPago()), g, totales);
        if (o.getObservaciones() != null) cuerpo.getChildren().add(UI.wrap(o.getObservaciones(), "muted"));
        if (o.getFechaPago() != null) cuerpo.getChildren().add(UI.banner("ok-banner", "PAYMENTS", "Pagada el " + AppClock.fmt(o.getFechaPago()) + "."));
        HBox pie = UI.hbox(8);
        if (o.getEstado() != EstadoCompra.RECIBIDA && o.getEstado() != EstadoCompra.CANCELADA) {
            Button cancelar = UI.button("Cancelar OC", "CANCEL", "btn-danger-soft");
            cancelar.setOnAction(e -> confirm.mostrar("¿Cancelar " + o.getCodigo() + "?", "La orden dejará de contar como compra en tránsito.", "Cancelar OC",
                    () -> accion(() -> compras.cancelar(o.getId()), o.getCodigo() + " cancelada.")));
            pie.getChildren().add(cancelar);
        }
        pie.getChildren().add(UI.spacer());
        switch (o.getEstado()) {
            case BORRADOR -> {
                Button b = UI.primary("Solicitar al proveedor", "SEND");
                b.setOnAction(e -> accion(() -> compras.solicitar(o.getId()), o.getCodigo() + " solicitada a " + o.getProveedor().getRazonSocial() + "."));
                pie.getChildren().add(b);
            }
            case SOLICITADA -> {
                Button b = UI.primary("Aprobar", "VERIFIED");
                b.setOnAction(e -> accion(() -> compras.aprobar(o.getId()), o.getCodigo() + " aprobada."));
                pie.getChildren().add(b);
            }
            case APROBADA -> {
                Button b = UI.success("Recibir en almacén", "MOVE_TO_INBOX");
                b.setOnAction(e -> confirm.mostrar(ConfirmDialog.Tipo.INFO, "¿Recibir " + o.getCodigo() + "?",
                        "Se sumarán " + o.getItems().size() + " materiales al stock y se registrará una Entrada por línea en el kardex.", "Recibir",
                        () -> accion(() -> compras.recibir(o.getId()), o.getCodigo() + " recibida · " + o.getItems().size() + " materiales ingresaron al almacén.")));
                pie.getChildren().add(b);
            }
            default -> {
            }
        }
        if ((o.getEstado() == EstadoCompra.APROBADA || o.getEstado() == EstadoCompra.RECIBIDA) && o.getFechaPago() == null) {
            Button pago = UI.secondary("Registrar pago", "PAYMENTS");
            pago.setOnAction(e -> accion(() -> compras.registrarPago(o.getId()), "Pago de " + o.getCodigo() + " registrado."));
            pie.getChildren().add(pago);
        }
        panel = overlays.panel(o.getCodigo(), o.getProveedor().getRazonSocial(), cuerpo, pie, 560);
    }

    private void accion(Runnable r, String ok) {
        if (toast.intentar(r)) {
            toast.exito(ok);
            if (panel != null) panel.cerrar();
            navigator.datosCambiaron();
            render();
        }
    }

    /** Fila editable de la nueva OC. */
    private final class Fila {
        final ComboBox<Material> material = Forms.combo(inventario.materiales(), null, m -> m.getCodigo() + " · " + m.getNombre());
        final ComboBox<Tela> color = Forms.combo(List.of(), null, Tela::getColor);
        final TextField cantidad = Forms.decimal(10);
        final TextField precio = Forms.decimal(BigDecimal.ZERO);
        final HBox nodo;

        Fila(VBox contenedor, Runnable cambio) {
            color.setPromptText("Color");
            color.setDisable(true);
            color.setPrefWidth(140);
            cantidad.setPrefWidth(80);
            precio.setPrefWidth(90);
            material.valueProperty().addListener((o, a, b) -> {
                if (b == null) return;
                precio.setText(String.valueOf(b.getCosto()));
                color.setDisable(!b.isTela());
                color.getItems().setAll(b.isTela() ? telas.porTipo(TipoTela.porMaterial(b.getCodigo())).stream().filter(t -> !t.getDescontinuada()).toList() : List.of());
                cambio.run();
            });
            cantidad.textProperty().addListener((o, a, b) -> cambio.run());
            precio.textProperty().addListener((o, a, b) -> cambio.run());
            Button quitar = UI.iconButton("DELETE_OUTLINE", "Quitar", "btn-ghost", "btn-xs");
            nodo = UI.hbox(8, material, color, cantidad, precio, quitar);
            HBox.setHgrow(material, Priority.ALWAYS);
            quitar.setOnAction(e -> {
                contenedor.getChildren().remove(nodo);
                filas.remove(this);
                cambio.run();
            });
        }
    }

    private final List<Fila> filas = new ArrayList<>();

    private void nueva() {
        filas.clear();
        ComboBox<Proveedor> proveedor = Forms.combo(proveedores.activos(), null, Proveedor::getRazonSocial);
        VBox lineas = new VBox(8);
        Label total = UI.label("", "price-value");
        CheckBox solicitar = new CheckBox("Enviar la solicitud al proveedor al guardar");
        Runnable recalcular = () -> {
            double s = filas.stream().mapToDouble(f -> Validacion.valor(f.cantidad) * Validacion.valor(f.precio)).sum();
            total.setText("Subtotal " + Money.fmt(s) + " · total con IGV " + Money.fmt(s * 1.18));
        };
        Runnable agregar = () -> {
            Fila f = new Fila(lineas, recalcular);
            filas.add(f);
            lineas.getChildren().add(f.nodo);
            recalcular.run();
        };
        proveedor.valueProperty().addListener((o, a, b) -> {
            if (b == null) return;
            List<Material> suyos = proveedores.materiales(b);
            filas.forEach(f -> f.material.getItems().setAll(suyos.isEmpty() ? inventario.materiales() : suyos));
        });
        Button mas = UI.button("Agregar material", "ADD", "btn-sm", "btn-secondary");
        mas.setOnAction(e -> {
            agregar.run();
            if (proveedor.getValue() != null) {
                List<Material> suyos = proveedores.materiales(proveedor.getValue());
                if (!suyos.isEmpty()) filas.get(filas.size() - 1).material.getItems().setAll(suyos);
            }
        });
        agregar.run();
        VBox cuerpo = new VBox(12, UI.field("Proveedor", proveedor),
                UI.hbox(8, UI.label("MATERIAL", "dato-label"), UI.spacer(), UI.label("COLOR · CANTIDAD · PRECIO S/ (SIN IGV)", "dato-label")), lineas, mas, total, solicitar);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Crear orden", "SAVE");
        var h = overlays.modal("Nueva orden de compra", "Los precios de compra se registran sin IGV", cuerpo, 760, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            OrdenCompra[] oc = new OrdenCompra[1];
            if (proveedor.getValue() == null) {
                toast.error("Elige el proveedor.");
                return;
            }
            List<CompraService.LineaCompra> ls = filas.stream().filter(f -> f.material.getValue() != null)
                    .map(f -> new CompraService.LineaCompra(f.material.getValue().getId(), f.color.getValue() == null ? null : f.color.getValue().getId(),
                            Validacion.valor(f.cantidad), Forms.dinero(f.precio))).toList();
            if (toast.intentar(() -> oc[0] = compras.crear(proveedor.getValue().getId(), ls, solicitar.isSelected(), null, "Manual", true))) {
                h.cerrar();
                toast.exito(oc[0].getCodigo() + " creada en " + oc[0].getEstado().nombre().toLowerCase() + " por " + Money.fmt(oc[0].getTotal()) + ".");
                navigator.datosCambiaron();
                render();
            }
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
