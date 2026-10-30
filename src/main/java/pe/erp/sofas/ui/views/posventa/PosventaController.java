/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PosventaController
 * Descripción: Retapizado de sofas de clientes (con cotizacion automatica) y garantias y reclamos (cobertura y SLA).
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
package pe.erp.sofas.ui.views.posventa;

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
import pe.erp.sofas.service.calculo.Cobertura;
import pe.erp.sofas.ui.AppWindow;
import pe.erp.sofas.ui.components.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/** Retapizado de sofas de clientes (con cotizacion automatica) y garantias y reclamos (cobertura y SLA). */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class PosventaController implements Vista {

    private final RetapizadoService retapizado;
    private final GarantiaService garantias;
    private final ClienteService clientes;
    private final ProductoService productos;
    private final TelaService telas;
    private final ImagenService imagenes;
    private final ExportService export;
    private final Overlays overlays;
    private final Toast toast;
    private final Navigator navigator;
    private final AppWindow appWindow;

    @FXML private VBox raiz;

    private SegmentedControl tabs;
    private final Button accion = UI.primary("Nuevo retapizado", "ADD");
    private final VBox contenido = new VBox(14);

    @FXML
    public void initialize() {
        tabs = new SegmentedControl("Retapizado", "Garantías y reclamos");
        tabs.getStyleClass().add("tabs");
        tabs.valorProperty().addListener((o, a, b) -> render());
        accion.setOnAction(e -> {
            if ("Retapizado".equals(tabs.getValor())) nuevoRetapizado();
            else nuevoReclamo();
        });
        ScrollPane sp = UI.scroll(contenido);
        VBox.setVgrow(sp, Priority.ALWAYS);
        raiz.getChildren().setAll(UI.hbox(10, tabs, UI.spacer(), accion), sp);
    }

    @Override
    public void onShow(Intent intent) {
        if (intent.dato() != null && intent.dato().toString().startsWith("GR-")) tabs.setValor("Garantías y reclamos");
        else if (intent.dato() != null && intent.dato().toString().startsWith("RT-")) tabs.setValor("Retapizado");
        render();
        if (intent.dato() != null && intent.dato().toString().startsWith("GR-")) {
            garantias.listar().stream().filter(g -> g.getCodigo().equals(intent.dato().toString())).findFirst().ifPresent(this::detalleReclamo);
        }
    }

    private void render() {
        boolean ret = "Retapizado".equals(tabs.getValor());
        accion.setText(ret ? "Nuevo retapizado" : "Registrar reclamo");
        if (ret) renderRetapizado();
        else renderGarantias();
    }

    // ================================================================ retapizado

    private void renderRetapizado() {
        RetapizadoService.Kpis k = retapizado.kpis();
        TableView<Retapizado> t = Cells.tabla("Sin retapizados.");
        t.getColumns().addAll(List.of(
                Cells.texto("Código", Retapizado::getCodigo, 64, "cell-mono"),
                Cells.nodo("Cliente", r -> UI.wrap(r.getCliente().getNombre(), "cell-title"), 140),
                Cells.nodo("Sofá del cliente", r -> new VBox(0, UI.label(r.getDescripcion(), "cell-title"), UI.label(r.getCuerpos() + " cuerpos · " + r.getMedidas()
                        + " · estructura " + r.getEstructura().toLowerCase() + " · espuma " + r.getEspuma().toLowerCase(), "cell-sub")), 280),
                Cells.nodo("Tela nueva", r -> r.getTela() == null ? UI.label("Por elegir", "cell-sub")
                        : UI.hbox(6, new ColorSwatch(r.getTela().getHex(), r.getTela().getTipo(), 18), new VBox(0, UI.label(r.getTela().getNombre(), "cell-title"),
                        UI.label(r.getMetros() == null ? "" : Money.numero(r.getMetros()) + " m", "cell-sub"))), 180),
                Cells.nodo("Recojo · entrega", r -> new VBox(0, UI.label("Recojo " + AppClock.fmt(r.getFechaRecojo()), "cell-sub"),
                        UI.label("Entrega " + AppClock.fmt(r.getFechaEntrega()), "cell-sub")), 130),
                Cells.derecha("Total", r -> r.getTotal() == null ? "—" : Money.fmt0(r.getTotal()), 76, "cell-strong"),
                Cells.nodo("Estado", r -> {
                    VBox v = new VBox(2, new StatusBadge(r.getEstado().nombre()));
                    if (r.getOpCodigo() != null) {
                        Hyperlink l = new Hyperlink(r.getOpCodigo());
                        l.setOnAction(e -> navigator.goTo("produccion", Intent.abrir(r.getOpCodigo())));
                        v.getChildren().add(l);
                    }
                    return v;
                }, 120),
                Cells.acciones("", r -> {
                    List<Node> n = new ArrayList<>();
                    if (r.getEstado().accion() != null) {
                        Button b = UI.button(r.getEstado().accion(), null, "btn-sm", "btn-secondary");
                        b.setOnAction(e -> {
                            String[] m = new String[1];
                            if (toast.intentar(() -> m[0] = retapizado.avanzar(r.getId()))) {
                                toast.exito(m[0]);
                                navigator.datosCambiaron();
                                render();
                            }
                        });
                        n.add(b);
                    }
                    Button pdf = UI.iconButton("PICTURE_AS_PDF", "Cotización PDF", "btn-ghost", "btn-xs");
                    pdf.setDisable(r.getTela() == null);
                    pdf.setOnAction(e -> cotizacionPdf(r));
                    n.add(pdf);
                    return n.toArray(Node[]::new);
                }, 190)));
        t.getItems().setAll(retapizado.listar());
        t.setFixedCellSize(56);
        t.setPrefHeight(56 * Math.max(4, t.getItems().size()) + 44);
        VBox card = new VBox(t);
        card.getStyleClass().addAll("card", "table-card");
        contenido.getChildren().setAll(UI.fila(
                new KpiCard("BUILD_CIRCLE", "blue", "Retapizados activos", String.valueOf(k.activos()), "Aún no entregados"),
                new KpiCard("BUILD", "violet", "En taller", String.valueOf(k.enTaller()), "Con orden de producción"),
                new KpiCard("LOCAL_SHIPPING", "wood", "Recojos programados", String.valueOf(k.recojos()), "Por recoger del cliente"),
                new KpiCard("PAYMENTS", "green", "Ingresos del mes", Money.fmt0(k.ingresosMes()), "Retapizados entregados")),
                UI.label("Flujo: Por cotizar → Cotizado → Aprobado → Recojo programado → En taller (crea la OP en Corte de tela) → Listo → Entregado", "cell-sub"), card);
    }

    private void cotizacionPdf(Retapizado r) {
        File f = Archivos.guardar(contenido, "Cotizacion-" + r.getCodigo(), "PDF", "pdf");
        if (f == null) return;
        Tareas.ejecutar(overlays, toast, "Generando cotización…", () -> {
            export.cotizacionRetapizado(r, retapizado.cotizar(r), f);
            return f;
        }, a -> {
            toast.exito("Cotización guardada: " + a.getName());
            appWindow.hostServices().showDocument(a.toURI().toString());
        });
    }

    private void nuevoRetapizado() {
        Validacion v = new Validacion();
        ComboBox<Cliente> cliente = Forms.combo(clientes.listar(), null, Cliente::getNombre);
        Spinner<Integer> cuerpos = new Spinner<>(1, 4, 3);
        cuerpos.setMaxWidth(Double.MAX_VALUE);
        TextField desc = UI.text("", "Sofá 3 cuerpos color vino, tela gastada");
        TextField largo = Forms.entero(200), fondo = Forms.entero(90), alto = Forms.entero(85);
        ComboBox<String> estructura = Forms.combo(Retapizado.ESTRUCTURAS, "Buena", s -> s);
        ComboBox<String> espuma = Forms.combo(Retapizado.ESPUMAS, "Hundida", s -> s);
        DatePicker recojo = Forms.fecha(AppClock.hoy().plusDays(2));
        ComboBox<Tela> tela = Forms.combo(telas.activas(), null, t -> t.getNombre() + " · " + Money.numero(t.getStock()) + " m");
        File[] foto = new File[1];
        ImageSlot slot = new ImageSlot("Foto del sofá del cliente", 220, 150, 12);
        slot.alElegir(f -> {
            foto[0] = f;
            slot.mostrar(new javafx.scene.image.Image(f.toURI().toString(), 600, 0, true, true));
        });
        slot.alQuitar(() -> {
            foto[0] = null;
            slot.mostrar((javafx.scene.image.Image) null);
        });
        VBox cotiz = new VBox(8);
        Runnable calcular = () -> {
            if (tela.getValue() == null || Forms.entero(largo) < 40) {
                cotiz.getChildren().setAll(UI.label("Elige la tela para ver la cotización automática.", "muted"));
                return;
            }
            try {
                RetapizadoService.Cotizacion c = retapizado.cotizar(cuerpos.getValue(), Forms.entero(largo), Forms.entero(fondo), Forms.entero(alto),
                        estructura.getValue(), espuma.getValue(), tela.getValue());
                GridPane g = new GridPane();
                g.setHgap(12);
                g.setVgap(4);
                int i = 0;
                for (var l : c.lineas()) {
                    g.addRow(i++, UI.label(l.concepto(), "cell-sub"), UI.label(l.material().getNombre(), "cell-title"),
                            UI.label(Money.numero(l.cantidad()) + " " + l.material().getUnidad(), "cell-sub"), UI.label(Money.fmt(l.costo()), "cell-title"));
                }
                g.addRow(i, UI.label("Mano de obra", "cell-sub"), UI.label("(tapizado + costura) × 1.2 + acabado", "cell-title"),
                        UI.label(Money.numero(c.horas()) + " h × S/ 9", "cell-sub"), UI.label(Money.fmt(c.costoMO()), "cell-title"));
                cotiz.getChildren().setAll(UI.label("Cotización automática · base " + c.ficha().getEtiqueta() + " escalada", "card-title"), g,
                        UI.hbox(10, UI.miniStat("Costo", Money.fmt(c.costo())), UI.miniStat("Precio (costo × 1.06 × 1.45)", Money.fmt0(c.precio())),
                                UI.miniStat("Tela", Money.numero(c.metrosTela()) + " m")));
                if (c.aviso() != null) cotiz.getChildren().add(UI.banner("danger-banner", "WARNING", c.aviso()));
            } catch (RuntimeException ex) {
                cotiz.getChildren().setAll(UI.label(ex.getMessage(), "field-error"));
            }
        };
        for (var p : List.of(estructura.valueProperty(), espuma.valueProperty(), tela.valueProperty())) p.addListener((o, a, b) -> calcular.run());
        cuerpos.valueProperty().addListener((o, a, b) -> calcular.run());
        for (TextField tf : List.of(largo, fondo, alto)) tf.textProperty().addListener((o, a, b) -> calcular.run());
        calcular.run();
        FormGrid f = new FormGrid(3);
        f.nodo(v.campo("Cliente", cliente, () -> Validacion.requerido(cliente, "el cliente")), 2);
        f.nodo(v.campo("Cuerpos", cuerpos, null), 1);
        f.nodo(v.campo("Descripción del sofá", desc, () -> Validacion.requerido(desc, "la descripción")), 3);
        f.nodo(v.campo("Largo (cm)", largo, () -> Validacion.numero(largo, 40, "El largo")), 1);
        f.nodo(v.campo("Fondo (cm)", fondo, () -> Validacion.numero(fondo, 40, "El fondo")), 1);
        f.nodo(v.campo("Alto (cm)", alto, () -> Validacion.numero(alto, 40, "El alto")), 1);
        f.nodo(v.campo("Estructura", estructura, null), 1);
        f.nodo(v.campo("Espuma", espuma, null), 1);
        f.nodo(v.campo("Fecha de recojo", recojo, () -> recojo.getValue() == null ? "Elige la fecha." : null), 1);
        f.nodo(v.campo("Tela nueva", tela, null), 3);
        HBox cuerpo = new HBox(16, slot, new VBox(14, f, cotiz));
        HBox.setHgrow(cuerpo.getChildren().get(1), Priority.ALWAYS);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Registrar retapizado", "SAVE");
        v.enlazar(ok);
        var h = overlays.modal("Nuevo retapizado", "La cotización usa la ficha de 1, 2 o 3 cuerpos escalada a las medidas", cuerpo, 900, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            Retapizado[] r = new Retapizado[1];
            if (toast.intentar(() -> {
                r[0] = retapizado.crear(cliente.getValue().getId(), cuerpos.getValue(), desc.getText(), Forms.entero(largo), Forms.entero(fondo),
                        Forms.entero(alto), estructura.getValue(), espuma.getValue(), recojo.getValue(), tela.getValue() == null ? null : tela.getValue().getId(), null);
                if (foto[0] != null) {
                    String ruta = imagenes.guardar(foto[0], r[0].getCodigo(), 1200, 800);
                    retapizado.fijarFoto(r[0].getId(), ruta);
                }
            })) {
                h.cerrar();
                toast.exito(r[0].getCodigo() + " registrado · " + r[0].getEstado().nombre() + (r[0].getTotal() == null ? "." : " por " + Money.fmt0(r[0].getTotal()) + "."));
                render();
            }
        });
    }

    // ================================================================ garantias

    private void renderGarantias() {
        GarantiaService.Kpis k = garantias.kpis();
        TableView<ReclamoGarantia> t = Cells.tabla("Sin reclamos.");
        t.getColumns().addAll(List.of(
                Cells.texto("Reclamo", ReclamoGarantia::getCodigo, 80, "cell-mono"),
                Cells.texto("Cliente", r -> r.getCliente().getNombre(), 190, "cell-strong"),
                Cells.nodo("Sofá y compra", r -> new VBox(0, UI.label(r.getProductoNombre(), "cell-title"), UI.label("Comprado el " + AppClock.fmt(r.getFechaCompra()), "cell-sub")), 200),
                Cells.texto("Falla", ReclamoGarantia::getFalla, 150),
                Cells.nodo("Cobertura", r -> {
                    Cobertura.Resultado c = garantias.cobertura(r);
                    return new VBox(1, new StatusBadge(c.cubierto() ? "En garantía" : "Fuera de garantía"), UI.label((c.cubierto() ? "Vence " : "Venció ") + AppClock.fmt(c.vence()), "cell-sub"));
                }, 150),
                Cells.nodo("SLA", r -> {
                    long d = garantias.sla(r);
                    Label l = UI.label(d + " días", "cell-title");
                    if (garantias.slaVencido(r)) l.setStyle("-fx-text-fill: -is-danger;");
                    return l;
                }, 80),
                Cells.badge("Estado", r -> r.getEstado().nombre(), 120)));
        t.getItems().setAll(garantias.listar());
        t.setFixedCellSize(54);
        t.setPrefHeight(54 * Math.max(5, t.getItems().size()) + 44);
        t.setRowFactory(tv -> {
            TableRow<ReclamoGarantia> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty()) detalleReclamo(r.getItem());
            });
            return r;
        });
        VBox card = new VBox(t);
        card.getStyleClass().addAll("card", "table-card");
        contenido.getChildren().setAll(UI.fila(
                new KpiCard("REPORT_PROBLEM", "amber", "Reclamos abiertos", String.valueOf(k.abiertos()), "Por resolver"),
                new KpiCard("VERIFIED_USER", "green", "Cubiertos por garantía", String.format("%.0f %%", k.pctCubiertos()), "Del total de reclamos"),
                new KpiCard("SCHEDULE", "blue", "Tiempo promedio de solución", Money.numero(k.diasPromedio()) + " días", "Reclamos cerrados"),
                new KpiCard("PAYMENTS", "red", "Costo de garantías", Money.fmt0(k.costo()), "Reparaciones y reemplazos")),
                UI.label("Cobertura: pata floja, estructura quebrada y mecanismo trabado 12 meses (estructura); el resto 6 meses (tapiz y espuma). SLA objetivo: 7 días.", "cell-sub"),
                card);
    }

    private void detalleReclamo(ReclamoGarantia r) {
        Cobertura.Resultado c = garantias.cobertura(r);
        TextArea diag = Forms.area(r.getDiagnostico(), "Diagnóstico técnico", 3);
        ComboBox<String> sol = Forms.combo(ReclamoGarantia.SOLUCIONES, r.getSolucion() == null ? "Por definir" : r.getSolucion(), s -> s);
        TextField costo = Forms.decimal(r.getCosto());
        ToggleGroup estados = new ToggleGroup();
        FlowPane botones = new FlowPane(6, 6);
        for (EstadoReclamo e : EstadoReclamo.values()) {
            ToggleButton b = new ToggleButton(e.nombre());
            b.getStyleClass().add("opt-tile");
            b.setUserData(e);
            b.setToggleGroup(estados);
            b.setSelected(e == r.getEstado());
            botones.getChildren().add(b);
        }
        estados.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null) estados.selectToggle(a);
        });
        VBox cuerpo = new VBox(14,
                UI.hbox(8, new StatusBadge(c.cubierto() ? "En garantía" : "Fuera de garantía"), UI.label(c.texto() + " · " + c.tipo(), "cell-sub")),
                UI.dato("Cliente", r.getCliente().getNombre() + " · " + r.getCliente().getTelefono()),
                UI.dato("Sofá", r.getProductoNombre() + " · comprado el " + AppClock.fmt(r.getFechaCompra())),
                UI.dato("Falla reportada el " + AppClock.fmt(r.getFecha()), r.getFalla() + ": " + r.getDescripcion()),
                UI.dato("SLA", garantias.sla(r) + " días" + (garantias.slaVencido(r) ? " · supera el objetivo de 7 días" : "")),
                UI.field("Diagnóstico", diag), UI.field("Solución", sol), UI.field("Costo para la empresa (S/)", costo),
                UI.label("Estado", "form-label"), botones);
        if (r.getFechaCierre() != null) cuerpo.getChildren().add(UI.label("Cerrado el " + AppClock.fmt(r.getFechaCierre()), "cell-sub"));
        Button guardar = UI.primary("Guardar", "SAVE");
        Overlays.Handle[] h = new Overlays.Handle[1];
        guardar.setOnAction(e -> {
            EstadoReclamo est = (EstadoReclamo) estados.getSelectedToggle().getUserData();
            if (toast.intentar(() -> garantias.actualizar(r.getId(), diag.getText(), sol.getValue(), Forms.dinero(costo), est))) {
                h[0].cerrar();
                toast.exito(r.getCodigo() + " actualizado · " + est.nombre() + (est.cerrado() ? " (cerrado hoy)." : "."));
                navigator.datosCambiaron();
                render();
            }
        });
        h[0] = overlays.panel(r.getCodigo() + " · " + r.getFalla(), r.getCliente().getNombre(), cuerpo, UI.hbox(8, UI.spacer(), guardar), 480);
    }

    private void nuevoReclamo() {
        Validacion v = new Validacion();
        ComboBox<Cliente> cliente = Forms.combo(clientes.listar(), null, Cliente::getNombre);
        ComboBox<Producto> producto = Forms.combo(productos.listar(), null, Producto::getNombre);
        DatePicker compra = Forms.fecha(AppClock.hoy().minusMonths(3));
        ComboBox<String> falla = Forms.combo(ReclamoGarantia.FALLAS, null, s -> s);
        TextArea desc = Forms.area("", "Qué pasó, desde cuándo, fotos recibidas…", 3);
        Label aviso = UI.label("", "semibold");
        Runnable cob = () -> {
            if (falla.getValue() == null || compra.getValue() == null) {
                aviso.setText("Elige la falla y la fecha de compra para ver la cobertura.");
                aviso.setStyle("-fx-text-fill: -is-muted;");
                return;
            }
            Cobertura.Resultado c = Cobertura.evaluar(falla.getValue(), compra.getValue(), AppClock.hoy());
            aviso.setText(c.texto() + " · " + c.tipo());
            aviso.setStyle(c.cubierto() ? "-fx-text-fill: -is-success-ink;" : "-fx-text-fill: -is-danger-ink;");
        };
        falla.valueProperty().addListener((o, a, b) -> cob.run());
        compra.valueProperty().addListener((o, a, b) -> cob.run());
        cob.run();
        FormGrid f = new FormGrid(2);
        f.nodo(v.campo("Cliente", cliente, () -> Validacion.requerido(cliente, "el cliente")), 2);
        f.nodo(v.campo("Sofá", producto, () -> Validacion.requerido(producto, "el sofá")), 1);
        f.nodo(v.campo("Fecha de compra", compra, () -> compra.getValue() == null || compra.getValue().isAfter(AppClock.hoy()) ? "Revisa la fecha." : null), 1);
        f.nodo(v.campo("Falla", falla, () -> Validacion.requerido(falla, "la falla")), 2);
        f.nodo(v.campo("Descripción", desc, null), 2);
        f.nodo(aviso, 2);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Registrar reclamo", "SAVE");
        v.enlazar(ok);
        var h = overlays.modal("Registrar reclamo", "La cobertura se calcula en vivo según la falla", f, 560, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            ReclamoGarantia[] r = new ReclamoGarantia[1];
            if (toast.intentar(() -> r[0] = garantias.registrar(cliente.getValue().getId(), producto.getValue().getId(), compra.getValue(), falla.getValue(), desc.getText()))) {
                h.cerrar();
                toast.exito(r[0].getCodigo() + " registrado · " + garantias.cobertura(r[0]).texto() + ".");
                navigator.datosCambiaron();
                render();
            }
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
