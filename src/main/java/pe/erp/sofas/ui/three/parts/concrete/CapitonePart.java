/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CapitonePart
 * Descripción: Capitoné: panel tapizado con hundimientos en patrón rombo + botones forrados.
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
package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import pe.erp.sofas.ui.three.geom.TuftingMesh;
import pe.erp.sofas.ui.three.parts.BaseSofaPart;

/**
 * Capitoné: panel tapizado con hundimientos en patrón rombo + botones forrados.
 * Típicamente usado en Chesterfield y Tuxedo.
 *
 * Componentes:
 * - Malla base con hundimientos cóncavos (patrón rombo)
 * - Botones forrados en cada vértice del patrón (esferas pequeñas)
 *
 * El efecto visual es de un sofá de lujo tapizado con precisión.
 */
public class CapitonePart extends BaseSofaPart {

    private final PhongMaterial materialTela;
    private final PhongMaterial materialBoton;

    public CapitonePart(double ancho, double alto, double profundidad, PhongMaterial materialTela) {
        super("Capitoné", ancho, profundidad, alto);
        this.materialTela = materialTela == null ? materialPorDefecto() : materialTela;

        // Material del botón: 20% más oscuro que la tela
        Color colBase = (Color) this.materialTela.getDiffuseColor();
        Color colBoton = colBase.deriveColor(0, 1, 0.8, 1);
        this.materialBoton = new PhongMaterial(colBoton);
        this.materialBoton.setSpecularPower(this.materialTela.getSpecularPower());
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Malla base capitoné (hundimientos)
        MeshView capitone = new MeshView(TuftingMesh.crearDefecto(ancho, alto, fondo));
        capitone.setMaterial(materialTela);
        capitone.setCullFace(javafx.scene.shape.CullFace.NONE);

        agregar(g, capitone, 0, alto / 2, 0);

        // Agregar botones forrados en los vértices del patrón
        agregarBotones(g);

        return g;
    }

    /**
     * Agrega botones pequeños forrados en los vértices del patrón rombo.
     */
    private void agregarBotones(Group g) {
        double tamRombo = 13;  // Coincide con TuftingMesh.crearDefecto()
        double radioBoto = 1.4;  // Radio del botón (cm)

        // Grid de botones siguiendo patrón rombo
        int filas = (int) Math.ceil(alto / tamRombo) + 1;
        int cols = (int) Math.ceil(ancho / tamRombo) + 1;

        double hx = ancho / 2, hy = alto / 2;

        for (int row = 0; row < filas; row++) {
            for (int col = 0; col < cols; col++) {
                double y = -hy + (row / (double) (filas - 1)) * alto;
                double x = -hx + (col / (double) (cols - 1)) * ancho;

                // Botón: esfera pequeña en el vértice del rombo
                Sphere boton = new Sphere(radioBoto, 8);
                boton.setMaterial(materialBoton);

                boton.setTranslateX(x);
                boton.setTranslateY(-y);  // Invertir Y (JavaFX convention)
                boton.setTranslateZ(fondo / 2 - 0.2);  // Un poco salido hacia frente

                g.getChildren().add(boton);
            }
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
