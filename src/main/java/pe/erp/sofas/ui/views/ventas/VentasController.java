/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: VentasController
 * Descripción: Punto de venta: catalogo con stock, carrito, descuento, IGV incluido, boleta o factura y metodos de pago.
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
package pe.erp.sofas.ui.views.ventas;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
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
import pe.erp.sofas.service.calculo.Igv;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.Render3D;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Punto de venta: catalogo con stock, carrito, descuento, IGV incluido, boleta o factura y metodos de pago. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class VentasController implements Vista {

    private static final class Item {
        final Producto producto;
        int cantidad;

        Item(Producto p) {
            this.producto = p;
            this.cantidad = 1;
        }

        BigDecimal subtotal() {
            return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private static final List<String> METODOS = List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin", "Mixto");
    private static final Map<String, String> ICONOS = Map.of("Efectivo", "PAYMENTS", "Tarjeta", "CREDIT_CARD", "Transferencia", "ACCOUNT_BALANCE",
            "Yape", "QR_CODE_2", "Plin", "SMARTPHONE", "Mixto", "CALL_SPLIT");

    private final ProductoService productos;
    private final ClienteService clientes;
    private final VentaService ventas;
    private final ConfiguracionService configuracion;
    private final Overlays overlays;
    private final Toast toast;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private SegmentedControl modo;
    private SearchField buscador;
    private final ChipFilterBar chips = new ChipFilterBar();
    private final TilePane grid = new TilePane(12, 12);
    private final StackPane contenido = new StackPane();
    private final VBox historial = new VBox(10);
    private final List<Item> carrito = new ArrayList<>();
    private final VBox carritoBox = new VBox();
    private final VBox totales = new VBox(6);
    private ComboBox<Cliente> cliente;
    private SegmentedControl comprobante;
    private SegmentedControl tipoDescuento;
    private TextField descuento;
    private final ToggleGroup metodos = new ToggleGroup();
    private final VBox detallePago = new VBox(8);
    private TextField recibido, monto1, monto2;
    private ComboBox<String> metodo2;
    private Button cobrar;
    private final Label errorCobro = UI.label("", "field-error");

    @FXML
    public void initialize() {
        modo = SegmentedControl.conIconos(new String[]{"Vender", "Historial de ventas"}, new String[]{"POINT_OF_SALE", "RECEIPT_LONG"});
        modo.valorProperty().addListener((o, a, b) -> mostrarModo());
        buscador = new SearchField("Buscar modelo o código");
        buscador.setPrefWidth(260);
        buscador.textProperty().addListener((o, a, b) -> renderGrid());
        chips.seleccionProperty().addListener((o, a, b) -> renderGrid());
        grid.setPrefTileWidth(196);
        grid.setTileAlignment(Pos.TOP_LEFT);
        ScrollPane spGrid = UI.scroll(grid);
        VBox vender = new VBox(10, UI.hbox(10, buscador, UI.spacer()), chips, spGrid);
        VBox.setVgrow(spGrid, Priority.ALWAYS);
        contenido.getChildren().setAll(vender, historial);
        historial.setVisible(false);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        VBox izq = new VBox(12, modo, contenido);
        HBox.setHgrow(izq, Priority.ALWAYS);
        VBox der = construirCarrito();
        HBox cuerpo = new HBox(16, izq, der);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        raiz.getChildren().setAll(cuerpo);
    }

    @Override
    public void onShow(Intent intent) {
        List<Producto> todos = productos.listar();
        List<ChipFilterBar.Chip> cs = new ArrayList<>();
        cs.add(new ChipFilterBar.Chip("*", "Todos", todos.size()));
        for (String c : Producto.CATEGORIAS) {
            long n = todos.stream().filter(p -> p.getCategoria().equals(c)).count();
            if (n > 0) cs.add(new ChipFilterBar.Chip(c, c, n));
        }
        chips.setChips(cs);
        Cliente sel = cliente.getValue();
        List<Cliente> lista = new ArrayList<>();
        lista.add(null);
        lista.addAll(clientes.listar());
        cliente.getItems().setAll(lista);
        cliente.setValue(sel);
        if (intent.es("AGREGAR") && intent.dato() != null) {
            modo.setValor("Vender");
            productos.porCodigo(intent.dato().toString()).ifPresent(this::agregar);
        }
        if (intent.es(Intent.ACCION_NUEVO) && intent.dato() instanceof Cliente c) {
            modo.setValor("Vender");
            cliente.setValue(cliente.getItems().stream().filter(x -> x != null && x.getId().equals(c.getId())).findFirst().orElse(null));
        }
        renderGrid();
        renderCarrito();
        if ("Historial de ventas".equals(modo.getValor())) renderHistorial();
    }

    private void mostrarModo() {
        boolean hist = "Historial de ventas".equals(modo.getValor());
        contenido.getChildren().get(0).setVisible(!hist);
        historial.setVisible(hist);
        if (hist) renderHistorial();
    }

    // ================================================================ catalogo

    private void renderGrid() {
        String q = BusquedaService.normalizar(buscador.getText());
        String cat = chips.getSeleccion();
        grid.getChildren().clear();
        for (Producto p : productos.listar()) {
            if (cat != null && !cat.equals("*") && !p.getCategoria().equals(cat)) continue;
            if (!q.isEmpty() && !BusquedaService.normalizar(p.getNombre() + " " + p.getCodigo()).contains(q)) continue;
            grid.getChildren().add(tarjeta(p));
        }
    }

    private int enCarrito(Producto p) {
        return carrito.stream().filter(i -> i.producto.getId().equals(p.getId())).mapToInt(i -> i.cantidad).sum();
    }

    private Node tarjeta(Producto p) {
        ImageSlot img = new ImageSlot("Foto: " + p.getNombre(), 180, 116, 10).soloLectura();
        if (p.getImagenPrincipal() == null) img.respaldo(() -> Render3D.modelo(p));
        img.mostrar(p.getImagenPrincipal());
        img.setMouseTransparent(true);
        int disponible = p.getStock() - enCarrito(p);
        Label stock = UI.label(p.getStock() <= 0 ? "Sin stock" : "Stock " + p.getStock(), "cell-sub");
        if (p.getStock() <= 0) stock.setStyle("-fx-text-fill: -is-danger-ink;");
        VBox v = new VBox(6, img, UI.label(p.getNombre(), "cell-title"), UI.hbox(6, UI.label(Money.fmt0(p.getPrecio()), "product-price"), UI.spacer(), stock));
        StackPane card = new StackPane(v);
        card.getStyleClass().add("pos-product");
        int en = enCarrito(p);
        if (en > 0) {
            Label q = UI.label(String.valueOf(en), "cart-qty");
            StackPane.setAlignment(q, Pos.TOP_RIGHT);
            card.getChildren().add(q);
        }
        if (p.getStock() <= 0) {
            card.getStyleClass().add("pos-product-disabled");
            Tooltip.install(card, new Tooltip("Sin stock: créalo como pedido a medida."));
        } else {
            card.setOnMouseClicked(e -> {
                if (disponible <= 0) toast.advertencia("No hay más stock de " + p.getNombre() + ".");
                else agregar(p);
            });
        }
        return card;
    }

    private void agregar(Producto p) {
        if (p.getStock() - enCarrito(p) <= 0) {
            toast.advertencia(p.getNombre() + " no tiene stock disponible.");
            return;
        }
        carrito.stream().filter(i -> i.producto.getId().equals(p.getId())).findFirst().ifPresentOrElse(i -> i.cantidad++, () -> carrito.add(new Item(p)));
        renderCarrito();
        renderGrid();
    }

    // ================================================================ carrito

    private VBox construirCarrito() {
        cliente = Forms.combo(List.of(), null, c -> c == null ? "Cliente varios" : c.getNombre() + " · " + c.getTipoDocumento() + " " + c.getDocumento());
        cliente.valueProperty().addListener((o, a, b) -> renderCarrito());
        comprobante = new SegmentedControl("Boleta", "Factura");
        comprobante.valorProperty().addListener((o, a, b) -> renderCarrito());
        tipoDescuento = new SegmentedControl("%", "S/");
        tipoDescuento.valorProperty().addListener((o, a, b) -> renderCarrito());
        descuento = Forms.decimal(0);
        descuento.setPrefWidth(90);
        descuento.textProperty().addListener((o, a, b) -> renderCarrito());
        FlowPane met = new FlowPane(6, 6);
        for (String m : METODOS) {
            ToggleButton t = new ToggleButton(m);
            t.setGraphic(Ico.of(ICONOS.get(m), 18));
            t.getStyleClass().add("pay-method");
            t.setPrefWidth(104);
            t.setUserData(m);
            t.setToggleGroup(metodos);
            met.getChildren().add(t);
        }
        metodos.selectToggle(metodos.getToggles().get(0));
        metodos.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null) metodos.selectToggle(a);
            renderPago();
            renderCarrito();
        });
        recibido = Forms.decimal(BigDecimal.ZERO);
        recibido.textProperty().addListener((o, a, b) -> renderCarrito());
        monto1 = Forms.decimal(BigDecimal.ZERO);
        monto2 = Forms.decimal(BigDecimal.ZERO);
        monto1.textProperty().addListener((o, a, b) -> renderCarrito());
        monto2.textProperty().addListener((o, a, b) -> renderCarrito());
        metodo2 = Forms.combo(List.of("Yape", "Plin", "Tarjeta", "Transferencia"), "Yape", s -> s);
        cobrar = UI.button("Cobrar", "POINT_OF_SALE", "btn-primary", "btn-charge");
        cobrar.setMaxWidth(Double.MAX_VALUE);
        cobrar.setOnAction(e -> cobrar());
        Button vaciar = UI.button("Vaciar", "DELETE_OUTLINE", "btn-ghost", "btn-sm");
        vaciar.setOnAction(e -> {
            carrito.clear();
            renderCarrito();
            renderGrid();
        });
        ScrollPane spItems = UI.scroll(carritoBox);
        spItems.setPrefHeight(200);
        VBox.setVgrow(spItems, Priority.ALWAYS);
        renderPago();
        VBox v = new VBox(12, UI.hbox(8, UI.label("Carrito", "card-title"), UI.spacer(), vaciar), UI.field("Cliente", cliente), spItems,
                UI.hbox(8, UI.label("Descuento", "form-label"), UI.spacer(), tipoDescuento, descuento), totales,
                UI.hbox(8, UI.label("Comprobante", "form-label"), UI.spacer(), comprobante), met, detallePago, errorCobro, cobrar);
        v.getStyleClass().add("cart-panel");
        v.setPadding(new Insets(16));
        v.setPrefWidth(380);
        v.setMinWidth(360);
        v.setMaxWidth(390);
        return v;
    }

    private String metodo() {
        return metodos.getSelectedToggle() == null ? "Efectivo" : (String) metodos.getSelectedToggle().getUserData();
    }

    private void renderPago() {
        detallePago.getChildren().clear();
        switch (metodo()) {
            case "Efectivo" -> detallePago.getChildren().add(UI.field("Monto recibido (S/)", recibido));
            case "Mixto" -> {
                GridPane g = UI.grid(2);
                g.add(UI.field("Efectivo (S/)", monto1), 0, 0);
                g.add(UI.field("Monto (S/)", monto2), 1, 0);
                g.add(UI.field("Segundo método", metodo2), 0, 1, 2, 1);
                detallePago.getChildren().add(g);
            }
            default -> detallePago.getChildren().add(UI.label("Se registra el pago con " + metodo().toLowerCase() + " por el total.", "cell-sub"));
        }
    }

    private BigDecimal bruto() {
        return carrito.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal descuentoSoles() {
        BigDecimal v = Forms.dinero(descuento);
        if (v == null) return BigDecimal.ZERO;
        if ("%".equals(tipoDescuento.getValor())) return bruto().multiply(v).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return v;
    }

    private BigDecimal total() {
        return bruto().subtract(descuentoSoles()).max(BigDecimal.ZERO);
    }

    private void renderCarrito() {
        if (carritoBox == null || cliente == null) return;
        carritoBox.getChildren().clear();
        if (carrito.isEmpty()) {
            carritoBox.getChildren().add(UI.empty("SHOPPING_CART", "Carrito vacío", "Haz clic en un modelo para agregarlo.", null));
        }
        for (Item i : new ArrayList<>(carrito)) {
            Button menos = UI.iconButton("REMOVE", "Quitar uno", "btn-ghost", "btn-xs");
            Button mas = UI.iconButton("ADD", "Agregar uno", "btn-ghost", "btn-xs");
            Button quitar = UI.iconButton("DELETE_OUTLINE", "Eliminar del carrito", "btn-ghost", "btn-xs");
            mas.setDisable(i.cantidad >= i.producto.getStock());
            menos.setOnAction(e -> {
                i.cantidad--;
                if (i.cantidad <= 0) carrito.remove(i);
                renderCarrito();
                renderGrid();
            });
            mas.setOnAction(e -> {
                i.cantidad++;
                renderCarrito();
                renderGrid();
            });
            quitar.setOnAction(e -> {
                carrito.remove(i);
                renderCarrito();
                renderGrid();
            });
            HBox qty = UI.hbox(2, menos, UI.label(String.valueOf(i.cantidad), "qty-value"), mas);
            qty.getStyleClass().add("qty-box");
            HBox fila = UI.hbox(8, new VBox(1, UI.label(i.producto.getNombre(), "cell-title"), UI.label(Money.fmt(i.producto.getPrecio()) + " c/u", "cell-sub")),
                    UI.spacer(), qty, UI.label(Money.fmt0(i.subtotal()), "cell-title"), quitar);
            fila.getStyleClass().add("cart-item");
            carritoBox.getChildren().add(fila);
        }
        BigDecimal total = total();
        BigDecimal sub = Igv.subtotal(total);
        totales.getChildren().setAll(
                fila("Subtotal (sin IGV)", Money.fmt(sub)),
                fila("IGV 18 % (incluido)", Money.fmt(total.subtract(sub))),
                fila("Descuento", "− " + Money.fmt(descuentoSoles())),
                UI.hbox(8, UI.label("Total", "card-title"), UI.spacer(), UI.label(Money.fmt(total), "grand-total")));
        String error = validar();
        errorCobro.setText(error == null ? "" : error);
        errorCobro.setVisible(error != null && !carrito.isEmpty());
        errorCobro.setManaged(errorCobro.isVisible());
        cobrar.setDisable(error != null);
        cobrar.setText(carrito.isEmpty() ? "Cobrar" : "Cobrar " + Money.fmt(total));
        if ("Efectivo".equals(metodo()) && Forms.dinero(recibido) != null && Forms.dinero(recibido).compareTo(total) >= 0 && total.signum() > 0) {
            totales.getChildren().add(UI.hbox(8, UI.label("Vuelto", "text-success"), UI.spacer(), UI.label(Money.fmt(Forms.dinero(recibido).subtract(total)), "text-success", "semibold")));
        }
    }

    private HBox fila(String a, String b) {
        HBox h = UI.hbox(8, UI.label(a, "muted"), UI.spacer(), UI.label(b, "cell-title"));
        h.getStyleClass().add("total-row");
        return h;
    }

    private String validar() {
        if (carrito.isEmpty()) return "Agrega productos al carrito.";
        BigDecimal d = descuentoSoles();
        if (d.signum() < 0 || d.compareTo(bruto()) >= 0) return "El descuento no es válido.";
        if ("Factura".equals(comprobante.getValor()) && (cliente.getValue() == null || !cliente.getValue().tieneRuc())) {
            return "La factura exige un cliente con RUC.";
        }
        BigDecimal total = total();
        if ("Efectivo".equals(metodo())) {
            BigDecimal r = Forms.dinero(recibido);
            if (r == null || r.compareTo(total) < 0) return "El monto recibido no cubre el total.";
        }
        if ("Mixto".equals(metodo())) {
            BigDecimal a = Forms.dinero(monto1), b = Forms.dinero(monto2);
            if (a == null || b == null || a.add(b).compareTo(total) != 0) return "Los dos montos deben sumar " + Money.fmt(total) + ".";
        }
        return null;
    }

    private void cobrar() {
        if (validar() != null) return;
        Venta[] v = new Venta[1];
        List<VentaService.Linea> lineas = carrito.stream().map(i -> new VentaService.Linea(i.producto.getId(), i.cantidad, null)).toList();
        if (toast.intentar(() -> v[0] = ventas.cobrar(new VentaService.Cobro(cliente.getValue() == null ? null : cliente.getValue().getId(),
                "Factura".equals(comprobante.getValor()) ? Venta.Comprobante.FACTURA : Venta.Comprobante.BOLETA, lineas, descuentoSoles(), metodo(),
                Forms.dinero(recibido), metodo2.getValue(), Forms.dinero(monto1), Forms.dinero(monto2))))) {
            toast.exito(v[0].getNumeroCompleto() + " emitida por " + Money.fmt(v[0].getTotal()) + " · stock actualizado.");
            navigator.datosCambiaron();
            ticket(v[0]);
        }
    }

    private void nuevaVenta() {
        carrito.clear();
        descuento.setText("0");
        recibido.setText("0");
        monto1.setText("0");
        monto2.setText("0");
        cliente.setValue(null);
        comprobante.setValor("Boleta");
        renderGrid();
        renderCarrito();
    }

    /** Ticket de prueba (sin validez tributaria). */
    private void ticket(Venta v) {
        Empresa e = configuracion.empresa();
        VBox t = new VBox(4);
        t.getStyleClass().add("ticket");
        t.setPrefWidth(320);
        t.setMaxWidth(320);
        Label marca = UI.label("SISTEMA ERP SOFAS · SOFÁS", "ticket-strong");
        t.getChildren().addAll(marca, UI.label(e.getRazonSocial() + " · RUC " + e.getRuc()), UI.label(e.getDireccion()),
                regla(), UI.label((v.getComprobante() == Venta.Comprobante.BOLETA ? "BOLETA DE VENTA" : "FACTURA") + " " + v.getNumeroCompleto(), "ticket-strong"),
                UI.label(AppClock.fmt(v.getFecha().toLocalDate()) + " " + v.getFecha().toLocalTime().toString().substring(0, 5) + " · " + v.getVendedor()),
                UI.label("Cliente: " + v.getClienteNombre()));
        if (v.getCliente() != null) t.getChildren().add(UI.label(v.getCliente().getTipoDocumento() + ": " + v.getCliente().getDocumento()));
        t.getChildren().add(regla());
        for (ItemVenta i : v.getItems()) {
            t.getChildren().add(UI.label(i.getCantidad() + " × " + i.getProducto().getNombre()));
            t.getChildren().add(UI.hbox(4, UI.label("   " + Money.fmt(i.getPrecioUnitario()) + " c/u"), UI.spacer(), UI.label(Money.fmt(i.getSubtotal()))));
        }
        t.getChildren().add(regla());
        BigDecimal sub = Igv.subtotal(v.getTotal());
        if (v.getDescuento() != null && v.getDescuento().signum() > 0) t.getChildren().add(UI.hbox(4, UI.label("Descuento"), UI.spacer(), UI.label("− " + Money.fmt(v.getDescuento()))));
        t.getChildren().addAll(UI.hbox(4, UI.label("Op. gravada"), UI.spacer(), UI.label(Money.fmt(sub))),
                UI.hbox(4, UI.label("IGV 18 %"), UI.spacer(), UI.label(Money.fmt(v.getTotal().subtract(sub)))),
                UI.hbox(4, UI.label("TOTAL", "ticket-total"), UI.spacer(), UI.label(Money.fmt(v.getTotal()), "ticket-total")),
                UI.label("Pago: " + v.getMetodoPago()));
        if (v.getVuelto() != null) t.getChildren().add(UI.label("Recibido " + Money.fmt(v.getRecibido()) + " · vuelto " + Money.fmt(v.getVuelto())));
        t.getChildren().addAll(regla(), UI.wrap("Ticket de prueba · sin validez tributaria", "ticket-muted"), UI.wrap("Garantía: 12 meses estructura · 6 meses tapiz y espuma", "ticket-muted"));
        StackPane fondo = new StackPane(t);
        fondo.getStyleClass().add("doc-backdrop");
        Button imprimir = UI.secondary("Imprimir", "PRINT");
        Button nueva = UI.primary("Nueva venta", "ADD");
        var h = overlays.modal("Venta registrada", v.getNumeroCompleto(), fondo, 440, imprimir, nueva);
        imprimir.setOnAction(e2 -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(raiz.getScene().getWindow())) {
                if (job.printPage(t)) {
                    job.endJob();
                    toast.exito("Ticket enviado a la impresora.");
                }
            } else if (job == null) {
                toast.info("No hay impresora configurada en este equipo.");
            }
        });
        nueva.setOnAction(e2 -> {
            h.cerrar();
            nuevaVenta();
        });
        h.alCerrar(this::nuevaVenta);
    }

    private Region regla() {
        Region r = new Region();
        r.getStyleClass().add("ticket-rule");
        VBox.setMargin(r, new Insets(4, 0, 4, 0));
        return r;
    }

    // ================================================================ historial

    private void renderHistorial() {
        SearchField b = new SearchField("Buscar comprobante, cliente o modelo");
        b.setPrefWidth(300);
        ComboBox<String> tipo = Forms.combo(Arrays.asList(null, "Boleta", "Factura"), null, s -> s == null ? "Boletas y facturas" : s);
        TableView<Venta> t = Cells.tabla("Sin ventas.");
        TableColumn<Venta, String> total = Cells.derecha("Total", v -> Money.fmt(v.getTotal()), 100, "cell-strong");
        total.setComparator(Comparator.comparing(Money::parse));
        t.getColumns().addAll(List.of(
                Cells.texto("Comprobante", Venta::getNumeroCompleto, 120, "cell-mono"),
                Cells.texto("Fecha", v -> AppClock.fmt(v.getFecha().toLocalDate()) + " " + v.getFecha().toLocalTime().toString().substring(0, 5), 120),
                Cells.texto("Cliente", Venta::getClienteNombre, 200, "cell-strong"),
                Cells.texto("Productos", v -> v.getItems().stream().map(i -> i.getCantidad() + "× " + i.getProducto().getNombre()).reduce((a, c) -> a + ", " + c).orElse(""), 260, "cell-muted"),
                Cells.texto("Pago", Venta::getMetodoPago, 160),
                Cells.texto("Vendedor", Venta::getVendedor, 150, "cell-muted"),
                total, Cells.badge("Tipo", v -> v.getComprobante().nombre(), 80)));
        Runnable filtrar = () -> {
            String q = BusquedaService.normalizar(b.getText());
            t.getItems().setAll(ventas.historial().stream()
                    .filter(v -> tipo.getValue() == null || v.getComprobante().nombre().equals(tipo.getValue()))
                    .filter(v -> q.isEmpty() || BusquedaService.normalizar(v.getNumeroCompleto() + " " + v.getClienteNombre() + " "
                            + v.getItems().stream().map(i -> i.getProducto().getNombre()).reduce("", String::concat)).contains(q)).toList());
        };
        b.textProperty().addListener((o, a, c) -> filtrar.run());
        tipo.valueProperty().addListener((o, a, c) -> filtrar.run());
        filtrar.run();
        VBox card = new VBox(t);
        VBox.setVgrow(t, Priority.ALWAYS);
        card.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        historial.getChildren().setAll(UI.hbox(10, b, tipo, UI.spacer(), UI.label("Octubre: " + Money.fmt0(ventas.totalMes()) + " · " + ventas.sofasMes() + " unidades", "cell-title")), card);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
