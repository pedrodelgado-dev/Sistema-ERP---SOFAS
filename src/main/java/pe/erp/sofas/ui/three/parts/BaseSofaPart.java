/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: BaseSofaPart
 * Descripción: Clase base abstracta para partes de sofá modulares.
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
package pe.erp.sofas.ui.three.parts;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Translate;
import pe.erp.sofas.ui.three.Mallas;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase base abstracta para partes de sofá modulares.
 * Proporciona utilidades comunes: crear cajas redondeadas, posicionar nodos, manejar materiales.
 */
public abstract class BaseSofaPart implements SofaPart {

    protected final String nombre;
    protected final double ancho;
    protected final double fondo;
    protected final double alto;

    public BaseSofaPart(String nombre, double ancho, double fondo, double alto) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.fondo = fondo;
        this.alto = alto;
    }

    @Override
    public PartDimensions getDimensions() {
        return new PartDimensions(ancho, fondo, alto);
    }

    @Override
    public Map<String, Punto3D> getAnchors() {
        // Anclajes por defecto: esquinas del rectángulo de planta
        Map<String, Punto3D> anchors = new HashMap<>();
        anchors.put("left", new Punto3D(-ancho / 2, 0, 0));
        anchors.put("right", new Punto3D(ancho / 2, 0, 0));
        anchors.put("front", new Punto3D(0, 0, -fondo / 2));
        anchors.put("back", new Punto3D(0, 0, fondo / 2));
        return anchors;
    }

    @Override
    public String getName() {
        return nombre;
    }

    /**
     * Utilidad: crea una caja redondeada usando Mallas.
     */
    protected MeshView cajaRedondeada(double w, double h, double d, double radio, PhongMaterial material) {
        MeshView mv = new MeshView(Mallas.cajaRedondeada(w, h, d, radio, 4));
        mv.setMaterial(material);
        mv.setCullFace(javafx.scene.shape.CullFace.NONE);
        return mv;
    }

    /**
     * Utilidad: suma un nodo a un Group en una posición específica.
     */
    protected Node agregar(Group grupo, Node nodo, double x, double yCentro, double z) {
        nodo.setTranslateX(x);
        nodo.setTranslateY(-yCentro);
        nodo.setTranslateZ(z);
        grupo.getChildren().add(nodo);
        return nodo;
    }

    /**
     * Material gris claro por defecto.
     */
    protected PhongMaterial materialPorDefecto() {
        return new PhongMaterial(javafx.scene.paint.Color.web("#B9B7B1"));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
