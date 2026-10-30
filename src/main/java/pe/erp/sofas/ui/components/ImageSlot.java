/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ImageSlot
 * Descripción: Espacio para una foto: borde punteado con icono y texto ("Foto: Sofá Moderno 3 cuerpos"). Acepta arrastrar y
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

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Espacio para una foto: borde punteado con icono y texto ("Foto: Sofá Moderno 3 cuerpos"). Acepta arrastrar y
 * soltar o un clic para elegir el archivo (JPG / PNG / WebP); muestra la imagen recortada en modo cover y tiene
 * menu contextual Reemplazar / Quitar. Sin foto puede mostrar un respaldo (el render 3D del modelo).
 */
public class ImageSlot extends StackPane {

    private final double radio;
    private final String placeholder;
    private Consumer<File> alElegir;
    private Runnable alQuitar;
    private Supplier<Image> respaldo;
    private boolean editable = true;

    public ImageSlot(String placeholder, double ancho, double alto, double radio) {
        this.radio = radio;
        this.placeholder = placeholder;
        getStyleClass().add("image-slot");
        if (ancho > 0) {
            setMinWidth(ancho);
            setPrefWidth(ancho);
            setMaxWidth(ancho);
        } else {
            setMinWidth(0);
            setMaxWidth(Double.MAX_VALUE);
        }
        setMinHeight(alto);
        setPrefHeight(alto);
        setMaxHeight(alto);
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radio * 2);
        clip.setArcHeight(radio * 2);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        setOnDragOver(e -> {
            if (editable && e.getDragboard().hasFiles() && esImagen(e.getDragboard().getFiles().get(0))) {
                e.acceptTransferModes(TransferMode.COPY);
                getStyleClass().add("image-slot-drop");
            }
            e.consume();
        });
        setOnDragExited(e -> getStyleClass().remove("image-slot-drop"));
        setOnDragDropped(e -> {
            boolean ok = false;
            if (e.getDragboard().hasFiles() && alElegir != null) {
                File f = e.getDragboard().getFiles().get(0);
                if (esImagen(f)) {
                    alElegir.accept(f);
                    ok = true;
                }
            }
            e.setDropCompleted(ok);
            e.consume();
        });
        setOnMouseClicked(e -> {
            if (!editable || e.getButton() != MouseButton.PRIMARY || alElegir == null) return;
            if (e.getClickCount() == 1 && getProperties().get("conImagen") == null) elegir();
        });
        mostrar((Image) null);
    }

    public ImageSlot alElegir(Consumer<File> c) {
        this.alElegir = c;
        configurarMenu();
        return this;
    }

    public ImageSlot alQuitar(Runnable r) {
        this.alQuitar = r;
        configurarMenu();
        return this;
    }

    /** Imagen alternativa cuando no hay foto (p. ej. snapshot 3D del modelo). */
    public ImageSlot respaldo(Supplier<Image> s) {
        this.respaldo = s;
        return this;
    }

    public ImageSlot soloLectura() {
        this.editable = false;
        getStyleClass().add("image-slot-readonly");
        return this;
    }

    public void elegir() {
        FileChooser fc = new FileChooser();
        fc.setTitle(placeholder);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes (JPG, PNG, WebP)", "*.jpg", "*.jpeg", "*.png", "*.webp"));
        File f = fc.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (f != null && alElegir != null) alElegir.accept(f);
    }

    private void configurarMenu() {
        if (!editable) return;
        MenuItem reemplazar = new MenuItem("Reemplazar…", Ico.of("SWAP_HORIZ", 16));
        reemplazar.setOnAction(e -> elegir());
        MenuItem quitar = new MenuItem("Quitar", Ico.of("DELETE_OUTLINE", 16));
        quitar.setOnAction(e -> {
            if (alQuitar != null) alQuitar.run();
        });
        ContextMenu m = new ContextMenu(reemplazar, quitar);
        setOnContextMenuRequested(e -> {
            quitar.setDisable(getProperties().get("conImagen") == null || alQuitar == null);
            m.show(this, e.getScreenX(), e.getScreenY());
        });
    }

    public void mostrar(String ruta) {
        mostrar(Imagenes.cargar(ruta));
    }

    public void mostrar(Image img) {
        getChildren().clear();
        getStyleClass().removeAll("image-slot-filled");
        getProperties().remove("conImagen");
        Image mostrar = img;
        boolean deRespaldo = false;
        if ((mostrar == null || mostrar.isError()) && respaldo != null) {
            mostrar = respaldo.get();
            deRespaldo = true;
        }
        if (mostrar != null && !mostrar.isError()) {
            ImageView iv = new ImageView(mostrar);
            iv.setSmooth(true);
            iv.setPreserveRatio(false);
            Image fin = mostrar;
            Runnable ajustar = () -> {
                double w = getWidth() > 0 ? getWidth() : getPrefWidth();
                double h = getHeight() > 0 ? getHeight() : getPrefHeight();
                if (w <= 0 || h <= 0) return;
                double ratio = w / h;
                double iw = fin.getWidth(), ih = fin.getHeight();
                double vw = Math.min(iw, ih * ratio), vh = vw / ratio;
                iv.setViewport(new Rectangle2D((iw - vw) / 2, (ih - vh) / 2, vw, vh));
                iv.setFitWidth(w);
                iv.setFitHeight(h);
            };
            widthProperty().addListener((o, a, b) -> ajustar.run());
            heightProperty().addListener((o, a, b) -> ajustar.run());
            ajustar.run();
            getChildren().add(iv);
            if (!deRespaldo) {
                getStyleClass().add("image-slot-filled");
                getProperties().put("conImagen", true);
            } else {
                var etiqueta = UI.label("Render 3D", "image-slot-tag");
                StackPane.setAlignment(etiqueta, Pos.TOP_LEFT);
                getChildren().add(etiqueta);
            }
        } else {
            VBox v = new VBox(6, Ico.of("ADD_PHOTO_ALTERNATE", getPrefHeight() > 120 ? 30 : 20),
                    UI.wrap(placeholder, "image-slot-text"));
            v.setAlignment(Pos.CENTER);
            v.setMaxWidth(Math.max(80, getPrefWidth() - 24));
            if (getPrefHeight() < 70) v.getChildren().remove(1);
            getChildren().add(v);
        }
    }

    public Node contenido() {
        return getChildren().isEmpty() ? null : getChildren().get(0);
    }

    private static boolean esImagen(File f) {
        String n = f.getName().toLowerCase(Locale.ROOT);
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp");
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
