/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Archivos
 * Descripción: Dialogo "Guardar como" con la carpeta de Documentos y el nombre sugerido.
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

import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.io.File;

/** Dialogo "Guardar como" con la carpeta de Documentos y el nombre sugerido. */
public final class Archivos {

    private static File ultimaCarpeta;

    private Archivos() {
    }

    public static File guardar(Node ancla, String nombre, String descripcion, String extension) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar " + descripcion);
        fc.setInitialFileName(nombre + "." + extension);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(descripcion, "*." + extension));
        File dir = ultimaCarpeta != null ? ultimaCarpeta : new File(System.getProperty("user.home"), "Documents");
        if (dir.isDirectory()) fc.setInitialDirectory(dir);
        File f = fc.showSaveDialog(ancla == null || ancla.getScene() == null ? null : ancla.getScene().getWindow());
        if (f != null) ultimaCarpeta = f.getParentFile();
        return f;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
