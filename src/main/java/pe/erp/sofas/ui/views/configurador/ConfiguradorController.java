/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConfiguradorController
 * Descripción: Configurador 3D: visor (≈65 %), pasos 1–8 a la derecha y barra de precio abajo. Cada cambio reconstruye el sofa
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
package pe.erp.sofas.ui.views.configurador;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.model.ConfigSofa.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.AppWindow;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.SofaBuilder;
import pe.erp.sofas.ui.three.Viewer3D;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * Configurador 3D: visor (≈65 %), pasos 1–8 a la derecha y barra de precio abajo. Cada cambio reconstruye el sofa
 * y recalcula el precio desde la ficha aprobada; guarda hasta 3 variantes A/B/C para compararlas.
 */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConfiguradorController implements Vista.ConCiclo {

    private final ConfiguradorService configurador;
    private final TelaService telas;
    private final ProductoService productos;
    private final ClienteService clientes;
    private final EmpleadoService empleados;
    private final ExportService export;
    private final Overlays overlays;
    private final Toast toast;
    private final Navigator navigator;
    private final AppWindow appWindow;

    @FXML private VBox raiz;

    private final Viewer3D visor = new Viewer3D(true);
    private final SofaBuilder builder = new SofaBuilder();
    private final VBox pasos = new VBox();
    private final HBox barraPrecio = new HBox();
    private final VBox desglose = new VBox();
    private ConfigSofa config;
    private ConfiguradorService.Cotizacion cotizacion;
    private final ConfigSofa[] variantes = new ConfigSofa[3];
    private final HBox variantesBox = new HBox(6);
    private Button btnGiro;
    private ScrollPane spPasos;

    @FXML
    public void initialize() {
        config = configurador.defecto(Modelo.MODERNO);
        variantesBox.getStyleClass().add("viewer-toolbar");
        variantesBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(variantesBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(variantesBox, new Insets(12));
        StackPane visorHost = new StackPane(visor, toolbar(), pista(), variantesBox);
        visorHost.getStyleClass().add("viewer-host");
        visor.setMinSize(200, 200);
        spPasos = UI.scroll(pasos);
        VBox panelPasos = new VBox(spPasos);
        VBox.setVgrow(spPasos, Priority.ALWAYS);
        panelPasos.getStyleClass().add("card");
        panelPasos.setStyle("-fx-padding: 0;");
        GridPane arriba = UI.columnas(new double[]{65, 35}, visorHost, panelPasos);
        arriba.setMinHeight(0);
        RowConstraints rc = new RowConstraints();
        rc.setVgrow(Priority.ALWAYS);
        arriba.getRowConstraints().add(rc);
        VBox.setVgrow(arriba, Priority.ALWAYS);
        barraPrecio.getStyleClass().addAll("price-bar", "card");
        barraPrecio.setStyle("-fx-background-radius: 14px;");
        desglose.setVisible(false);
        desglose.setManaged(false);
        raiz.getChildren().setAll(arriba, desglose, barraPrecio);
    }

    @Override
    public void onShow(Intent intent) {
        if (intent.es(Intent.ACCION_PRESELECCIONAR) && intent.dato() != null) {
            productos.porCodigo(intent.dato().toString()).ifPresent(this::desdeProducto);
        } else if (intent.dato() instanceof ConfigSofa c) {
            config = c;
        }
        actualizar(true);
    }

    @Override
    public void onHide() {
        visor.detenerGiro();
        if (btnGiro != null) btnGiro.getStyleClass().remove("btn-primary");
    }

    private void desdeProducto(Producto p) {
        Modelo m = switch (p.getForma()) {
            case SECCIONAL_L, SECCIONAL_U -> Modelo.SECCIONAL_L;
            case SILLON -> Modelo.SILLON;
            case CHESTERFIELD -> Modelo.CHESTERFIELD;
            case SOFA_CAMA -> Modelo.SOFA_CAMA;
            case ESCANDINAVO -> Modelo.ESCANDINAVO;
            default -> Modelo.MODERNO;
        };
        ConfigSofa c = configurador.defecto(m);
        if (m == Modelo.MODERNO && p.getForma() != Forma.JUEGO) c = c.conCuerpos(Math.max(1, Math.min(4, p.getCuerpos())));
        if (p.getForma() == Forma.RECLINABLE) c = c.conMecanismo(Mecanismo.RECLINABLE_MANUAL);
        Optional<Tela> t = telas.activas().stream().filter(x -> x.getColor().equalsIgnoreCase(p.getColor())
                && x.getTipo().nombre().toLowerCase().startsWith(p.getMaterial().toLowerCase().split(" ")[0])).findFirst();
        if (t.isPresent()) c = c.conTela(t.get().getId());
        config = c;
        toast.info("Configurando a partir de " + p.getNombre() + ".");
    }

    // ================================================================ visor

    private Node toolbar() {
        HBox camaras = new HBox(4);
        for (Viewer3D.Camara c : Viewer3D.Camara.values()) {
            Button b = UI.button(c.nombre(), null, "btn-sm", "btn-ghost");
            b.setOnAction(e -> visor.camara(c));
            camaras.getChildren().add(b);
        }
        btnGiro = UI.iconButton("MDOAL_360", "Giro automático 360°", "btn-ghost");
        btnGiro.setOnAction(e -> {
            boolean g = visor.alternarGiro();
            if (g) btnGiro.getStyleClass().add("btn-primary");
            else btnGiro.getStyleClass().remove("btn-primary");
        });
        Button centrar = UI.iconButton("CENTER_FOCUS_STRONG", "Centrar (doble clic)", "btn-ghost");
        centrar.setOnAction(e -> visor.centrar(true));
        Button foto = UI.iconButton("PHOTO_CAMERA", "Captura PNG", "btn-ghost");
        foto.setOnAction(e -> capturaPng());
        Button full = UI.iconButton("FULLSCREEN", "Pantalla completa", "btn-ghost");
        full.setOnAction(e -> pantallaCompleta());
        HBox t = new HBox(4, camaras, new Separator(javafx.geometry.Orientation.VERTICAL), btnGiro, centrar, foto, full);
        t.getStyleClass().add("viewer-toolbar");
        t.setAlignment(Pos.CENTER_LEFT);
        t.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(t, Pos.TOP_LEFT);
        StackPane.setMargin(t, new Insets(12));
        return t;
    }

    private Node pista() {
        Label l = UI.label("Arrastra para girar · rueda para acercar · doble clic para centrar · Shift + arrastrar para mover", "viewer-hint");
        l.setMouseTransparent(true);
        StackPane.setAlignment(l, Pos.BOTTOM_LEFT);
        StackPane.setMargin(l, new Insets(12));
        return l;
    }

    private Tela tela() {
        return config.telaId() == null ? null : telas.tela(config.telaId());
    }

    private Tela telaCojines() {
        return config.telaCojinesId() == null ? tela() : telas.tela(config.telaCojinesId());
    }

    private void actualizar(boolean recentrar) {
        visor.setContenido(builder.build(SofaBuilder.Opciones.de(config, tela(), telaCojines())), recentrar);
        try {
            cotizacion = configurador.cotizar(config);
        } catch (NegocioException e) {
            toast.error(e.getMessage());
            cotizacion = null;
        }
        renderPasos();
        renderPrecio();
    }

    private void cambiar(ConfigSofa nuevo) {
        boolean recentrar = nuevo.cuerpos() != config.cuerpos() || nuevo.chaise() != config.chaise() || nuevo.modelo() != config.modelo()
                || nuevo.profundidad() != config.profundidad();
        config = nuevo;
        actualizar(recentrar);
    }

    // ================================================================ pasos

    private void renderPasos() {
        ScrollPane sp = spPasos;
        double pos = sp == null ? 0 : sp.getVvalue();
        pasos.getChildren().setAll(
                paso(1, "Modelo base", modelos()),
                paso(2, "Tamaño", tamano()),
                paso(3, "Tela y color", telaYColor()),
                paso(4, "Brazos", opciones(List.of(Brazo.values()), config.brazo(), Brazo::nombre, b -> null, b -> false, b -> cambiar(config.conBrazo(b)))),
                paso(5, "Patas", patas()),
                paso(6, "Confort", opciones(List.of(Espuma.values()), config.espuma(), Espuma::nombre, e -> e.bajoPedido() ? "bajo pedido" : null,
                        e -> false, e -> cambiar(config.conEspuma(e)))),
                paso(7, "Cojines decorativos", cojines()),
                paso(8, "Mecanismo", opciones(List.of(Mecanismo.values()), config.mecanismo(), Mecanismo::nombre, m -> switch (m) {
                    case RECLINABLE_MANUAL -> "un mecanismo por cuerpo";
                    case RECLINABLE_ELECTRICO -> "bajo pedido";
                    case CAMA -> "mecanismo + colchoneta";
                    default -> null;
                }, m -> false, m -> cambiar(config.conMecanismo(m)))));
        if (sp != null) javafx.application.Platform.runLater(() -> sp.setVvalue(pos));
    }

    private Node paso(int n, String titulo, Node contenido) {
        Label num = UI.label(String.valueOf(n), "cfg-step-num");
        VBox v = new VBox(10, UI.hbox(10, num, UI.label(titulo, "cfg-step-title")), contenido);
        v.getStyleClass().add("cfg-step");
        return v;
    }

    private <T> Node opciones(List<T> valores, T actual, java.util.function.Function<T, String> nombre, java.util.function.Function<T, String> sub,
                              java.util.function.Predicate<T> deshabilitar, java.util.function.Consumer<T> alElegir) {
        FlowPane f = new FlowPane(8, 8);
        ToggleGroup g = new ToggleGroup();
        for (T v : valores) {
            ToggleButton t = new ToggleButton(nombre.apply(v));
            String s = sub.apply(v);
            if (s != null) {
                t.setText(nombre.apply(v) + "\n" + s);
                t.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            }
            t.getStyleClass().add("opt-tile");
            t.setToggleGroup(g);
            t.setSelected(v.equals(actual));
            t.setDisable(deshabilitar.test(v));
            t.setMinWidth(Region.USE_PREF_SIZE);
            t.setOnAction(e -> {
                if (!v.equals(actual)) alElegir.accept(v);
                else t.setSelected(true);
            });
            f.getChildren().add(t);
        }
        return f;
    }

    private Node modelos() {
        return opciones(List.of(Modelo.values()), config.modelo(), Modelo::nombre, m -> "desde " + Money.fmt0(configurador.desde(m)),
                m -> false, m -> {
                    config = configurador.defecto(m);
                    actualizar(true);
                    toast.info(m.nombre() + ": valores por defecto aplicados" + (m == Modelo.CHESTERFIELD ? " (cuero cognac, capitoné)." : "."));
                });
    }

    private Node tamano() {
        Node cuerpos = opciones(List.of(1, 2, 3, 4), config.cuerpos(), n -> n + (n == 1 ? " cuerpo" : " cuerpos"),
                n -> ConfigSofa.largoPara(n) + " cm", n -> config.modelo() == Modelo.SILLON && n > 1, n -> cambiar(config.conCuerpos(n)));
        Node chaise = opciones(List.of(Chaise.values()), config.chaise(), Chaise::nombre, c -> null,
                c -> c != Chaise.NINGUNA && config.cuerpos() < 2, c -> cambiar(config.conChaise(c)));
        Node prof = opciones(List.of(92, 105), config.profundidad(), p -> p == 92 ? "Estándar" : "Profunda", p -> p + " cm",
                p -> false, p -> cambiar(config.conProfundidad(p)));
        return new VBox(8, UI.label("Cuerpos", "form-label"), cuerpos, UI.label("Chaise", "form-label"), chaise, UI.label("Profundidad", "form-label"), prof);
    }

    private Node telaYColor() {
        Tela actual = tela();
        TipoTela tipo = actual == null ? TipoTela.CHENILLE : actual.getTipo();
        Node tipos = opciones(List.of(TipoTela.values()), tipo, TipoTela::nombre, t -> "S/ " + (int) t.costoMetro() + "/m · +" + (int) t.recargoPct() + " %",
                t -> telas.porTipo(t).stream().allMatch(Tela::getDescontinuada), t -> telas.porTipo(t).stream().filter(x -> !x.getDescontinuada())
                        .max(Comparator.comparingDouble(Tela::getStock)).ifPresent(x -> cambiar(config.conTela(x.getId()))));
        FlowPane colores = new FlowPane(10, 10);
        for (Tela t : telas.porTipo(tipo)) {
            if (t.getDescontinuada()) continue;
            ColorSwatch sw = new ColorSwatch(t.getHex(), t.getTipo(), 34).seleccionada(actual != null && t.getId().equals(actual.getId()));
            Label nombre = UI.label(t.getColor(), "cell-sub");
            VBox caja = new VBox(4, sw, nombre);
            caja.setAlignment(Pos.CENTER);
            caja.setPrefWidth(64);
            caja.getStyleClass().add("clickable");
            Tooltip.install(caja, new Tooltip(t.getNombre() + " · " + Money.numero(t.getStock()) + " m · " + t.getEstado()));
            if (t.getStock() <= 0) caja.setOpacity(0.55);
            caja.setOnMouseClicked(e -> cambiar(config.conTela(t.getId())));
            colores.getChildren().add(caja);
        }
        double metros = cotizacion == null || actual == null ? 0 : cotizacion.lineas().stream()
                .filter(l -> l.material().getCodigo().equals(actual.getTipo().materialCodigo())).mapToDouble(ConfiguradorService.Linea::cantidad).sum();
        String nota = actual == null ? "" : telas.notaDisponibilidad(actual, metros);
        HBox notaBox = UI.banner(actual != null && actual.getStock() >= metros ? "ok-banner" : "danger-banner",
                actual != null && actual.getStock() >= metros ? "CHECK_CIRCLE" : "WARNING",
                (actual == null ? "" : actual.getNombre() + ": ") + nota + (metros > 0 ? " · este sofá usa " + Money.numero(metros) + " m" : ""));
        return new VBox(10, tipos, colores, notaBox);
    }

    private Node patas() {
        Node tipos = opciones(List.of(Patas.values()), config.patas(), Patas::nombre, p -> p.recargo() > 0 ? "+S/ " + (int) p.recargo() + " c/u"
                : p.bajoPedido() ? "bajo pedido" : null, p -> false, p -> cambiar(config.conPatas(p)));
        VBox v = new VBox(8, tipos);
        if (config.patas() == Patas.MADERA) {
            HBox acabados = new HBox(10);
            for (AcabadoMadera a : AcabadoMadera.values()) {
                ToggleButton t = new ToggleButton(a.nombre());
                t.getStyleClass().add("opt-tile");
                Region muestra = new Region();
                muestra.setMinSize(18, 18);
                muestra.setStyle("-fx-background-color: " + a.hex() + "; -fx-background-radius: 999;");
                t.setGraphic(muestra);
                t.setContentDisplay(ContentDisplay.LEFT);
                t.setSelected(a == config.acabado());
                t.setOnAction(e -> cambiar(config.conAcabado(a)));
                acabados.getChildren().add(t);
            }
            v.getChildren().addAll(UI.label("Acabado de la madera", "form-label"), acabados);
        }
        return v;
    }

    private Node cojines() {
        Node n = opciones(List.of(0, 2, 4), config.cojines(), c -> c == 0 ? "Sin cojines" : c + " cojines", c -> c == 0 ? null : "0.6 m de tela c/u",
                c -> false, c -> cambiar(config.conCojines(c, config.telaCojinesId())));
        VBox v = new VBox(8, n);
        if (config.cojines() > 0) {
            FlowPane colores = new FlowPane(8, 8);
            Tela actual = telaCojines();
            for (Tela t : telas.activas()) {
                if (t.getStock() <= 0) continue;
                ColorSwatch sw = new ColorSwatch(t.getHex(), t.getTipo(), 26).seleccionada(actual != null && actual.getId().equals(t.getId()));
                Tooltip.install(sw, new Tooltip(t.getNombre()));
                sw.getStyleClass().add("clickable");
                sw.setOnMouseClicked(e -> cambiar(config.conCojines(config.cojines(), t.getId())));
                colores.getChildren().add(sw);
            }
            v.getChildren().addAll(UI.label("Color de los cojines" + (actual == null ? "" : " · " + actual.getNombre()), "form-label"), colores);
        }
        return v;
    }

    // ================================================================ precio

    private void renderPrecio() {
        if (cotizacion == null) {
            barraPrecio.getChildren().setAll(UI.label("No se pudo cotizar esta configuración.", "muted"));
            return;
        }
        ConfiguradorService.Cotizacion c = cotizacion;
        VBox precio = fijo(new VBox(0, UI.label("PRECIO DE VENTA", "price-label"), UI.label(Money.fmt0(c.precio()), "price-big")));
        VBox costo = fijo(new VBox(1, UI.label("Costo estándar", "price-label"), UI.label(Money.fmt(c.costo()), "price-value"), UI.label(c.referencia(), "code")));
        VBox margen = fijo(new VBox(2, UI.label("Margen", "price-label"), new StatusBadge(String.format("%.1f %%", c.margenPct()), c.margenPct() >= 35 ? "green" : "red")));
        Label diasL = UI.label(c.dias() + " días", "price-value");
        VBox dias = fijo(new VBox(2, UI.label("Fabricación", "price-label"), diasL));
        List<String> avisos = new ArrayList<>();
        if (c.bajoPedido()) avisos.add("+4 días por material bajo pedido");
        if (c.critico()) avisos.add("+5 días por material crítico");
        if (!avisos.isEmpty()) Tooltip.install(dias, new Tooltip(String.join("\n", avisos)));
        if (!avisos.isEmpty()) diasL.setStyle("-fx-text-fill: -is-warn-ink;");
        ToggleButton verDesglose = new ToggleButton("Desglose");
        verDesglose.setMinWidth(Region.USE_PREF_SIZE);
        verDesglose.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        verDesglose.setGraphic(Ico.of("TABLE_VIEW", 16));
        verDesglose.setSelected(desglose.isVisible());
        verDesglose.setOnAction(e -> {
            desglose.setVisible(verDesglose.isSelected());
            desglose.setManaged(verDesglose.isSelected());
            if (verDesglose.isSelected()) renderDesglose();
        });
        if (desglose.isVisible()) renderDesglose();
        renderVariantes();
        Button sala = UI.secondary("Ver en mi sala", "MEETING_ROOM");
        sala.setOnAction(e -> navigator.goTo("sala", new Intent("AGREGAR", config)));
        Button pdf = UI.secondary("Cotización", "PICTURE_AS_PDF");
        UI.tooltip(pdf, "Cotización en PDF con el render 3D");
        pdf.setOnAction(e -> cotizacionPdf());
        Button pedido = UI.primary("Crear pedido", "ASSIGNMENT");
        pedido.setOnAction(e -> crearPedido());
        barraPrecio.setSpacing(16);
        barraPrecio.getChildren().setAll(precio, costo, margen, dias, verDesglose, UI.spacer(), sala, pdf, pedido);
    }

    private static VBox fijo(VBox v) {
        v.setMinWidth(Region.USE_PREF_SIZE);
        return v;
    }

    private void renderDesglose() {
        ConfiguradorService.Cotizacion c = cotizacion;
        TableView<ConfiguradorService.Linea> t = Cells.tabla("Sin materiales.");
        t.getColumns().addAll(List.of(
                Cells.nodo("Material", l -> new VBox(0, UI.label(l.material().getNombre(), "cell-title"), UI.label(l.material().getCodigo() + " · " + l.componente(), "cell-sub")), 280),
                Cells.derecha("Cantidad", l -> Money.numero(l.cantidad()) + " " + l.material().getUnidad(), 110),
                Cells.derecha("Costo", l -> Money.fmt(l.costo()), 100),
                Cells.badge("Stock", ConfiguradorService.Linea::estado, 110)));
        t.getItems().setAll(c.lineas());
        t.setFixedCellSize(40);
        t.setPrefHeight(Math.min(6, c.lineas().size()) * 40 + 42);
        VBox totales = new VBox(6,
                UI.hbox(8, UI.label("Materiales", "muted"), UI.spacer(), UI.label(Money.fmt(c.costoMateriales()), "cell-title")),
                UI.hbox(8, UI.label("Mano de obra (" + Money.numero(c.horasMO()) + " h)", "muted"), UI.spacer(), UI.label(Money.fmt(c.costoMO()), "cell-title")),
                UI.hbox(8, UI.label("Indirectos 6 %", "muted"), UI.spacer(), UI.label(Money.fmt(c.indirectos()), "cell-title")),
                UI.hbox(8, UI.label("Costo estándar", "card-title"), UI.spacer(), UI.label(Money.fmt(c.costo()), "card-title")),
                UI.hbox(8, UI.label("Precio = costo × 1.55 a la decena", "cell-sub"), UI.spacer(), UI.label(Money.fmt(c.precio()), "card-title")));
        totales.setPrefWidth(280);
        HBox h = new HBox(16, t, totales);
        HBox.setHgrow(t, Priority.ALWAYS);
        VBox card = UI.card(h);
        desglose.getChildren().setAll(card);
    }

    private void renderVariantes() {
        variantesBox.getChildren().clear();
        variantesBox.setAlignment(Pos.CENTER);
        String[] letras = {"A", "B", "C"};
        for (int i = 0; i < 3; i++) {
            int idx = i;
            Button b = UI.button(letras[i], null, "btn-sm", variantes[i] == null ? "btn-ghost" : "btn-wood");
            b.setStyle("-fx-padding: 0 10;");
            UI.tooltip(b, variantes[i] == null ? "Guardar la configuración actual como variante " + letras[i]
                    : "Variante " + letras[i] + ": " + configurador.especificacion(variantes[i]) + "\nClic: cargar · clic derecho: reemplazar");
            b.setOnAction(e -> {
                if (variantes[idx] == null) {
                    variantes[idx] = config;
                    toast.exito("Variante " + letras[idx] + " guardada.");
                    renderVariantes();
                } else {
                    config = variantes[idx];
                    actualizar(true);
                }
            });
            b.setOnContextMenuRequested(e -> {
                variantes[idx] = config;
                toast.exito("Variante " + letras[idx] + " reemplazada.");
                renderVariantes();
            });
            variantesBox.getChildren().add(b);
        }
        Button comparar = UI.iconButton("COMPARE", "Comparar variantes lado a lado", "btn-ghost");
        comparar.setDisable(Arrays.stream(variantes).filter(Objects::nonNull).count() < 2);
        comparar.setOnAction(e -> comparar());
        Label tit = UI.label("Variantes", "price-label");
        tit.setPadding(new Insets(0, 4, 0, 6));
        variantesBox.getChildren().add(0, tit);
        variantesBox.getChildren().add(comparar);
    }

    private void comparar() {
        HBox cols = new HBox(14);
        String[] letras = {"A", "B", "C"};
        for (int i = 0; i < 3; i++) {
            ConfigSofa v = variantes[i];
            if (v == null) continue;
            ConfiguradorService.Cotizacion c = configurador.cotizar(v);
            Tela t = v.telaId() == null ? null : telas.tela(v.telaId());
            Tela tc = v.telaCojinesId() == null ? t : telas.tela(v.telaCojinesId());
            WritableImage img = Viewer3D.render(new SofaBuilder().build(SofaBuilder.Opciones.de(v, t, tc)), 300, 200, -30, 16);
            ImageSlot foto = new ImageSlot("", 300, 200, 12).soloLectura();
            foto.mostrar(img);
            int idx = i;
            Button usar = UI.secondary("Usar esta", null);
            usar.setMaxWidth(Double.MAX_VALUE);
            VBox col = new VBox(10, UI.label("Variante " + letras[i], "card-title"), foto,
                    UI.hbox(8, UI.label("Precio", "muted"), UI.spacer(), UI.label(Money.fmt0(c.precio()), "price-value")),
                    UI.hbox(8, UI.label("Fabricación", "muted"), UI.spacer(), UI.label(c.dias() + " días", "cell-title")),
                    UI.hbox(8, UI.label("Tela", "muted"), UI.spacer(), t == null ? UI.label("—") : UI.hbox(6, new ColorSwatch(t.getHex(), t.getTipo(), 16), UI.label(t.getNombre(), "cell-title"))),
                    UI.wrap(configurador.especificacion(v), "cell-sub"), usar);
            col.getStyleClass().add("variant-card");
            col.setPrefWidth(320);
            cols.getChildren().add(col);
            usar.setOnAction(e -> {
                config = variantes[idx];
                actualizar(true);
                overlays.cerrarTodos();
            });
        }
        Button cerrar = UI.secondary("Cerrar", null);
        var h = overlays.modal("Comparar variantes", "Precio, días de fabricación y tela lado a lado", cols, 1040, cerrar);
        cerrar.setOnAction(e -> h.cerrar());
    }

    // ================================================================ acciones

    private byte[] png(double escala) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(Snapshot.imagen(visor.captura(escala)), "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void capturaPng() {
        File f = Archivos.guardar(visor, "Sofa-" + config.modelo().nombre().replace(" ", "-"), "Imagen PNG", "png");
        if (f == null) return;
        if (toast.intentar(() -> {
            try {
                ImageIO.write(Snapshot.imagen(visor.captura(2)), "png", f);
            } catch (java.io.IOException e) {
                throw new NegocioException("No se pudo guardar la captura.");
            }
        })) toast.exito("Captura guardada: " + f.getName());
    }

    private void pantallaCompleta() {
        Viewer3D v = new Viewer3D(true);
        v.setContenido(new SofaBuilder().build(SofaBuilder.Opciones.de(config, tela(), telaCojines())), true);
        Label ayuda = UI.label("Esc para salir · arrastra para girar · rueda para acercar", "viewer-hint");
        StackPane.setAlignment(ayuda, Pos.BOTTOM_CENTER);
        StackPane.setMargin(ayuda, new Insets(20));
        StackPane root = new StackPane(v, ayuda);
        root.getStyleClass().add("viewer3d");
        Scene s = new Scene(root, 1280, 800, true);
        s.getStylesheets().setAll(appWindow.scene().getStylesheets());
        Stage st = new Stage();
        st.initOwner(appWindow.stage());
        st.setTitle("Configurador 3D · " + configurador.especificacion(config));
        st.setScene(s);
        st.setFullScreenExitHint("");
        st.setFullScreen(true);
        s.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) st.close();
        });
        st.show();
        javafx.application.Platform.runLater(() -> v.centrar(false));
    }

    private void cotizacionPdf() {
        if (cotizacion == null) return;
        List<Cliente> cs = new ArrayList<>();
        cs.add(null);
        cs.addAll(clientes.listar());
        ComboBox<Cliente> cliente = Forms.combo(cs, null, c -> c == null ? "Cliente por definir" : c.getNombre());
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Generar PDF", "PICTURE_AS_PDF");
        var h = overlays.modal("Cotización PDF", configurador.especificacion(config), UI.field("Cliente", cliente), 480, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            h.cerrar();
            File f = Archivos.guardar(visor, "Cotizacion-sofa-a-medida", "PDF", "pdf");
            if (f == null) return;
            byte[] imagen = png(2);
            ConfiguradorService.Cotizacion c = cotizacion;
            String espec = configurador.especificacion(config);
            String nombre = cliente.getValue() == null ? "Por definir" : cliente.getValue().getNombre();
            Tareas.ejecutar(overlays, toast, "Generando cotización…", () -> {
                export.cotizacionConfigurador(c, espec, nombre, imagen, f);
                return f;
            }, a -> {
                toast.exito("Cotización guardada: " + a.getName());
                appWindow.hostServices().showDocument(a.toURI().toString());
            });
        });
    }

    private void crearPedido() {
        if (cotizacion == null) return;
        Validacion v = new Validacion();
        ComboBox<Cliente> cliente = Forms.combo(clientes.listar(), null, Cliente::getNombre);
        DatePicker entrega = Forms.fecha(AppClock.hoy().plusDays(cotizacion.dias()));
        BigDecimal precio = BigDecimal.valueOf(cotizacion.precio());
        TextField adelanto = Forms.decimal(precio.divide(BigDecimal.valueOf(2)).setScale(0, java.math.RoundingMode.UP));
        ComboBox<String> metodo = Forms.combo(List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin"), "Efectivo", s -> s);
        TextArea obs = Forms.area("", "Observaciones (piso, acceso, horario de entrega…)", 2);
        CheckBox crearOP = new CheckBox("Crear también la orden de producción (guarda la ficha " + cotizacion.ficha().getEtiqueta() + " y las sustituciones)");
        crearOP.setSelected(true);
        crearOP.setWrapText(true);
        ComboBox<Empleado> resp = Forms.combo(empleados.taller(), empleados.taller().isEmpty() ? null : empleados.taller().get(0), Empleado::getNombre);
        resp.disableProperty().bind(crearOP.selectedProperty().not());
        FormGrid f = new FormGrid(2);
        f.nodo(UI.wrap(configurador.especificacion(config), "muted"), 2);
        f.nodo(v.campo("Cliente", cliente, () -> Validacion.requerido(cliente, "el cliente")), 2);
        f.nodo(v.campo("Entrega", entrega, () -> entrega.getValue() == null || entrega.getValue().isBefore(AppClock.hoy()) ? "Elige una fecha futura." : null), 1);
        f.nodo(v.campo("Precio", Forms.soloLectura(Money.fmt(precio)), null), 1);
        f.nodo(v.campo("Adelanto (S/)", adelanto, () -> {
            String e = Validacion.numero(adelanto, 0, "El adelanto");
            if (e != null) return e;
            return Validacion.valor(adelanto) > precio.doubleValue() ? "El adelanto no puede superar el precio." : null;
        }), 1);
        f.nodo(v.campo("Método", metodo, null), 1);
        f.nodo(v.campo("Observaciones", obs, null), 2);
        f.nodo(crearOP, 2);
        f.nodo(v.campo("Responsable de la OP", resp, null), 2);
        Label nota = UI.label("", "cell-sub");
        Runnable n = () -> nota.setText(Validacion.valor(adelanto) >= precio.doubleValue() / 2 ? "Con 50 % o más el pedido queda Confirmado."
                : "Con menos del 50 % el pedido queda Registrado y la OP se crea en Pendiente.");
        adelanto.textProperty().addListener((o, a, b) -> n.run());
        n.run();
        f.nodo(nota, 2);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Crear pedido", "ASSIGNMENT");
        v.enlazar(ok);
        var h = overlays.modal("Crear pedido personalizado", Money.fmt(precio) + " · " + cotizacion.dias() + " días de fabricación", f, 620, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            Pedido[] p = new Pedido[1];
            if (toast.intentar(() -> p[0] = configurador.crearPedido(config, cliente.getValue().getId(), entrega.getValue(),
                    Forms.dinero(adelanto), metodo.getValue(), obs.getText(), crearOP.isSelected(), resp.getValue() == null ? null : resp.getValue().getId()))) {
                h.cerrar();
                toast.exito(p[0].getCodigo() + " creado para " + cliente.getValue().getNombre() + (p[0].getOpCodigo() != null ? " · " + p[0].getOpCodigo() + " en Pendiente" : "") + ".");
                navigator.datosCambiaron();
                navigator.goTo("pedidos", Intent.abrir(p[0].getCodigo()));
            }
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
