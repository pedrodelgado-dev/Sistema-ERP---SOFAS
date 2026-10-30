/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Imagenes
 * Descripción: Carga imagenes por ruta ("classpath:/images/..." o archivo local) con cache.
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
package pe.erp.sofas.ui.components;

import javafx.scene.image.Image;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Carga imagenes por ruta ("classpath:/images/..." o archivo local) con cache. */
public final class Imagenes {

    private static final Map<String, Image> CACHE = new HashMap<>();

    private Imagenes() {
    }

    public static Image cargar(String ruta) {
        if (ruta == null || ruta.isBlank()) return null;
        return CACHE.computeIfAbsent(ruta + "@" + new File(ruta).lastModified(), k -> {
            if (ruta.startsWith("classpath:")) {
                URL url = Imagenes.class.getResource(ruta.substring("classpath:".length()));
                return url == null ? null : new Image(url.toExternalForm(), 1400, 0, true, true, false);
            }
            File f = new File(ruta);
            return f.exists() ? new Image(f.toURI().toString(), 1400, 0, true, true, false) : null;
        });
    }

    public static void olvidar(String ruta) {
        CACHE.keySet().removeIf(k -> k.startsWith(ruta + "@"));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
