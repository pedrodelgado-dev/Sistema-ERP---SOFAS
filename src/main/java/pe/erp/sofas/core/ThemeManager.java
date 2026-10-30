/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ThemeManager
 * Descripción: Tema claro/oscuro en caliente: cambia la hoja de tokens de la escena (theme-light.css ↔ theme-dark.css)
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
package pe.erp.sofas.core;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Tema claro/oscuro en caliente: cambia la hoja de tokens de la escena (theme-light.css ↔ theme-dark.css)
 * y la hoja base de AtlantaFX. Tambien aplica la densidad de las tablas (Compacta/Normal/Amplia).
 */
@Component
@Lazy
public class ThemeManager {

    public static final List<String> DENSIDADES = List.of("Compacta", "Normal", "Amplia");

    private final BooleanProperty oscuro = new SimpleBooleanProperty(Prefs.bool(Prefs.TEMA_OSCURO, false));
    private String densidad = Prefs.str(Prefs.DENSIDAD, "Normal");
    private Scene scene;

    public void aplicar(Scene scene) {
        this.scene = scene;
        refrescar();
    }

    public BooleanProperty oscuroProperty() {
        return oscuro;
    }

    public boolean isOscuro() {
        return oscuro.get();
    }

    public void alternar() {
        setOscuro(!isOscuro());
    }

    public void setOscuro(boolean valor) {
        oscuro.set(valor);
        Prefs.setBool(Prefs.TEMA_OSCURO, valor);
        refrescar();
    }

    public String getDensidad() {
        return densidad;
    }

    public void setDensidad(String densidad) {
        this.densidad = densidad;
        Prefs.setStr(Prefs.DENSIDAD, densidad);
        refrescar();
    }

    private void refrescar() {
        Application.setUserAgentStylesheet(isOscuro()
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
        if (scene == null) return;
        scene.getStylesheets().setAll(
                css("/css/base.css"),
                css(isOscuro() ? "/css/theme-dark.css" : "/css/theme-light.css"));
        if (scene.getRoot() != null) {
            var clases = scene.getRoot().getStyleClass();
            clases.removeAll("density-compacta", "density-normal", "density-amplia", "dark", "light");
            clases.add("density-" + densidad.toLowerCase());
            clases.add(isOscuro() ? "dark" : "light");
        }
    }

    /** Vuelve a aplicar clases a un nuevo nodo raiz (p. ej. al pasar del login al shell). */
    public void reaplicar() {
        refrescar();
    }

    private String css(String ruta) {
        return Objects.requireNonNull(getClass().getResource(ruta), ruta).toExternalForm();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
