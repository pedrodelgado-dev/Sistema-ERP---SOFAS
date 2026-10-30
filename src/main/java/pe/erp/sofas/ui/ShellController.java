/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ShellController
 * Descripción: Ventana principal tras el login: menu lateral (248 px, colapsable a 72), barra superior (titulo del modulo,
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
package pe.erp.sofas.ui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.service.BusquedaService;
import pe.erp.sofas.service.GarantiaService;
import pe.erp.sofas.service.InventarioService;
import pe.erp.sofas.service.NotificacionService;
import pe.erp.sofas.ui.components.*;

import java.util.*;

/**
 * Ventana principal tras el login: menu lateral (248 px, colapsable a 72), barra superior (titulo del modulo,
 * Regresar, buscador global Ctrl+K, fecha, tema, notificaciones y perfil) y area de contenido. Cada modulo se
 * carga de su FXML una vez y se cachea; al entrar se muestra un skeleton de 250 ms y un fade de 200 ms.
 */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ShellController implements Navigator.Host {

    private static final double ANCHO_MENU = 248;
    private static final double ANCHO_CONTRAIDO = 72;
    private static final int MAX_HISTORIAL = 25;

    private final AppWindow appWindow;
    private final Navigator navigator;
    private final Overlays overlays;
    private final Toast toast;
    private final ConfirmDialog confirmDialog;
    private final ThemeManager themeManager;
    private final Session session;
    private final BusquedaService busquedaService;
    private final NotificacionService notificacionService;
    private final GarantiaService garantiaService;
    private final InventarioService inventarioService;

    @FXML private StackPane raiz;
    @FXML private VBox sidebar;
    @FXML private HBox marca;
    @FXML private VBox nav;
    @FXML private VBox pieMenu;
    @FXML private HBox buscadorHost;
    @FXML private Label lblFecha;
    @FXML private Button btnTema;
    @FXML private StackPane campanaHost;
    @FXML private HBox perfil;
    @FXML private Button btnRegresar;
    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;
    @FXML private StackPane vistaHost;
    @FXML private Pane capaPopups;
    @FXML private VBox zonaToasts;

    private final Map<Modulo, HBox> itemsMenu = new EnumMap<>(Modulo.class);
    private final Map<Modulo, Label> contadores = new EnumMap<>(Modulo.class);
    private final Map<Modulo, AppWindow.Cargado<Vista>> vistas = new EnumMap<>(Modulo.class);
    private final Deque<Modulo> historial = new ArrayDeque<>();
    private final List<Node> textosMenu = new ArrayList<>();

    private Modulo actual;
    private int tokenNavegacion;
    private boolean contraido;
    private SearchField buscador;
    private Label contadorCampana;
    private Region popupAbierto;
    private Node anclaPopup;
    private Button btnContraer;

    @FXML
    public void initialize() {
        toast.registrar(zonaToasts);
        overlays.registrar(raiz);
        navigator.registrar(this);

        construirMenu();
        construirBarraSuperior();
        btnRegresar.setGraphic(Ico.of("ARROW_BACK", 17));
        btnRegresar.setText("Regresar");
        btnRegresar.setMinWidth(Region.USE_PREF_SIZE);
        UI.tooltip(btnRegresar, "Volver a la pantalla anterior");

        raiz.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.K && e.isShortcutDown()) {
                buscador.campo().requestFocus();
                buscador.campo().selectAll();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE && popupAbierto != null) {
                cerrarPopup();
                e.consume();
            }
        });
        raiz.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (popupAbierto != null && !dentro(e.getTarget(), popupAbierto) && !dentro(e.getTarget(), anclaPopup)) cerrarPopup();
        });
        if (Prefs.bool(Prefs.MENU_CONTRAIDO, false)) contraer(true, false);
    }

    public void arrancar(String moduloInicio) {
        mostrar(Modulo.porClave(moduloInicio == null ? "dashboard" : moduloInicio), Intent.NONE, false);
    }

    // ================================================================ menu

    private void construirMenu() {
        LogoMark logo = new LogoMark(38, "ERP · SOFÁS");
        textosMenu.add(logo.getChildren().get(1));
        btnContraer = UI.iconButton("MENU_OPEN", "Contraer menú", "btn-ghost", "nav-toggle");
        marca.getChildren().addAll(logo, UI.spacer(), btnContraer);
        marca.setAlignment(Pos.CENTER_LEFT);
        btnContraer.setOnAction(e -> contraer(!contraido, true));

        String seccion = "";
        for (Modulo m : Modulo.values()) {
            if (!m.enMenu()) continue;
            if (m.seccion() != null && !m.seccion().equals(seccion)) {
                seccion = m.seccion();
                Label titulo = UI.label(seccion, "nav-section");
                nav.getChildren().add(titulo);
                textosMenu.add(titulo);
            }
            Label texto = UI.label(m.titulo(), "nav-text");
            Label contador = UI.label("", "nav-badge");
            contador.setVisible(false);
            contador.setManaged(false);
            HBox item = new HBox(12, Ico.of(m.icono(), 19), texto, UI.spacer(), contador);
            if (m == Modulo.CONFIGURADOR) {
                Label tres = UI.label("3D", "nav-badge", "nav-badge-3d");
                item.getChildren().add(tres);
                textosMenu.add(tres);
            }
            item.getStyleClass().add("nav-item");
            item.setAlignment(Pos.CENTER_LEFT);
            item.setFocusTraversable(true);
            item.setAccessibleText(m.titulo());
            Tooltip t = new Tooltip(m.titulo());
            t.setShowDelay(Duration.millis(250));
            Tooltip.install(item, t);
            item.setOnMouseClicked(e -> mostrar(m, Intent.NONE, true));
            item.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) mostrar(m, Intent.NONE, true);
            });
            textosMenu.add(texto);
            itemsMenu.put(m, item);
            contadores.put(m, contador);
            nav.getChildren().add(item);
        }

        // Pie: tarjeta del usuario y cerrar sesion
        AvatarInitials avatar = new AvatarInitials(session.nombre(), 36, "gradient");
        VBox textos = new VBox(1, UI.label(session.nombre(), "user-name"), UI.label(session.cargo(), "user-role"));
        HBox tarjeta = new HBox(10, avatar, textos);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.getStyleClass().add("user-card");
        textosMenu.add(textos);
        Label salirTxt = UI.label("Cerrar sesión", "nav-text");
        HBox salir = new HBox(12, Ico.of("EXIT_TO_APP", 19), salirTxt);
        salir.getStyleClass().addAll("nav-item", "nav-logout");
        salir.setAlignment(Pos.CENTER_LEFT);
        salir.setFocusTraversable(true);
        salir.setOnMouseClicked(e -> cerrarSesion());
        salir.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) cerrarSesion();
        });
        textosMenu.add(salirTxt);
        pieMenu.getChildren().addAll(tarjeta, salir);
    }

    private void contraer(boolean valor, boolean animar) {
        contraido = valor;
        Prefs.setBool(Prefs.MENU_CONTRAIDO, valor);
        double ancho = valor ? ANCHO_CONTRAIDO : ANCHO_MENU;
        sidebar.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("collapsed"), valor);
        UI.tooltip(btnContraer, valor ? "Expandir menú" : "Contraer menú");
        btnContraer.setGraphic(Ico.of(valor ? "MENU" : "MENU_OPEN", 18));
        if (valor) {
            textosMenu.forEach(n -> {
                n.setVisible(false);
                n.setManaged(false);
            });
            contadores.values().forEach(c -> {
                c.setVisible(false);
                c.setManaged(false);
            });
        }
        Runnable alFinal = () -> {
            if (!valor) {
                textosMenu.forEach(n -> {
                    n.setVisible(true);
                    n.setManaged(true);
                });
                if (contadorCampana != null) refrescarContadores();
            }
        };
        if (!animar) {
            sidebar.setPrefWidth(ancho);
            sidebar.setMinWidth(ancho);
            sidebar.setMaxWidth(ancho);
            alFinal.run();
            return;
        }
        Interpolator suave = Interpolator.SPLINE(0.4, 0, 0.2, 1);
        Timeline t = new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(sidebar.prefWidthProperty(), ancho, suave),
                new KeyValue(sidebar.minWidthProperty(), ancho, suave),
                new KeyValue(sidebar.maxWidthProperty(), ancho, suave)));
        t.setOnFinished(e -> alFinal.run());
        t.play();
    }

    // ======================================================== barra superior

    private void construirBarraSuperior() {
        buscador = new SearchField("Buscar sofás, telas, clientes, pedidos…");
        buscador.getStyleClass().add("global-search");
        buscador.getChildren().add(UI.label("Ctrl K", "kbd"));
        HBox.setHgrow(buscador, Priority.ALWAYS);
        buscadorHost.getChildren().add(buscador);
        buscadorHost.setAlignment(Pos.CENTER_LEFT);
        buscador.textProperty().addListener((o, a, n) -> actualizarBusqueda());
        buscador.campo().focusedProperty().addListener((o, a, f) -> {
            if (f && buscador.getText().length() >= 2) actualizarBusqueda();
        });
        buscador.campo().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                var res = busquedaService.buscar(buscador.getText());
                if (!res.isEmpty()) abrirResultado(res.get(0));
            }
        });

        lblFecha.setText(AppClock.fechaLarga());
        lblFecha.setGraphic(Ico.of("CALENDAR_TODAY", 16));

        actualizarIconoTema();
        themeManager.oscuroProperty().addListener((o, a, n) -> actualizarIconoTema());

        Button campana = UI.iconButton("NOTIFICATIONS", "Notificaciones", "btn-ghost", "topbar-btn");
        contadorCampana = UI.label("", "bell-count");
        StackPane.setAlignment(contadorCampana, Pos.TOP_RIGHT);
        contadorCampana.setMouseTransparent(true);
        campanaHost.getChildren().addAll(campana, contadorCampana);
        campana.setOnAction(e -> alternarPopup(campanaHost, this::popupNotificaciones));

        AvatarInitials avatar = new AvatarInitials(session.nombre(), 34, "gradient");
        perfil.getChildren().addAll(avatar, Ico.of("EXPAND_MORE", 18));
        perfil.setAlignment(Pos.CENTER_LEFT);
        perfil.setFocusTraversable(true);
        Tooltip.install(perfil, new Tooltip(session.nombre() + " · " + session.cargo()));
        perfil.setOnMouseClicked(e -> alternarPopup(perfil, this::popupPerfil));
        perfil.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) alternarPopup(perfil, this::popupPerfil);
        });
    }

    private void actualizarIconoTema() {
        boolean oscuro = themeManager.isOscuro();
        btnTema.setGraphic(Ico.of(oscuro ? "WB_SUNNY" : "NIGHTS_STAY", 19));
        UI.tooltip(btnTema, oscuro ? "Cambiar a modo claro" : "Cambiar a modo oscuro");
    }

    @FXML
    private void onTema() {
        themeManager.alternar();
    }

    // ---------------------------------------------------------- popups

    private void alternarPopup(Node ancla, java.util.function.Supplier<Region> fabrica) {
        if (popupAbierto != null && anclaPopup == ancla) {
            cerrarPopup();
            return;
        }
        abrirPopup(ancla, fabrica.get(), true);
    }

    private void abrirPopup(Node ancla, Region contenido, boolean derecha) {
        cerrarPopup();
        contenido.getStyleClass().add("popup");
        capaPopups.getChildren().add(contenido);
        contenido.applyCss();
        contenido.autosize();
        Bounds b = ancla.localToScene(ancla.getBoundsInLocal());
        Point2D p = capaPopups.sceneToLocal(derecha ? b.getMaxX() : b.getMinX(), b.getMaxY() + 8);
        double ancho = contenido.prefWidth(-1);
        double x = derecha ? p.getX() - ancho : Math.min(p.getX(), capaPopups.getWidth() - ancho - 16);
        contenido.relocate(x, p.getY());
        popupAbierto = contenido;
        anclaPopup = ancla;
        contenido.setOpacity(0);
        contenido.setTranslateY(-6);
        FadeTransition f = new FadeTransition(Duration.millis(150), contenido);
        f.setToValue(1);
        TranslateTransition t = new TranslateTransition(Duration.millis(150), contenido);
        t.setToY(0);
        new ParallelTransition(f, t).play();
    }

    private void cerrarPopup() {
        if (popupAbierto != null) {
            capaPopups.getChildren().remove(popupAbierto);
            popupAbierto = null;
            anclaPopup = null;
        }
    }

    private static boolean dentro(Object objetivo, Node contenedor) {
        if (contenedor == null || !(objetivo instanceof Node n)) return false;
        for (Node x = n; x != null; x = x.getParent()) if (x == contenedor) return true;
        return false;
    }

    private void actualizarBusqueda() {
        String q = buscador.getText();
        if (q.length() < 2) {
            if (anclaPopup == buscadorHost) cerrarPopup();
            return;
        }
        var resultados = busquedaService.buscar(q);
        VBox caja = new VBox(2);
        caja.setPrefWidth(Math.max(440, buscadorHost.getWidth()));
        caja.getStyleClass().add("search-popup");
        if (resultados.isEmpty()) {
            caja.getChildren().add(UI.empty("SEARCH", "Sin resultados para “" + q + "”.", "Prueba con un código como SF-001, TL-07, PE-0241 u OP-1021.", null));
        } else {
            String grupo = null;
            boolean primero = true;
            for (BusquedaService.Resultado r : resultados) {
                if (!r.grupo().equals(grupo)) {
                    grupo = r.grupo();
                    caja.getChildren().add(UI.label(grupo.toUpperCase(AppClock.PERU), "popup-caption"));
                }
                StackPane ic = new StackPane(Ico.of(r.icono(), 17));
                ic.getStyleClass().add("popup-item-icon");
                VBox textos = new VBox(1, UI.label(r.titulo(), "popup-item-title"), UI.label(r.subtitulo(), "popup-item-sub"));
                HBox fila = new HBox(12, ic, textos, UI.spacer());
                if (primero) {
                    fila.getChildren().add(UI.label("Enter ↵", "kbd"));
                    primero = false;
                }
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.getStyleClass().add("popup-item");
                UI.onClick(fila, e -> abrirResultado(r));
                caja.getChildren().add(fila);
            }
        }
        ScrollPane sp = new ScrollPane(caja);
        sp.setFitToWidth(true);
        sp.setMaxHeight(520);
        sp.setPrefViewportHeight(Math.min(500, caja.prefHeight(-1)));
        sp.getStyleClass().add("modal-scroll");
        VBox cont = new VBox(sp);
        cerrarPopup();
        abrirPopup(buscadorHost, cont, false);
    }

    private void abrirResultado(BusquedaService.Resultado r) {
        cerrarPopup();
        buscador.setText("");
        navigator.goTo(r.modulo(), Intent.abrir(r.dato()));
    }

    private Region popupNotificaciones() {
        VBox caja = new VBox(4);
        caja.setPrefWidth(400);
        Button marcar = UI.button("Marcar todas como leídas", null, "btn-link");
        marcar.setOnAction(e -> {
            notificacionService.marcarTodasLeidas();
            refrescarContadores();
            cerrarPopup();
            toast.exito("Notificaciones marcadas como leídas.");
        });
        HBox cab = UI.hbox(8, UI.label("Notificaciones", "popup-title"), UI.spacer(), marcar);
        cab.getStyleClass().add("popup-header");
        caja.getChildren().add(cab);
        var lista = notificacionService.todas();
        if (lista.isEmpty()) caja.getChildren().add(UI.empty("NOTIFICATIONS_NONE", "Sin notificaciones", "Todo está al día.", null));
        for (var n : lista) {
            StackPane ic = new StackPane(Ico.of(n.icono(), 17));
            ic.getStyleClass().addAll("notif-icon", "notif-" + n.tipo());
            VBox textos = new VBox(2, UI.wrap(n.titulo(), "popup-item-title"), UI.wrap(n.detalle(), "popup-item-sub"),
                    UI.label(n.hace(), "popup-item-time"));
            HBox.setHgrow(textos, Priority.ALWAYS);
            boolean leida = notificacionService.leida(n);
            Button leer = UI.iconButton("DONE", "Marcar como leída", "btn-ghost", "btn-xs");
            leer.setDisable(leida);
            leer.setOnAction(e -> {
                notificacionService.marcarLeida(n);
                refrescarContadores();
                cerrarPopup();
                abrirPopup(campanaHost, popupNotificaciones(), true);
            });
            Button descartar = UI.iconButton("CLOSE", "Descartar", "btn-ghost", "btn-xs");
            descartar.setOnAction(e -> {
                notificacionService.descartar(n);
                refrescarContadores();
                cerrarPopup();
                abrirPopup(campanaHost, popupNotificaciones(), true);
            });
            VBox acciones = new VBox(2, leer, descartar);
            HBox fila = new HBox(12, ic, textos);
            if (!leida) {
                Region punto = new Region();
                punto.getStyleClass().add("unread-dot");
                fila.getChildren().add(punto);
            }
            fila.getChildren().add(acciones);
            fila.getStyleClass().add("popup-item");
            if (leida) fila.getStyleClass().add("popup-item-read");
            textos.setOnMouseClicked(e -> {
                cerrarPopup();
                notificacionService.marcarLeida(n);
                navigator.goTo(n.modulo(), n.dato() == null ? Intent.NONE : Intent.abrir(n.dato()));
            });
            textos.getStyleClass().add("clickable");
            caja.getChildren().add(fila);
        }
        return caja;
    }

    private Region popupPerfil() {
        VBox caja = new VBox(2);
        caja.setPrefWidth(260);
        VBox cab = new VBox(2, UI.label(session.nombre(), "popup-item-title"),
                UI.label(session.cargo() + " · " + session.username(), "popup-item-sub"));
        cab.getStyleClass().add("popup-header");
        caja.getChildren().add(cab);
        caja.getChildren().add(itemMenu("ACCOUNT_CIRCLE", "Mi perfil", () -> navigator.goTo("configuracion", Intent.abrir("perfil"))));
        caja.getChildren().add(itemMenu("SETTINGS", "Configuración", () -> navigator.goTo("configuracion", Intent.NONE)));
        caja.getChildren().add(UI.separator());
        HBox salir = itemMenu("EXIT_TO_APP", "Cerrar sesión", this::cerrarSesion);
        salir.getStyleClass().add("popup-item-danger");
        caja.getChildren().add(salir);
        return caja;
    }

    private HBox itemMenu(String icono, String texto, Runnable accion) {
        HBox fila = new HBox(12, Ico.of(icono, 18), UI.label(texto, "popup-item-title"));
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().addAll("popup-item", "menu-item-row");
        UI.onClick(fila, e -> {
            cerrarPopup();
            accion.run();
        });
        return fila;
    }

    @Override
    public void cerrarSesion() {
        confirmDialog.mostrar(ConfirmDialog.Tipo.AVISO, "¿Cerrar sesión?",
                "Se cerrará tu sesión en SISTEMA ERP SOFAS. Los cambios ya están guardados en la base de datos.", "Cerrar sesión", () -> {
                    session.cerrar();
                    appWindow.mostrarLogin();
                });
    }

    // =========================================================== navegacion

    @Override
    public void mostrar(Modulo modulo, Intent intent, boolean registrarHistorial) {
        cerrarPopup();
        overlays.cerrarTodos();
        if (registrarHistorial && actual != null && actual != modulo) {
            historial.push(actual);
            while (historial.size() > MAX_HISTORIAL) historial.removeLast();
        }
        boolean mismoModulo = actual == modulo && vistas.containsKey(modulo);
        Modulo anterior = actual;
        actual = modulo;
        Modulo marcado = modulo == Modulo.SALA ? Modulo.CONFIGURADOR : modulo;
        itemsMenu.forEach((m, item) -> item.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), m == marcado));
        lblTitulo.setText(modulo.titulo());
        lblDescripcion.setText(modulo.descripcion());
        boolean hay = !historial.isEmpty();
        btnRegresar.setVisible(hay);
        btnRegresar.setManaged(hay);

        if (anterior != null && anterior != modulo && vistas.containsKey(anterior)
                && vistas.get(anterior).controlador() instanceof Vista.ConCiclo c) c.onHide();

        int token = ++tokenNavegacion;
        Intent in = intent == null ? Intent.NONE : intent;
        if (mismoModulo) {
            vistas.get(modulo).controlador().onShow(in);
            refrescarContadores();
            return;
        }
        Skeleton esqueleto = new Skeleton();
        vistaHost.getChildren().setAll(esqueleto);
        PauseTransition espera = new PauseTransition(Duration.millis(250));
        espera.setOnFinished(e -> {
            if (token != tokenNavegacion) return;
            AppWindow.Cargado<Vista> vista = vistas.computeIfAbsent(modulo, m -> appWindow.cargar(m.fxml()));
            vistaHost.getChildren().setAll(vista.nodo());
            vista.controlador().onShow(in);
            FadeTransition f = new FadeTransition(Duration.millis(200), vista.nodo());
            f.setFromValue(0);
            f.setToValue(1);
            f.play();
            refrescarContadores();
        });
        espera.play();
    }

    @Override
    public boolean regresar() {
        if (historial.isEmpty()) return false;
        mostrar(historial.pop(), Intent.NONE, false);
        return true;
    }

    @FXML
    private void onRegresar() {
        regresar();
    }

    @Override
    public void refrescarContadores() {
        fijarContador(Modulo.POSVENTA, garantiaService.abiertos());
        fijarContador(Modulo.INVENTARIO, inventarioService.criticos().stream().filter(m -> !m.getBajoPedido()).count());
        long noLeidas = notificacionService.noLeidas();
        contadorCampana.setText(String.valueOf(noLeidas));
        contadorCampana.setVisible(noLeidas > 0);
    }

    private void fijarContador(Modulo m, long valor) {
        Label l = contadores.get(m);
        if (l == null) return;
        l.setText(String.valueOf(valor));
        l.setVisible(valor > 0 && !contraido);
        l.setManaged(l.isVisible());
    }

    public Modulo actual() {
        return actual;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
