/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: AppWindow
 * Descripción: Ventana principal: carga fuentes y tema, muestra el login y luego el shell.
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

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.ThemeManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/** Ventana principal: carga fuentes y tema, muestra el login y luego el shell. */
@Component
@Lazy
@RequiredArgsConstructor
@Slf4j
public class AppWindow {

    private static final String[] FUENTES = {
            "Inter-Regular", "Inter-Medium", "Inter-SemiBold", "Inter-Bold", "Inter-ExtraBold",
            "JetBrainsMono-Regular", "JetBrainsMono-Medium", "JetBrainsMono-Bold"};

    public record Cargado<T>(Parent nodo, T controlador) {
    }

    private final ApplicationContext context;
    private final ThemeManager themeManager;

    private Stage stage;
    private Scene scene;
    private HostServices hostServices;

    public void iniciar(Stage stage, HostServices hostServices) {
        this.stage = stage;
        this.hostServices = hostServices;
        cargarFuentes();

        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double ancho = Math.min(1440, pantalla.getWidth() - 40);
        double alto = Math.min(900, pantalla.getHeight() - 40);
        scene = new Scene(cargar("/fxml/login.fxml").nodo(), ancho, alto);
        themeManager.aplicar(scene);

        stage.setTitle("SISTEMA ERP SOFAS · ERP de sofás · Juliaca");
        stage.setScene(scene);
        stage.setMinWidth(1180);
        stage.setMinHeight(720);
        try (InputStream icono = getClass().getResourceAsStream("/images/icono.png")) {
            if (icono != null) stage.getIcons().add(new Image(icono));
        } catch (IOException ignored) {
            // sin icono de ventana
        }
        stage.show();

        String tour = System.getProperty("innova.tour");
        if (tour != null) {
            context.getBean(DemoTour.class).ejecutar(tour);
        }
    }

    public void mostrarLogin() {
        scene.setRoot(cargar("/fxml/login.fxml").nodo());
        themeManager.reaplicar();
    }

    public void mostrarShell(String moduloInicio) {
        Cargado<ShellController> shell = cargar("/fxml/shell.fxml");
        scene.setRoot(shell.nodo());
        themeManager.reaplicar();
        shell.controlador().arrancar(moduloInicio);
    }

    public <T> Cargado<T> cargar(String ruta) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
        loader.setControllerFactory(context::getBean);
        // Explicito: en macOS el hilo de eventos puede no tener class loader de contexto.
        loader.setClassLoader(getClass().getClassLoader());
        try {
            Parent nodo = loader.load();
            return new Cargado<>(nodo, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar " + ruta, e);
        }
    }

    public Stage stage() {
        return stage;
    }

    public Scene scene() {
        return scene;
    }

    public HostServices hostServices() {
        return hostServices;
    }

    private void cargarFuentes() {
        for (String f : FUENTES) {
            try (InputStream in = getClass().getResourceAsStream("/fonts/" + f + ".ttf")) {
                Font font = in == null ? null : Font.loadFont(in, 13);
                if (font != null) log.debug("Fuente {} → familia '{}'", f, font.getFamily());
            } catch (IOException e) {
                log.warn("No se pudo cargar la fuente {}", f);
            }
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
