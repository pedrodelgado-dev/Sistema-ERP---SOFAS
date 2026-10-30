/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ImagenService
 * Descripción: Guarda las fotos en {@code ~/.innovasur/images/{id}.jpg}, recortadas en modo cover a la proporcion pedida.
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
package pe.erp.sofas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.erp.sofas.core.NegocioException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Guarda las fotos en {@code ~/.innovasur/images/{id}.jpg}, recortadas en modo cover a la proporcion pedida.
 */
@Service
public class ImagenService {

    private final File carpeta;

    public ImagenService(@Value("${app.db.dir:${user.home}/.innovasur}") String dir) {
        this.carpeta = new File(dir, "images");
        carpeta.mkdirs();
    }

    public File carpeta() {
        return carpeta;
    }

    public File archivo(String id) {
        return new File(carpeta, id + ".jpg");
    }

    /** Recorta (cover) a {@code ancho × alto} y guarda como JPG. Devuelve la ruta absoluta. */
    public String guardar(File origen, String id, int ancho, int alto) {
        String n = origen.getName().toLowerCase(Locale.ROOT);
        if (!(n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp"))) {
            throw new NegocioException("Formato no soportado: usa JPG, PNG o WebP.");
        }
        BufferedImage img;
        try {
            img = ImageIO.read(origen);
        } catch (IOException e) {
            img = null;
        }
        if (img == null) {
            throw new NegocioException(n.endsWith(".webp")
                    ? "Este equipo no puede leer WebP; convierte la foto a JPG o PNG."
                    : "No se pudo leer la imagen.");
        }
        BufferedImage recorte = cover(img, ancho, alto);
        File destino = archivo(id);
        try {
            ImageIO.write(recorte, "jpg", destino);
        } catch (IOException e) {
            throw new NegocioException("No se pudo guardar la imagen: " + e.getMessage());
        }
        return destino.getAbsolutePath();
    }

    public static BufferedImage cover(BufferedImage img, int ancho, int alto) {
        double ratio = ancho / (double) alto;
        int w = img.getWidth(), h = img.getHeight();
        int vw = (int) Math.min(w, Math.round(h * ratio));
        int vh = (int) Math.round(vw / ratio);
        int x = (w - vw) / 2, y = (h - vh) / 2;
        BufferedImage out = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ancho, alto);
        g.drawImage(img, 0, 0, ancho, alto, x, y, x + vw, y + vh, null);
        g.dispose();
        return out;
    }

    public void eliminar(String ruta) {
        if (ruta == null) return;
        File f = new File(ruta);
        if (f.getParentFile() != null && f.getParentFile().equals(carpeta)) f.delete();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
