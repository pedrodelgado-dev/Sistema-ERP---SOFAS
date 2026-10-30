/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: MainApp
 * Descripción: Arranca Spring (SQLite + servicios) en init() y la interfaz JavaFX en start().
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
package pe.erp.sofas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pe.erp.sofas.ui.AppWindow;

/** Arranca Spring (SQLite + servicios) en init() y la interfaz JavaFX en start(). */
public class MainApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        // La base SQLite vive en ~/.sistemaerpsofas (ver app.db.dir); config.CarpetaBaseDatos crea la carpeta.
        this.context = new SpringApplicationBuilder(SofasApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run(getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage stage) {
        context.getBean(AppWindow.class).iniciar(stage, getHostServices());
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
