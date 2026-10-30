/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: BackrestPart
 * Descripción: Respaldo: panel "inflado" que forma la espalda del sofá.
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
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import pe.erp.sofas.ui.three.geom.PillowMesh;
import pe.erp.sofas.ui.three.parts.BaseSofaPart;

/**
 * Respaldo: panel "inflado" que forma la espalda del sofá.
 * Altura típica: variable (bajo 78, estándar 85, alto 95 cm).
 * Se inclina ligeramente hacia atrás (8° de rotación en X).
 *
 * Usa PillowMesh para un aspecto más mullido y realista.
 */
public class BackrestPart extends BaseSofaPart {

    private final PhongMaterial material;
    private final double altoRespaldo;

    public BackrestPart(double ancho, double altoRespaldo, PhongMaterial material) {
        super("Respaldo", ancho, 20, altoRespaldo);  // 20 cm de profundidad estándar
        this.material = material == null ? materialPorDefecto() : material;
        this.altoRespaldo = altoRespaldo;
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Panel de respaldo con PillowMesh (inflado) para mejor apariencia
        MeshView respaldo = new MeshView(PillowMesh.crearDefecto(ancho, altoRespaldo, fondo, 4));
        respaldo.setMaterial(material);
        respaldo.setCullFace(javafx.scene.shape.CullFace.NONE);

        // Inclinar hacia atrás 8°
        respaldo.getTransforms().add(new Rotate(-8, 0, 0, 0, Rotate.X_AXIS));

        agregar(g, respaldo, 0, altoRespaldo / 2, fondo / 2 - 10);

        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
