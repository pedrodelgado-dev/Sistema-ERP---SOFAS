/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PedidosController
 * Descripción: Pedidos a medida: lista con filtros a la izquierda y detalle con linea de tiempo, pagos y OP a la derecha.
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
package pe.erp.sofas.ui.views.pedidos;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.AppWindow;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.Render3D;
import pe.erp.sofas.ui.three.SofaBuilder;
import pe.erp.sofas.ui.three.Viewer3D;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/** Pedidos a medida: lista con filtros a la izquierda y detalle con linea de tiempo, pagos y OP a la derecha. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class PedidosController implements Vista {

    private final PedidoService pedidos;
    private final ClienteService clientes;
    private final ProductoService productos;
    private final TelaService telas;
    private final ProduccionService produccion;
    private final EmpleadoService empleados;
    private final ExportService export;
    private final Overlays overlays;
    private final Toast toast;
    private final ConfirmDialog confirm;
    private final Navigator navigator;
    private final AppWindow appWindow;

    @FXML private VBox raiz;

    private SearchField buscador;
    private ComboBox<EstadoPedido> cmbEstado;
    private ComboBox<Pedido.Tipo> cmbTipo;
    private final VBox lista = new VBox(8);
    private final VBox detalle = new VBox(14);
    private final Label conteo = UI.label("", "cell-sub");
    private Long seleccion;

    @FXML
    public void initialize() {
        buscador = new SearchField("Buscar pedido, cliente o modelo");
        buscador.textProperty().addListener((o, a, b) -> renderLista());
        List<EstadoPedido> es = new ArrayList<>();
        es.add(null);
        es.addAll(List.of(EstadoPedido.values()));
        cmbEstado = Forms.combo(es, null, e -> e == null ? "Todos los estados" : e.nombre());
        cmbEstado.valueProperty().addListener((o, a, b) -> renderLista());
        List<Pedido.Tipo> ts = new ArrayList<>();
        ts.add(null);
        ts.addAll(List.of(Pedido.Tipo.values()));
        cmbTipo = Forms.combo(ts, null, t -> t == null ? "Todos los tipos" : t.nombre());
        cmbTipo.valueProperty().addListener((o, a, b) -> renderLista());
        Button nuevo = UI.primary("Nuevo pedido", "ADD");
        nuevo.setOnAction(e -> nuevo(null));
        nuevo.setMaxWidth(Double.MAX_VALUE);
        ScrollPane spLista = UI.scroll(lista);
        VBox.setVgrow(spLista, Priority.ALWAYS);
        VBox izq = new VBox(10, nuevo, buscador, UI.hbox(8, cmbEstado, cmbTipo), conteo, spLista);
        HBox.setHgrow(cmbEstado, Priority.ALWAYS);
        HBox.setHgrow(cmbTipo, Priority.ALWAYS);
        izq.setPrefWidth(380);
        izq.setMinWidth(340);
        izq.setMaxWidth(400);
        ScrollPane spDet = UI.scroll(detalle);
        HBox.setHgrow(spDet, Priority.ALWAYS);
        HBox cuerpo = new HBox(16, izq, spDet);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        raiz.getChildren().setAll(cuerpo);
    }

    @Override
    public void onShow(Intent intent) {
        if (intent.es(Intent.ACCION_ABRIR) && intent.dato() != null) {
            pedidos.porCodigo(intent.dato().toString()).ifPresent(p -> seleccion = p.getId());
        }
        if (seleccion == null && !pedidos.listar().isEmpty()) seleccion = pedidos.listar().get(0).getId();
        renderLista();
        renderDetalle();
        if (intent.es(Intent.ACCION_NUEVO)) nuevo(intent.dato() instanceof Cliente c ? c : null);
    }

    private void seleccionar(Long id) {
        seleccion = id;
        renderLista();
        renderDetalle();
    }

    private void renderLista() {
        String q = BusquedaService.normalizar(buscador.getText());
        List<Pedido> l = pedidos.listar().stream()
                .filter(p -> cmbEstado.getValue() == null || p.getEstado() == cmbEstado.getValue())
                .filter(p -> cmbTipo.getValue() == null || p.getTipo() == cmbTipo.getValue())
                .filter(p -> q.isEmpty() || BusquedaService.normalizar(p.getCodigo() + " " + p.getCliente().getNombre() + " " + p.getProductoNombre()).contains(q))
                .toList();
        conteo.setText(l.size() + (l.size() == 1 ? " pedido" : " pedidos") + " · " + pedidos.pendientesAMedida() + " a medida pendientes");
        lista.getChildren().clear();
        for (Pedido p : l) {
            ProgressBarMini pago = new ProgressBarMini(p.getPctPagado(), p.getPctPagado() >= 1 ? "#16A34A" : p.getPctPagado() >= 0.5 ? "#2563EB" : "#F59E0B", 5);
            HBox tela = new HBox(6);
            tela.setAlignment(Pos.CENTER_LEFT);
            if (p.getTela() != null) tela.getChildren().add(new ColorSwatch(p.getTela().getHex(), p.getTela().getTipo(), 14));
            tela.getChildren().add(UI.label(p.getProductoNombre() + (p.getCantidad() > 1 ? " ×" + p.getCantidad() : ""), "cell-sub"));
            VBox c = new VBox(6,
                    UI.hbox(6, UI.codigo(p.getCodigo()), p.getTipo() == Pedido.Tipo.PERSONALIZADO ? new StatusBadge("Personalizado") : new StatusBadge("Estándar"),
                            UI.spacer(), new StatusBadge(p.isRetrasado() ? "Retrasado" : p.getEstado().nombre())),
                    UI.label(p.getCliente().getNombre(), "cell-title"), tela,
                    UI.hbox(6, UI.label("Entrega " + AppClock.fmt(p.getFechaEntrega()), "cell-sub"), UI.spacer(), UI.label(Money.fmt0(p.getPrecio()), "cell-title")),
                    pago);
            c.getStyleClass().add("order-card");
            if (p.getId().equals(seleccion)) c.getStyleClass().add("order-card-selected");
            c.setOnMouseClicked(e -> seleccionar(p.getId()));
            lista.getChildren().add(c);
        }
        if (l.isEmpty()) lista.getChildren().add(UI.empty("ASSIGNMENT", "Sin pedidos", "No hay pedidos con estos filtros.", null));
    }

    // ================================================================ detalle

    private void renderDetalle() {
        detalle.getChildren().clear();
        if (seleccion == null) return;
        Pedido p = pedidos.pedido(seleccion);
        HBox cab = UI.hbox(10, new VBox(2, UI.hbox(8, UI.label(p.getCodigo(), "card-title"), new StatusBadge(p.getTipo().nombre()),
                        new StatusBadge(p.isRetrasado() ? "Retrasado" : p.getEstado().nombre())),
                UI.label(p.getCliente().getNombre() + " · " + p.getCliente().getTipoDocumento() + " " + p.getCliente().getDocumento() + " · " + p.getCliente().getTelefono(), "cell-sub")),
                UI.spacer());
        Hyperlink verCliente = new Hyperlink("Ver cliente");
        verCliente.setOnAction(e -> navigator.goTo("clientes", Intent.abrir(p.getCliente().getCodigo())));
        cab.getChildren().add(verCliente);
        detalle.getChildren().add(UI.card(cab, timeline(p), acciones(p)));

        // Especificacion
        GridPane g = UI.grid(2);
        g.add(UI.dato("Modelo", p.getProductoNombre() + (p.getCantidad() > 1 ? " × " + p.getCantidad() : "")), 0, 0);
        g.add(UI.dato("Medidas", p.getMedidas()), 1, 0);
        HBox tela = new HBox(8);
        tela.setAlignment(Pos.CENTER_LEFT);
        if (p.getTela() != null) tela.getChildren().addAll(new ColorSwatch(p.getTela().getHex(), p.getTela().getTipo(), 22), UI.label(p.getTela().getNombre(), "dato-valor"));
        else tela.getChildren().add(UI.label("Según el modelo", "dato-valor"));
        g.add(UI.dato("Material y color", tela), 0, 1);
        g.add(UI.dato("Fecha del pedido", AppClock.fmt(p.getFecha()) + " · entrega " + AppClock.fmt(p.getFechaEntrega())), 1, 1);
        VBox esp = new VBox(12, UI.label("Especificación", "card-title"), g);
        if (p.getEspecificacion() != null) esp.getChildren().add(UI.wrap(p.getEspecificacion(), "muted"));
        if (p.getObservaciones() != null && !p.getObservaciones().isBlank()) {
            esp.getChildren().add(UI.banner("lock-banner", "INFO", p.getObservaciones()));
        }
        HBox espCard = new HBox(16, miniatura(p), esp);
        HBox.setHgrow(esp, Priority.ALWAYS);
        detalle.getChildren().add(UI.card(espCard));

        detalle.getChildren().add(UI.columnas(new double[]{1.1, 1}, pagos(p), ordenVinculada(p)));
    }

    private Node miniatura(Pedido p) {
        ImageSlot s = new ImageSlot("Foto: " + p.getProductoNombre(), 260, 175, 12).soloLectura();
        ConfigSofa cfg = ConfigSofa.leer(p.getConfiguracion());
        if (cfg != null) {
            Tela t = cfg.telaId() == null ? null : telas.tela(cfg.telaId());
            Tela tc = cfg.telaCojinesId() == null ? t : telas.tela(cfg.telaCojinesId());
            s.mostrar(Viewer3D.render(new SofaBuilder().build(SofaBuilder.Opciones.de(cfg, t, tc)), 520, 350, -28, 16));
        } else if (p.getProducto() != null) {
            if (p.getProducto().getImagenPrincipal() == null) s.respaldo(() -> Render3D.modelo(p.getProducto()));
            s.mostrar(p.getProducto().getImagenPrincipal());
        }
        return s;
    }

    private Node timeline(Pedido p) {
        HBox h = new HBox();
        h.setAlignment(Pos.TOP_CENTER);
        EstadoPedido[] flujo = EstadoPedido.flujo();
        boolean cancelado = p.getEstado() == EstadoPedido.CANCELADO;
        int actual = cancelado ? -1 : Arrays.asList(flujo).indexOf(p.getEstado());
        for (int i = 0; i < flujo.length; i++) {
            EstadoPedido e = flujo[i];
            StackPane nodo = new StackPane(Ico.of(i < actual ? "CHECK" : e.icono(), 17));
            nodo.getStyleClass().add("timeline-node");
            if (i < actual) nodo.getStyleClass().add("timeline-done");
            if (i == actual) nodo.getStyleClass().add(e == EstadoPedido.ENTREGADO ? "timeline-done" : "timeline-current");
            Label l = UI.wrap(e.nombre(), "timeline-label");
            l.setAlignment(Pos.CENTER);
            VBox paso = new VBox(6, nodo, l);
            paso.setAlignment(Pos.TOP_CENTER);
            paso.setPrefWidth(90);
            h.getChildren().add(paso);
            if (i < flujo.length - 1) {
                Region linea = new Region();
                linea.getStyleClass().add("timeline-line");
                if (i < actual) linea.getStyleClass().add("timeline-line-done");
                HBox.setHgrow(linea, Priority.ALWAYS);
                linea.setTranslateY(18);
                h.getChildren().add(linea);
            }
        }
        if (cancelado) {
            VBox v = new VBox(8, h, UI.banner("danger-banner", "CANCEL", "Pedido cancelado."));
            h.setOpacity(0.45);
            return v;
        }
        return h;
    }

    private Node acciones(Pedido p) {
        FlowPane f = new FlowPane(8, 8);
        boolean cerrado = p.getEstado().cerrado();
        if (!cerrado && p.getSaldo().signum() > 0) {
            Button adel = UI.secondary(p.getAdelanto().signum() == 0 ? "Registrar adelanto" : "Registrar pago", "PAYMENTS");
            adel.setOnAction(e -> registrarPago(p));
            f.getChildren().add(adel);
        }
        if (!cerrado) {
            EstadoPedido sig = p.getEstado().siguiente();
            if (sig == EstadoPedido.EN_PRODUCCION && p.getOpCodigo() == null) {
                Button op = UI.button("Generar OP", "BUILD", "btn-wood");
                op.setOnAction(e -> generarOP(p));
                f.getChildren().add(op);
            } else if (sig != p.getEstado()) {
                Button av = UI.primary("Pasar a " + sig.nombre().toLowerCase(), "ARROW_FORWARD");
                if (sig == EstadoPedido.LISTO && p.getOpCodigo() != null) {
                    produccion.porCodigo(p.getOpCodigo()).filter(op -> op.getEtapa() != Etapa.LISTO).ifPresent(op -> {
                        av.setDisable(true);
                        av.setText("Listo cuando " + op.getCodigo() + " termine");
                    });
                }
                av.setOnAction(e -> {
                    if (toast.intentar(() -> pedidos.avanzar(p.getId()))) {
                        toast.exito(p.getCodigo() + " → " + sig.nombre() + ".");
                        navigator.datosCambiaron();
                        seleccionar(p.getId());
                    }
                });
                f.getChildren().add(av);
            }
            if (p.getOpCodigo() == null && (p.getEstado() == EstadoPedido.REGISTRADO || p.getEstado() == EstadoPedido.COTIZADO)) {
                Button op = UI.button("Generar OP", "BUILD", "btn-ghost");
                op.setOnAction(e -> generarOP(p));
                f.getChildren().add(op);
            }
        }
        Button pdf = UI.secondary("Cotización PDF", "PICTURE_AS_PDF");
        pdf.setOnAction(e -> cotizacion(p));
        Button wsp = UI.secondary("WhatsApp", "CHAT");
        wsp.setOnAction(e -> whatsapp(p));
        f.getChildren().addAll(pdf, wsp);
        if (!cerrado) {
            Button cancelar = UI.button("Cancelar", "CANCEL", "btn-danger-soft");
            cancelar.setOnAction(e -> confirm.mostrar("¿Cancelar " + p.getCodigo() + "?",
                    "El pedido de " + p.getCliente().getNombre() + " quedará cancelado" + (p.getOpCodigo() != null ? " y la orden " + p.getOpCodigo() + " se anulará liberando sus reservas." : ".")
                            + (p.getAdelanto().signum() > 0 ? " Recuerda gestionar la devolución del adelanto de " + Money.fmt(p.getAdelanto()) + "." : ""),
                    "Cancelar pedido", () -> {
                        if (toast.intentar(() -> pedidos.cancelar(p.getId()))) {
                            toast.exito(p.getCodigo() + " cancelado.");
                            navigator.datosCambiaron();
                            seleccionar(p.getId());
                        }
                    }));
            f.getChildren().add(cancelar);
        }
        return f;
    }

    private Node pagos(Pedido p) {
        ProgressBarMini barra = new ProgressBarMini(p.getPctPagado(), p.getPctPagado() >= 1 ? "#16A34A" : "#2563EB", 8);
        VBox v = UI.card(UI.cardHeader("Pagos", String.format("%.0f %% pagado", p.getPctPagado() * 100)),
                UI.hbox(8, UI.miniStat("Precio", Money.fmt0(p.getPrecio())), UI.miniStat("A cuenta", Money.fmt0(p.getAdelanto())), UI.miniStat("Saldo", Money.fmt0(p.getSaldo()))),
                barra);
        if (!PedidoService.cubreMinimo(p) && !p.getEstado().cerrado()) {
            v.getChildren().add(UI.banner("lock-banner", "INFO", "Para confirmar se necesita un adelanto mínimo del 50 % (" + Money.fmt(p.getPrecio().multiply(PedidoService.MIN_ADELANTO)) + ")."));
        }
        for (Pago pago : pedidos.pagos(p)) {
            v.getChildren().add(UI.hbox(8, Ico.of("PAYMENTS", 16), new VBox(0, UI.label(pago.getConcepto() + " · " + pago.getMetodo(), "cell-title"),
                    UI.label(AppClock.fmt(pago.getFecha()), "cell-sub")), UI.spacer(), UI.label(Money.fmt(pago.getMonto()), "cell-title")));
        }
        return v;
    }

    private Node ordenVinculada(Pedido p) {
        VBox v = UI.card(UI.cardHeader("Orden de producción", null));
        if (p.getOpCodigo() == null) {
            v.getChildren().add(UI.label("Aún sin orden de producción.", "muted"));
            return v;
        }
        produccion.porCodigo(p.getOpCodigo()).ifPresent(op -> {
            Hyperlink l = new Hyperlink(op.getCodigo() + " · " + op.getProductoNombre());
            l.setOnAction(e -> navigator.goTo("produccion", Intent.abrir(op.getCodigo())));
            Region punto = new Region();
            punto.getStyleClass().add("legend-dot");
            punto.setStyle("-fx-background-color: " + op.getEtapa().color() + ";");
            var ind = produccion.indicador(op);
            v.getChildren().addAll(l, UI.hbox(8, punto, UI.label(op.getEtapa().nombre() + " · etapa " + op.getEtapa().paso() + " de 10", "cell-title")),
                    new ProgressBarMini(op.getEtapa().avance(), op.getEtapa().color(), 6),
                    UI.hbox(8, new StatusBadge(ind.texto(), ind.variante()), UI.spacer(),
                            UI.label("Responsable: " + (op.getResponsable() == null ? "—" : op.getResponsable().getNombre()), "cell-sub")));
        });
        return v;
    }

    // ================================================================ acciones

    private void registrarPago(Pedido p) {
        Validacion v = new Validacion();
        BigDecimal minimo = p.getPrecio().multiply(PedidoService.MIN_ADELANTO).subtract(p.getAdelanto()).max(BigDecimal.ZERO);
        TextField monto = Forms.decimal(minimo.signum() > 0 ? minimo : p.getSaldo());
        ComboBox<String> metodo = Forms.combo(List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin"), "Efectivo", s -> s);
        VBox cuerpo = new VBox(12, UI.hbox(8, UI.miniStat("Saldo", Money.fmt(p.getSaldo())), UI.miniStat("Para el 50 %", Money.fmt(minimo))),
                v.campo("Monto (S/)", monto, () -> {
                    String e = Validacion.numero(monto, 0.01, "El monto");
                    if (e != null) return e;
                    return Validacion.valor(monto) > p.getSaldo().doubleValue() + 0.001 ? "Supera el saldo de " + Money.fmt(p.getSaldo()) + "." : null;
                }), v.campo("Método de pago", metodo, null));
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Registrar", "PAYMENTS");
        v.enlazar(ok);
        var h = overlays.modal("Registrar pago · " + p.getCodigo(), p.getCliente().getNombre(), cuerpo, 460, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            Pedido[] r = new Pedido[1];
            if (toast.intentar(() -> r[0] = pedidos.registrarAdelanto(p.getId(), Forms.dinero(monto), metodo.getValue()))) {
                h.cerrar();
                toast.exito("Pago de " + Money.fmt(Forms.dinero(monto)) + " registrado" + (r[0].getEstado() != p.getEstado() ? " · pedido " + r[0].getEstado().nombre().toLowerCase() : "") + ".");
                navigator.datosCambiaron();
                seleccionar(p.getId());
            }
        });
    }

    private void generarOP(Pedido p) {
        ComboBox<Empleado> resp = Forms.combo(empleados.taller(), empleados.taller().isEmpty() ? null : empleados.taller().get(0), Empleado::getNombre);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Generar OP", "BUILD");
        var h = overlays.modal("Generar orden de producción", "Usa la ficha aprobada del modelo y pasa el pedido a «En producción»",
                new VBox(12, UI.wrap(p.getProductoNombre() + " × " + p.getCantidad() + (p.getTela() == null ? "" : " · " + p.getTela().getNombre())
                        + (p.getLargo() != null ? " · " + p.getMedidas() : ""), "muted"), UI.field("Responsable", resp)), 480, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            OrdenProduccion[] op = new OrdenProduccion[1];
            if (toast.intentar(() -> op[0] = pedidos.generarOP(p.getId(), resp.getValue() == null ? null : resp.getValue().getId()))) {
                h.cerrar();
                toast.exito(op[0].getCodigo() + " creada en Pendiente" + (op[0].isSinFicha() ? " (sin ficha aprobada)" : " con la ficha " + op[0].getFichaUid().replace("v", " v"))
                        + " · " + p.getCodigo() + " en producción.");
                navigator.datosCambiaron();
                seleccionar(p.getId());
            }
        });
    }

    private void cotizacion(Pedido p) {
        File f = Archivos.guardar(detalle, "Cotizacion-" + p.getCodigo(), "PDF", "pdf");
        if (f == null) return;
        Tareas.ejecutar(overlays, toast, "Generando cotización…", () -> {
            export.cotizacionPedido(p, f);
            return f;
        }, a -> {
            toast.exito("Cotización guardada: " + a.getName());
            appWindow.hostServices().showDocument(a.toURI().toString());
        });
    }

    private void whatsapp(Pedido p) {
        String msg = pedidos.mensajeWhatsapp(p);
        Label texto = UI.wrap(msg);
        VBox burbuja = new VBox(texto);
        burbuja.getStyleClass().add("whatsapp-box");
        Button copiar = UI.secondary("Copiar mensaje", "CONTENT_COPY");
        Button enviar = UI.button("Enviar por WhatsApp", "SEND", "btn-success");
        Button cerrar = UI.secondary("Cerrar", null);
        var h = overlays.modal("Enviar por WhatsApp (simulado)", "A " + p.getCliente().getNombre() + " · " + p.getCliente().getTelefono(),
                new VBox(12, burbuja, UI.label("No se envía ningún mensaje real: es una simulación.", "cell-sub")), 520, copiar, UI.spacer(), cerrar, enviar);
        copiar.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(msg);
            Clipboard.getSystemClipboard().setContent(cc);
            toast.exito("Mensaje copiado.");
        });
        cerrar.setOnAction(e -> h.cerrar());
        enviar.setOnAction(e -> {
            h.cerrar();
            toast.exito("Mensaje enviado a " + p.getCliente().getTelefono() + " (simulado).");
        });
    }

    private void nuevo(Cliente preCliente) {
        Validacion v = new Validacion();
        ComboBox<Cliente> cliente = Forms.combo(clientes.listar(), preCliente, c -> c.getNombre() + " · " + c.getDocumento());
        ComboBox<Producto> producto = Forms.combo(productos.listar(), null, p -> p.getCodigo() + " · " + p.getNombre());
        ComboBox<Pedido.Tipo> tipo = Forms.combo(List.of(Pedido.Tipo.values()), Pedido.Tipo.PERSONALIZADO, Pedido.Tipo::nombre);
        Spinner<Integer> cantidad = new Spinner<>(1, 50, 1);
        cantidad.setEditable(true);
        cantidad.setMaxWidth(Double.MAX_VALUE);
        TextField largo = Forms.entero((Integer) null), fondo = Forms.entero((Integer) null), alto = Forms.entero((Integer) null);
        largo.setPromptText("Largo");
        fondo.setPromptText("Fondo");
        alto.setPromptText("Alto");
        List<Tela> ts = new ArrayList<>();
        ts.add(null);
        ts.addAll(telas.activas());
        ComboBox<Tela> tela = Forms.combo(ts, null, t -> t == null ? "Según el modelo" : t.getNombre() + " · " + Money.numero(t.getStock()) + " m");
        DatePicker entrega = Forms.fecha(AppClock.hoy().plusDays(20));
        TextField precio = Forms.decimal(BigDecimal.ZERO);
        TextField adelanto = Forms.decimal(BigDecimal.ZERO);
        ComboBox<String> metodo = Forms.combo(List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin"), "Yape", s -> s);
        TextArea obs = Forms.area("", "Observaciones (piso, acceso, horario…)", 2);
        Runnable sugerir = () -> {
            if (producto.getValue() == null) return;
            Producto p = producto.getValue();
            if (largo.getText().isBlank() && p.getLargo() != null && p.getForma() != Forma.JUEGO) {
                largo.setText(String.valueOf(p.getLargo()));
                fondo.setText(String.valueOf(p.getFondo()));
                alto.setText(String.valueOf(p.getAlto()));
            }
            Integer l = largo.getText().isBlank() ? null : Forms.entero(largo);
            precio.setText(pedidos.precioSugerido(p, l, tela.getValue(), cantidad.getValue()).toPlainString());
        };
        producto.valueProperty().addListener((o, a, b) -> {
            largo.clear();
            sugerir.run();
        });
        tela.valueProperty().addListener((o, a, b) -> sugerir.run());
        cantidad.valueProperty().addListener((o, a, b) -> sugerir.run());
        largo.focusedProperty().addListener((o, a, b) -> {
            if (!b) sugerir.run();
        });
        FormGrid f = new FormGrid(3);
        f.seccion("1", "Cliente y modelo");
        f.nodo(v.campo("Cliente", cliente, () -> Validacion.requerido(cliente, "el cliente")), 2);
        f.nodo(v.campo("Tipo", tipo, null), 1);
        f.nodo(v.campo("Modelo", producto, () -> Validacion.requerido(producto, "el modelo")), 2);
        f.nodo(v.campo("Cantidad", cantidad, null), 1);
        f.seccion("2", "Medidas y tela");
        f.nodo(v.campo("Largo (cm)", largo, () -> largo.getText().isBlank() ? null : Validacion.numero(largo, 40, "El largo")), 1);
        f.nodo(v.campo("Fondo (cm)", fondo, () -> fondo.getText().isBlank() ? null : Validacion.numero(fondo, 40, "El fondo")), 1);
        f.nodo(v.campo("Alto (cm)", alto, () -> alto.getText().isBlank() ? null : Validacion.numero(alto, 40, "El alto")), 1);
        f.nodo(v.campo("Tela y color", tela, null), 3);
        f.seccion("3", "Entrega y pago");
        f.nodo(v.campo("Fecha de entrega", entrega, () -> entrega.getValue() == null || entrega.getValue().isBefore(AppClock.hoy()) ? "Elige una fecha futura." : null), 1);
        f.nodo(v.campo("Precio (S/)", precio, () -> Validacion.numero(precio, 1, "El precio")), 1);
        f.nodo(v.campo("Adelanto (S/)", adelanto, () -> {
            String e = Validacion.numero(adelanto, 0, "El adelanto");
            if (e != null) return e;
            return Validacion.valor(adelanto) > Validacion.valor(precio) ? "No puede superar el precio." : null;
        }), 1);
        f.nodo(v.campo("Método del adelanto", metodo, null), 1);
        f.nodo(v.campo("Observaciones", obs, null), 2);
        v.escuchar(precio.textProperty());
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Registrar pedido", "SAVE");
        v.enlazar(ok);
        var h = overlays.modal("Nuevo pedido", "Precio sugerido según el modelo, el largo y el recargo de la tela", f, 680, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            Pedido[] r = new Pedido[1];
            Integer l = largo.getText().isBlank() ? null : Forms.entero(largo);
            if (toast.intentar(() -> r[0] = pedidos.crear(new PedidoService.NuevoPedido(cliente.getValue().getId(), producto.getValue().getId(),
                    tipo.getValue(), cantidad.getValue(), l, l == null ? null : Forms.entero(fondo), l == null ? null : Forms.entero(alto),
                    tela.getValue() == null ? null : tela.getValue().getId(), entrega.getValue(), Forms.dinero(precio), Forms.dinero(adelanto),
                    metodo.getValue(), obs.getText(), null, null)))) {
                h.cerrar();
                toast.exito(r[0].getCodigo() + " registrado para " + r[0].getCliente().getNombre() + " · " + r[0].getEstado().nombre() + ".");
                navigator.datosCambiaron();
                seleccionar(r[0].getId());
            }
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
