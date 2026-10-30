/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PBRMaterial
 * Descripción: Material PBR (Physically Based Rendering) para JavaFX 3D.
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
package pe.erp.sofas.ui.three.materials;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 * Material PBR (Physically Based Rendering) para JavaFX 3D.
 * Extiende PhongMaterial con especular maps y normal maps para realismo.
 *
 * En JavaFX, esto se implementa como:
 * - DiffuseMap: color base (albedo)
 * - SpecularMap: mapa de especularidad (brillo por píxel)
 * - BumpMap (usado como normal map aproximado): detalle de superficie
 *
 * Este es un wrapper que facilita la creación de materiales con múltiples mapas.
 */
public class PBRMaterial extends PhongMaterial {

    private Image diffuseMap;
    private Image specularMap;
    private Image normalMap;

    public PBRMaterial(Color diffuseColor) {
        super(diffuseColor);
    }

    /**
     * Establece el mapa de difusión (color base / albedo).
     * Típicamente una textura con los colores de la tela.
     */
    public PBRMaterial conDiffuseMap(Image mapa) {
        this.diffuseMap = mapa;
        this.setDiffuseMap(mapa);
        return this;
    }

    /**
     * Establece el mapa de especularidad (brillo).
     * Imagen en escala de grises donde blanco = muy brillante, negro = mate.
     *
     * Permite que diferentes partes de la tela tengan brillo variable:
     * - Costuras: oscuras (mate)
     * - Tela lisa: clara (brillante)
     * - Botones: muy claros (muy brillante)
     */
    public PBRMaterial conSpecularMap(Image mapa) {
        this.specularMap = mapa;
        this.setSpecularMap(mapa);
        return this;
    }

    /**
     * Establece el mapa de normales (detalle de superficie).
     * En JavaFX, se usa como bump map para simular relieve.
     *
     * Imagen RGB donde:
     * - R y G: dirección XY de la normal
     * - B: altura / profundidad del relieve
     *
     * Crea la ilusión de detalle sin geometría adicional.
     */
    public PBRMaterial conNormalMap(Image mapa) {
        this.normalMap = mapa;
        this.setBumpMap(mapa);  // JavaFX usa bump map como aproximación
        return this;
    }

    /**
     * Establece power especular (brillo general).
     */
    public PBRMaterial conSpecularPower(double power) {
        this.setSpecularPower(power);
        return this;
    }

    /**
     * Establece color especular (tono del brillo).
     * Típicamente:
     * - Cuero: gris (0.5)
     * - Terciopelo: blanco con tono (0.8 con ligero color)
     * - Botones metálicos: amarillo/dorado
     */
    public PBRMaterial conSpecularColor(Color color) {
        this.setSpecularColor(color);
        return this;
    }

    // Getters (métodos de PhongMaterial son final, así que usamos nombres diferentes)
    public Image getDiffuseMapLocal() {
        return diffuseMap;
    }

    public Image getSpecularMapLocal() {
        return specularMap;
    }

    public Image getNormalMapLocal() {
        return normalMap;
    }

    /**
     * Crea un material PBR simplificado para cuero.
     * Especularidad media, brillo cálido.
     */
    public static PBRMaterial crearCuero(Color color) {
        PBRMaterial m = new PBRMaterial(color);
        m.setSpecularPower(40);
        m.setSpecularColor(Color.color(0.6, 0.55, 0.5));  // Tono cálido
        return m;
    }

    /**
     * Crea un material PBR simplificado para terciopelo.
     * Especularidad baja, brillo direccional.
     */
    public static PBRMaterial crearTerciopelo(Color color) {
        PBRMaterial m = new PBRMaterial(color);
        m.setSpecularPower(25);
        m.setSpecularColor(Color.color(0.7, 0.7, 0.8));  // Blanco azulado
        return m;
    }

    /**
     * Crea un material PBR simplificado para tela.
     * Especularidad muy baja, aspecto mate.
     */
    public static PBRMaterial crearTela(Color color) {
        PBRMaterial m = new PBRMaterial(color);
        m.setSpecularPower(8);
        m.setSpecularColor(Color.color(0.3, 0.3, 0.3));  // Gris oscuro
        return m;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
