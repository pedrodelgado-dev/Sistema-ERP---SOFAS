/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: LoginController
 * Descripción: Recuperacion simulada: pide el usuario o correo y confirma el envio.
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
package pe.erp.sofas.ui.views.login;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.Usuario;
import pe.erp.sofas.service.AuthService;
import pe.erp.sofas.service.ImagenService;
import pe.erp.sofas.ui.AppWindow;
import pe.erp.sofas.ui.components.*;

import java.io.File;

@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    private final Session session;
    private final AppWindow appWindow;
    private final Toast toast;
    private final Overlays overlays;
    private final ImagenService imagenService;

    @FXML private StackPane raiz;
    @FXML private VBox formulario;
    @FXML private StackPane logoHost;
    @FXML private HBox cajaUsuario;
    @FXML private HBox cajaPassword;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnOjo;
    @FXML private CheckBox chkRecordar;
    @FXML private Button btnIngresar;
    @FXML private HBox notaDemo;
    @FXML private StackPane hero;
    @FXML private Label errUsuario;
    @FXML private Label errPassword;
    @FXML private HBox errorGeneral;
    @FXML private Label lblErrorGeneral;

    @FXML
    public void initialize() {
        overlays.registrar(raiz);
        logoHost.getChildren().add(new LogoMark(42, "SOFÁS · JULIACA"));
        cajaUsuario.getChildren().add(0, Ico.of("PERSON_OUTLINE", 19));
        cajaPassword.getChildren().add(0, Ico.of("LOCK", 19));
        btnOjo.setGraphic(Ico.of("VISIBILITY", 18));
        UI.tooltip(btnOjo, "Mostrar contraseña");
        notaDemo.getChildren().add(0, Ico.of("INFO", 18));
        errorGeneral.getChildren().add(0, Ico.of("ERROR", 16));
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        boolean recordar = Prefs.bool(Prefs.RECORDAR, true);
        chkRecordar.setSelected(recordar);
        if (recordar) txtUsuario.setText(Prefs.str(Prefs.ULTIMO_USUARIO, "cmendoza"));
        txtUsuario.textProperty().addListener((o, a, b) -> limpiar(errUsuario, cajaUsuario));
        txtPassword.textProperty().addListener((o, a, b) -> limpiar(errPassword, cajaPassword));

        hero.getChildren().add(construirHero());
        UI.entrar(formulario);
        Platform.runLater(() -> (txtUsuario.getText().isEmpty() ? txtUsuario : txtPassword).requestFocus());
    }

    private void limpiar(Label err, HBox caja) {
        err.setVisible(false);
        err.setManaged(false);
        caja.getStyleClass().remove("field-invalid");
        errorGeneral.setVisible(false);
        errorGeneral.setManaged(false);
    }

    private void error(Label err, HBox caja, String msg) {
        err.setText(msg);
        err.setVisible(true);
        err.setManaged(true);
        if (!caja.getStyleClass().contains("field-invalid")) caja.getStyleClass().add("field-invalid");
    }

    @FXML
    private void onMostrarPassword() {
        boolean mostrar = !txtPasswordVisible.isVisible();
        txtPasswordVisible.setVisible(mostrar);
        txtPassword.setVisible(!mostrar);
        ((FontIcon) btnOjo.getGraphic()).setIconCode(Ico.ikon(mostrar ? "VISIBILITY_OFF" : "VISIBILITY"));
        btnOjo.getTooltip().setText(mostrar ? "Ocultar contraseña" : "Mostrar contraseña");
    }

    /** Recuperacion simulada: pide el usuario o correo y confirma el envio. */
    @FXML
    private void onOlvido() {
        Validacion v = new Validacion();
        TextField correo = UI.text(txtUsuario.getText(), "Usuario o correo @innovasur.pe");
        VBox cuerpo = new VBox(14, UI.wrap("Te enviaremos un enlace para restablecer tu contraseña. Si no lo recibes, "
                        + "pide ayuda a la administradora (Milagros Cahuana).", "muted"),
                v.campo("Usuario o correo", correo, () -> Validacion.requerido(correo, "tu usuario o correo")));
        Button cancelar = UI.secondary("Cancelar", null);
        Button enviar = UI.primary("Enviar enlace", "SEND");
        v.enlazar(enviar);
        var h = overlays.modal("Recuperar contraseña", "Simulado: no se envía ningún correo real.", cuerpo, 440, cancelar, enviar);
        cancelar.setOnAction(e -> h.cerrar());
        enviar.setOnAction(e -> {
            h.cerrar();
            toast.exito("Enlace de recuperación enviado a " + (correo.getText().contains("@") ? correo.getText() : correo.getText().trim() + "@innovasur.pe") + ".");
        });
        Platform.runLater(correo::requestFocus);
    }

    @FXML
    private void onIngresar() {
        if (btnIngresar.isDisabled()) return;
        boolean ok = true;
        if (txtUsuario.getText() == null || txtUsuario.getText().isBlank()) {
            error(errUsuario, cajaUsuario, "Ingresa tu usuario.");
            ok = false;
        }
        if (txtPassword.getText() == null || txtPassword.getText().isEmpty()) {
            error(errPassword, cajaPassword, "Ingresa tu contraseña.");
            ok = false;
        }
        if (!ok) return;
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.getStyleClass().add("btn-spinner");
        spinner.setPrefSize(16, 16);
        btnIngresar.setGraphic(spinner);
        btnIngresar.setText("Verificando…");
        btnIngresar.setDisable(true);
        PauseTransition espera = new PauseTransition(Duration.seconds(1));
        espera.setOnFinished(e -> {
            try {
                Usuario u = authService.login(txtUsuario.getText(), txtPassword.getText());
                session.iniciar(u);
                Prefs.setBool(Prefs.RECORDAR, chkRecordar.isSelected());
                if (chkRecordar.isSelected()) Prefs.setStr(Prefs.ULTIMO_USUARIO, u.getUsername());
                appWindow.mostrarShell("dashboard");
                toast.exito("Sesión iniciada. ¡Buen día, " + u.getPrimerNombre() + "!");
            } catch (NegocioException ex) {
                btnIngresar.setGraphic(null);
                btnIngresar.setText("Iniciar sesión");
                btnIngresar.setDisable(false);
                lblErrorGeneral.setText(ex.getMessage());
                errorGeneral.setVisible(true);
                errorGeneral.setManaged(true);
                cajaPassword.getStyleClass().add("field-invalid");
                txtPassword.selectAll();
                txtPassword.requestFocus();
            }
        });
        espera.play();
    }

    /** Columna derecha: foto grande de una sala con sofa (ImageSlot) con el titular en blanco. */
    private StackPane construirHero() {
        StackPane tarjeta = new StackPane();
        tarjeta.getStyleClass().add("login-hero");
        ImageSlot foto = new ImageSlot("Foto: sala con sofá (arrastra una imagen aquí)", -1, 10, 18);
        foto.setMinHeight(0);
        foto.setPrefHeight(Region.USE_COMPUTED_SIZE);
        foto.setMaxHeight(Double.MAX_VALUE);
        File local = imagenService.archivo("login");
        foto.mostrar(local.exists() ? local.getAbsolutePath() : "classpath:/images/sofas/login.jpg");
        foto.alElegir(f -> toast.intentar(() -> {
            String ruta = imagenService.guardar(f, "login", 1600, 1200);
            Imagenes.olvidar(ruta);
            foto.mostrar(ruta);
        }));
        foto.alQuitar(() -> {
            imagenService.eliminar(local.getAbsolutePath());
            Imagenes.olvidar(local.getAbsolutePath());
            foto.mostrar("classpath:/images/sofas/login.jpg");
        });
        tarjeta.getChildren().add(foto);
        Region degradado = new Region();
        degradado.getStyleClass().add("login-hero-gradient");
        degradado.setMouseTransparent(true);
        tarjeta.getChildren().add(degradado);

        Label titular = UI.wrap("Sofás hechos a tu medida en Juliaca desde 2012.", "hero-title");
        Label sub = UI.wrap("Telas, producción del taller, pedidos a medida y ventas de SISTEMA ERP SOFAS conectados en una sola plataforma.", "hero-subtitle");
        VBox textos = new VBox(12, titular, sub);
        textos.setMouseTransparent(true);
        textos.setMaxWidth(560);
        textos.setPadding(new Insets(0, 44, 44, 44));
        textos.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(textos, Pos.BOTTOM_LEFT);
        textos.setMaxHeight(Region.USE_PREF_SIZE);
        tarjeta.getChildren().add(textos);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(36);
        clip.setArcHeight(36);
        clip.widthProperty().bind(tarjeta.widthProperty());
        clip.heightProperty().bind(tarjeta.heightProperty());
        tarjeta.setClip(clip);
        return tarjeta;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
