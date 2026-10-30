/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: InventarioController
 * Descripción: Inventario: materiales (stock con reservado), kardex con saldo corrido y alertas de stock con generacion de OC.
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
package pe.erp.sofas.ui.views.inventario;

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
import pe.erp.sofas.service.calculo.UnidadCompra;
import pe.erp.sofas.ui.components.*;

import java.util.*;

/** Inventario: materiales (stock con reservado), kardex con saldo corrido y alertas de stock con generacion de OC. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class InventarioController implements Vista {

    private final InventarioService inventario;
    private final TelaService telas;
    private final ProveedorService proveedores;
    private final MrpService mrp;
    private final Overlays overlays;
    private final Toast toast;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private final VBox kpis = new VBox();
    private SegmentedControl tabs;
    private SearchField buscador;
    private ComboBox<String> categoria;
    private ComboBox<String> estado;
    private final StackPane contenido = new StackPane();
    private final TableView<Material> tablaMat = Cells.tabla("Ningún material coincide con los filtros.");
    private ComboBox<Material> kardexMaterial;
    private final TableView<Movimiento> tablaKardex = Cells.tabla("Sin movimientos.");
    private final Map<Long, Double> saldos = new HashMap<>();

    @FXML
    public void initialize() {
        tabs = new SegmentedControl("Materiales", "Kardex", "Alertas");
        tabs.getStyleClass().add("tabs");
        tabs.valorProperty().addListener((o, a, b) -> render());
        buscador = new SearchField("Buscar material, código o ubicación");
        buscador.setPrefWidth(300);
        buscador.textProperty().addListener((o, a, b) -> render());
        List<String> cats = new ArrayList<>();
        cats.add(null);
        cats.addAll(Material.CATEGORIAS);
        categoria = Forms.combo(cats, null, c -> c == null ? "Todas las categorías" : c);
        categoria.setPrefWidth(190);
        categoria.valueProperty().addListener((o, a, b) -> render());
        estado = Forms.combo(Arrays.asList(null, "Normal", "Stock bajo", "Crítico", "Agotado", "Bajo pedido"), null, s -> s == null ? "Todos los estados" : s);
        estado.setPrefWidth(170);
        estado.valueProperty().addListener((o, a, b) -> render());
        Button nuevo = UI.primary("Registrar material", "ADD");
        nuevo.setOnAction(e -> nuevoMaterial());
        construirTablas();
        VBox.setVgrow(contenido, Priority.ALWAYS);
        raiz.getChildren().setAll(kpis, UI.hbox(10, tabs, UI.spacer(), buscador, categoria, estado, nuevo), contenido);
    }

    @Override
    public void onShow(Intent intent) {
        if (intent.es(Intent.ACCION_ABRIR) && intent.dato() != null) {
            tabs.setValor("Kardex");
            render();
            kardexMaterial.getItems().stream().filter(m -> m != null && m.getCodigo().equals(intent.dato().toString())).findFirst().ifPresent(kardexMaterial::setValue);
            return;
        }
        render();
    }

    private void render() {
        List<Material> mats = inventario.materiales();
        long criticos = mats.stream().filter(Material::isCritico).count();
        kpis.getChildren().setAll(UI.fila(
                new KpiCard("ACCOUNT_BALANCE_WALLET", "wood", "Valor del inventario", Money.fmt0(inventario.valorTotal()), "A costo unitario"),
                new KpiCard("ALL_INBOX", "blue", "Materiales", String.valueOf(mats.size()), Material.CATEGORIAS.size() + " categorías"),
                new KpiCard("WARNING", "red", "Crítico o agotado", String.valueOf(criticos), mats.stream().filter(m -> "Stock bajo".equals(m.getEstado())).count() + " más en stock bajo")
                        .alClic(() -> tabs.setValor("Alertas")),
                new KpiCard("HISTORY", "green", "Movimientos del mes", String.valueOf(inventario.movimientosDelMes()), "Entradas, salidas, reservas y consumos")));
        String t = tabs.getValor();
        boolean esMat = "Materiales".equals(t);
        buscador.setDisable(!esMat);
        categoria.setDisable(!esMat);
        estado.setDisable(!esMat);
        switch (t) {
            case "Kardex" -> contenido.getChildren().setAll(kardex(mats));
            case "Alertas" -> contenido.getChildren().setAll(alertas());
            default -> {
                String q = BusquedaService.normalizar(buscador.getText());
                tablaMat.getItems().setAll(mats.stream()
                        .filter(m -> categoria.getValue() == null || m.getCategoria().equals(categoria.getValue()))
                        .filter(m -> estado.getValue() == null || m.getEstado().equals(estado.getValue()))
                        .filter(m -> q.isEmpty() || BusquedaService.normalizar(m.getCodigo() + " " + m.getNombre() + " " + m.getUbicacion()).contains(q)).toList());
                VBox card = new VBox(tablaMat);
                VBox.setVgrow(tablaMat, Priority.ALWAYS);
                card.getStyleClass().addAll("card", "table-card");
                contenido.getChildren().setAll(card);
            }
        }
    }

    private void construirTablas() {
        TableColumn<Material, String> valor = Cells.derecha("Valor", m -> Money.fmt(m.getValor()), 100, "cell-strong");
        valor.setComparator(Comparator.comparing(Money::parse));
        tablaMat.getColumns().addAll(List.of(
                Cells.texto("Código", Material::getCodigo, 70, "cell-mono"),
                Cells.texto("Material", Material::getNombre, 200, "cell-strong"),
                Cells.texto("Categoría", Material::getCategoria, 100, "cell-muted"),
                Cells.nodo("Stock · mínimo", this::celdaStock, 220),
                Cells.derecha("C. unit.", m -> Money.fmt(m.getCosto()), 80, "cell-muted"),
                valor,
                Cells.texto("Ubicación", Material::getUbicacion, 140, "cell-muted"),
                Cells.badge("Estado", Material::getEstado, 100),
                Cells.acciones("", m -> new Node[]{
                        boton("MOVE_TO_INBOX", "Entrada", () -> movimiento(m, TipoMovimiento.ENTRADA)),
                        boton("UNARCHIVE", "Salida", () -> movimiento(m, TipoMovimiento.SALIDA)),
                        boton("TUNE", "Ajuste", () -> movimiento(m, TipoMovimiento.AJUSTE)),
                        boton("HISTORY", "Ver kardex", () -> {
                            tabs.setValor("Kardex");
                            kardexMaterial.setValue(kardexMaterial.getItems().stream().filter(x -> x != null && x.getId().equals(m.getId())).findFirst().orElse(null));
                        })}, 140)));
        tablaMat.setFixedCellSize(54);
        tablaMat.setRowFactory(tv -> {
            TableRow<Material> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !r.isEmpty()) {
                    tabs.setValor("Kardex");
                    kardexMaterial.setValue(kardexMaterial.getItems().stream().filter(x -> x != null && x.getId().equals(r.getItem().getId())).findFirst().orElse(null));
                }
            });
            return r;
        });
        kardexMaterial = Forms.combo(List.of(), null, m -> m == null ? "Todos los materiales" : m.getCodigo() + " · " + m.getNombre());
        kardexMaterial.setPrefWidth(340);
        kardexMaterial.valueProperty().addListener((o, a, b) -> llenarKardex());
        tablaKardex.getColumns().addAll(List.of(
                Cells.texto("Fecha", m -> AppClock.fmt(m.getFecha().toLocalDate()) + " " + m.getFecha().toLocalTime().toString().substring(0, 5), 120),
                Cells.texto("Material", m -> m.getMaterial().getCodigo() + " · " + m.getMaterial().getNombre(), 220, "cell-strong"),
                Cells.nodo("Tipo", m -> new StatusBadge(m.getTipo().nombre(), m.getTipo().variante()).conIcono(m.getTipo().icono()), 130),
                Cells.nodo("Cantidad", m -> {
                    double ef = m.getEfecto();
                    String signo = !m.getTipo().afectaSaldo() ? "" : ef >= 0 ? "+" : "−";
                    Label l = UI.label(signo + Money.numero(Math.abs(m.getTipo() == TipoMovimiento.AJUSTE ? ef : m.getCantidad())) + " " + m.getMaterial().getUnidad(), "cell-title");
                    if (!m.getTipo().afectaSaldo()) l.getStyleClass().add("muted");
                    else l.setStyle(ef >= 0 ? "-fx-text-fill: -is-success-ink;" : "-fx-text-fill: -is-danger-ink;");
                    return l;
                }, 120),
                Cells.texto("Referencia", Movimiento::getReferencia, 100, "cell-mono"),
                Cells.texto("Concepto", Movimiento::getConcepto, 240, "cell-muted"),
                Cells.derecha("Saldo", m -> Money.numero(saldos.getOrDefault(m.getId(), 0d)), 90, "cell-strong"),
                Cells.texto("Usuario", Movimiento::getUsuario, 90, "cell-muted")));
    }

    private Button boton(String icono, String tip, Runnable r) {
        Button b = UI.iconButton(icono, tip, "btn-ghost", "btn-xs");
        b.setOnAction(e -> r.run());
        return b;
    }

    private Node celdaStock(Material m) {
        String color = switch (m.getEstado()) {
            case "Normal" -> "#16A34A";
            case "Stock bajo", "Bajo pedido" -> "#F59E0B";
            default -> "#DC2626";
        };
        ProgressBarMini barra = new ProgressBarMini(m.getMinimo() <= 0 ? 1 : m.getDisponible() / (m.getMinimo() * 2), color, 6);
        barra.setPrefWidth(70);
        Label stock = UI.label(Money.numero(m.getStock()) + " " + m.getUnidad(), "cell-title");
        Label res = UI.label(m.getReservado() > 0 ? "res. " + Money.numero(m.getReservado()) : "sin reservas", "cell-sub");
        Tooltip.install(res, new Tooltip("Disponible: " + Money.numero(m.getDisponible()) + " · mínimo " + Money.numero(m.getMinimo())));
        VBox v = new VBox(2, UI.hbox(6, stock, res), UI.hbox(6, UI.grow(barra), UI.label("mín. " + Money.numero(m.getMinimo()), "cell-sub")));
        return v;
    }

    private Node kardex(List<Material> mats) {
        Material sel = kardexMaterial.getValue();
        List<Material> items = new ArrayList<>();
        items.add(null);
        items.addAll(mats);
        kardexMaterial.getItems().setAll(items);
        kardexMaterial.setValue(sel == null ? null : mats.stream().filter(m -> m.getId().equals(sel.getId())).findFirst().orElse(null));
        llenarKardex();
        VBox card = new VBox(tablaKardex);
        VBox.setVgrow(tablaKardex, Priority.ALWAYS);
        card.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        HBox leyenda = new HBox(8);
        for (TipoMovimiento t : TipoMovimiento.values()) leyenda.getChildren().add(new StatusBadge(t.nombre(), t.variante()).conIcono(t.icono()));
        return new VBox(10, UI.hbox(10, kardexMaterial, UI.spacer(), UI.label("Reserva, Liberación y Retazo no alteran el saldo", "cell-sub")), leyenda, card);
    }

    /** Saldo corrido por material en orden cronologico; la tabla muestra lo mas reciente primero. */
    private void llenarKardex() {
        Material sel = kardexMaterial.getValue();
        List<Movimiento> movs = sel == null ? inventario.movimientos() : inventario.kardex(sel.getId());
        List<Movimiento> crono = new ArrayList<>(movs);
        crono.sort(Comparator.comparing(Movimiento::getFecha).thenComparing(Movimiento::getId));
        saldos.clear();
        Map<Long, Double> acum = new HashMap<>();
        if (sel == null) {
            // saldos por material desde el inicio de su kardex
            List<Movimiento> todos = new ArrayList<>(inventario.movimientos());
            todos.sort(Comparator.comparing(Movimiento::getFecha).thenComparing(Movimiento::getId));
            for (Movimiento m : todos) {
                double s = acum.getOrDefault(m.getMaterial().getId(), 0d) + m.getEfecto();
                acum.put(m.getMaterial().getId(), s);
                saldos.put(m.getId(), s);
            }
        } else {
            double s = 0;
            for (Movimiento m : crono) {
                s += m.getEfecto();
                saldos.put(m.getId(), s);
            }
        }
        List<Movimiento> recientes = new ArrayList<>(crono);
        Collections.reverse(recientes);
        tablaKardex.getItems().setAll(recientes);
    }

    private Node alertas() {
        List<Material> bajo = inventario.bajoMinimo();
        VBox lista = new VBox(8);
        if (bajo.isEmpty()) lista.getChildren().add(UI.empty("DONE_ALL", "Sin alertas", "Todos los materiales están sobre su mínimo.", null));
        for (Material m : bajo) {
            double sugerido = UnidadCompra.redondear(m.getUnidad(), Math.max(m.getMinimo() * 2 - m.getDisponible(), m.getMinimo()));
            Button oc = UI.button("Generar OC", "SHOPPING_CART", "btn-sm", "btn-wood");
            oc.setOnAction(e -> generarOC(List.of(m)));
            HBox h = UI.hbox(12, new StatusBadge(m.getEstado()), new VBox(1, UI.label(m.getCodigo() + " · " + m.getNombre(), "cell-title"),
                            UI.label("Disponible " + Money.numero(m.getDisponible()) + " " + m.getUnidad() + " (stock " + Money.numero(m.getStock()) + ", reservado "
                                    + Money.numero(m.getReservado()) + ") · mínimo " + Money.numero(m.getMinimo()) + " · proveedor "
                                    + (m.getProveedor() == null ? "—" : m.getProveedor().getRazonSocial()), "cell-sub")),
                    UI.spacer(), UI.label("Sugerido: " + Money.numero(sugerido) + " " + m.getUnidad(), "cell-sub"), oc);
            h.getStyleClass().add("alert-row");
            lista.getChildren().add(h);
        }
        Button todas = UI.primary("Generar OC de todas", "SHOPPING_CART");
        todas.setDisable(bajo.isEmpty());
        todas.setOnAction(e -> generarOC(bajo));
        List<Tela> telasBajas = telas.activas().stream().filter(t -> !"Disponible".equals(t.getEstado())).toList();
        VBox telasBox = new VBox(6, UI.label("Colores del muestrario por reponer", "card-title"));
        for (Tela t : telasBajas) {
            Hyperlink l = new Hyperlink("Ver en Telas y colores");
            l.setOnAction(e -> navigator.goTo("telas", Intent.abrir(t.getCodigo())));
            telasBox.getChildren().add(UI.hbox(10, new ColorSwatch(t.getHex(), t.getTipo(), 22), UI.label(t.getNombre() + " · " + Money.numero(t.getStock()) + " m", "cell-title"),
                    new StatusBadge(t.getEstado()), UI.spacer(), l));
        }
        return new VBox(14, UI.card(UI.cardHeader("Materiales bajo el mínimo", bajo.size() + " materiales · el disponible descuenta lo reservado", todas), lista), UI.card(telasBox));
    }

    private void generarOC(List<Material> mats) {
        List<MrpService.Fila> filas = new ArrayList<>();
        for (Material m : mats) {
            double falta = Math.max(m.getMinimo() * 2 - m.getDisponible(), m.getMinimo());
            if (m.getProveedor() != null) filas.add(new MrpService.Fila(m, falta, 0, falta, AppClock.hoy(), m.getProveedor(), Set.of("Alerta de stock")));
        }
        List<String>[] c = new List[1];
        if (toast.intentar(() -> c[0] = mrp.generarOrdenes(filas, "Alerta de stock"))) {
            toast.exito("Órdenes de compra en borrador: " + String.join(", ", c[0]) + ".");
            navigator.datosCambiaron();
        }
    }

    private void movimiento(Material m, TipoMovimiento tipo) {
        Validacion v = new Validacion();
        TextField cantidad = Forms.decimal(tipo == TipoMovimiento.AJUSTE ? 0 : 1);
        if (tipo == TipoMovimiento.AJUSTE) cantidad.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().matches("-?[0-9.,]*") ? c : null));
        TextField ref = UI.text("", tipo == TipoMovimiento.ENTRADA ? "Guía o factura" : "Vale o documento");
        ComboBox<String> motivo = Forms.combo(switch (tipo) {
            case ENTRADA -> List.of("Compra sin OC", "Devolución del taller", "Donación / muestra", "Otro");
            case SALIDA -> List.of("Uso en taller (sin OP)", "Merma / daño", "Muestra para cliente", "Otro");
            default -> List.of("Inventario físico", "Error de registro", "Otro");
        }, null, s -> s);
        FormGrid f = new FormGrid(2);
        ComboBox<Tela> color = null;
        if (m.isTela()) {
            color = Forms.combo(telas.porTipo(TipoTela.porMaterial(m.getCodigo())).stream().filter(t -> !t.getDescontinuada()).toList(), null,
                    t -> t.getColor() + " · " + Money.numero(t.getStock()) + " m");
            ComboBox<Tela> cc = color;
            f.nodo(v.campo("Color de la tela", cc, () -> Validacion.requerido(cc, "el color")), 2);
        }
        f.nodo(v.campo(tipo == TipoMovimiento.AJUSTE ? "Cantidad (+/−, " + m.getUnidad() + ")" : "Cantidad (" + m.getUnidad() + ")", cantidad, () -> {
            double x = Validacion.valor(cantidad);
            if (tipo == TipoMovimiento.AJUSTE) return x == 0 ? "Ingresa una cantidad distinta de cero." : m.getStock() + x < 0 ? "El stock quedaría negativo." : null;
            if (x <= 0) return "La cantidad debe ser mayor a cero.";
            return tipo == TipoMovimiento.SALIDA && x > m.getDisponible() + 1e-9 ? "Solo hay " + Money.numero(m.getDisponible()) + " disponibles (el resto está reservado)." : null;
        }), 1);
        f.nodo(v.campo("Referencia", ref, null), 1);
        f.nodo(v.campo("Motivo", motivo, () -> Validacion.requerido(motivo, "el motivo")), 2);
        VBox cuerpo = new VBox(12, UI.hbox(10, UI.miniStat("Stock", Money.numero(m.getStock()) + " " + m.getUnidad()), UI.miniStat("Reservado", Money.numero(m.getReservado())),
                UI.miniStat("Disponible", Money.numero(m.getDisponible()))), f);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Registrar " + tipo.nombre().toLowerCase(), tipo.icono());
        v.enlazar(ok);
        var h = overlays.modal(tipo.nombre() + " · " + m.getNombre(), m.getCodigo() + " · " + m.getUbicacion(), cuerpo, 520, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ComboBox<Tela> colorFinal = color;
        ok.setOnAction(e -> {
            if (toast.intentar(() -> inventario.movimientoManual(m.getId(), tipo, Validacion.valor(cantidad),
                    colorFinal == null || colorFinal.getValue() == null ? null : colorFinal.getValue().getId(), ref.getText(), motivo.getValue()))) {
                h.cerrar();
                toast.exito(tipo.nombre() + " de " + cantidad.getText() + " " + m.getUnidad() + " de " + m.getNombre() + " registrada en el kardex.");
                navigator.datosCambiaron();
                render();
            }
        });
    }

    private void nuevoMaterial() {
        Validacion v = new Validacion();
        TextField nombre = UI.text("", "Nombre del material");
        ComboBox<String> cat = Forms.combo(Material.CATEGORIAS, "Insumos", s -> s);
        ComboBox<String> unidad = Forms.combo(InventarioService.UNIDADES, "unidad", s -> s);
        TextField costo = Forms.decimal(0), stock = Forms.decimal(0), minimo = Forms.decimal(0);
        TextField ubic = UI.text("", "Almacén A · Rack 1");
        ComboBox<Proveedor> prov = Forms.combo(proveedores.activos(), null, Proveedor::getRazonSocial);
        CheckBox bajoPedido = new CheckBox("Se compra bajo pedido (sin stock)");
        FormGrid f = new FormGrid(3);
        f.nodo(v.campo("Nombre", nombre, () -> Validacion.requerido(nombre, "el nombre")), 3);
        f.nodo(v.campo("Categoría", cat, null), 1);
        f.nodo(v.campo("Unidad", unidad, null), 1);
        f.nodo(v.campo("Costo unitario (S/)", costo, () -> Validacion.numero(costo, 0, "El costo")), 1);
        f.nodo(v.campo("Stock inicial", stock, () -> Validacion.numero(stock, 0, "El stock")), 1);
        f.nodo(v.campo("Mínimo", minimo, () -> Validacion.numero(minimo, 0, "El mínimo")), 1);
        f.nodo(v.campo("Ubicación", ubic, null), 1);
        f.nodo(v.campo("Proveedor", prov, null), 2);
        f.nodo(bajoPedido, 1);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Registrar material", "SAVE");
        v.enlazar(ok);
        var h = overlays.modal("Registrar material", "El stock inicial queda registrado como Entrada en el kardex", f, 640, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            Material[] g = new Material[1];
            if (toast.intentar(() -> g[0] = inventario.guardarMaterial(Material.builder().nombre(nombre.getText().trim()).categoria(cat.getValue())
                    .unidad(unidad.getValue()).costo(Validacion.valor(costo)).stock(Validacion.valor(stock)).minimo(Validacion.valor(minimo))
                    .ubicacion(ubic.getText()).proveedor(prov.getValue()).bajoPedido(bajoPedido.isSelected()).reservado(0d).build()))) {
                h.cerrar();
                toast.exito(g[0].getCodigo() + " · " + g[0].getNombre() + " registrado.");
                render();
            }
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
