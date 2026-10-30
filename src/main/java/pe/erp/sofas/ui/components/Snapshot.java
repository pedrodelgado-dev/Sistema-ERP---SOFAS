/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Snapshot
 * Descripción: Captura un nodo como PNG (para incrustar graficos en los PDF) sin depender de javafx-swing.
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
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Captura un nodo como PNG (para incrustar graficos en los PDF) sin depender de javafx-swing. */
public final class Snapshot {

    private Snapshot() {
    }

    public static BufferedImage imagen(WritableImage img) {
        int w = (int) img.getWidth(), h = (int) img.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader pr = img.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) bi.setRGB(x, y, pr.getArgb(x, y));
        }
        return bi;
    }

    /** PNG a doble resolucion con fondo blanco; null si el nodo no esta disponible. */
    public static byte[] png(Node nodo) {
        if (nodo == null || nodo.getScene() == null) return null;
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.WHITE);
        sp.setTransform(Transform.scale(2, 2));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(imagen(nodo.snapshot(sp, null)), "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
