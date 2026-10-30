/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: RibetePart
 * Descripción: Ribete decorativo: tubo delgado que delinea los bordes de un cojín.
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
import pe.erp.sofas.ui.three.geom.PipingMesh;
import pe.erp.sofas.ui.three.parts.BaseSofaPart;

/**
 * Ribete decorativo: tubo delgado que delinea los bordes de un cojín.
 * Se posiciona en el perímetro (típicamente en la arista superior de un cojín de asiento).
 *
 * El ribete es principalmente decorativo pero añade realismo al sofá.
 * Color: típicamente 10-15% más oscuro que el cojín.
 */
public class RibetePart extends BaseSofaPart {

    private final PhongMaterial materialRibete;

    public RibetePart(double ancho, double profundidad, PhongMaterial materialBase) {
        super("Ribete", ancho, profundidad, 1);  // Alto mínimo, casi invisible
        // Ribete: color ligeramente más oscuro que el base
        if (materialBase != null) {
            Color colBase = (Color) materialBase.getDiffuseColor();
            Color colOscuro = colBase.deriveColor(0, 1, 0.85, 1);  // 15% más oscuro
            this.materialRibete = new PhongMaterial(colOscuro);
            this.materialRibete.setSpecularPower(materialBase.getSpecularPower());
        } else {
            this.materialRibete = new PhongMaterial(Color.web("#8B8B8B"));
            this.materialRibete.setSpecularPower(8);
        }
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Crear tubo de ribete alrededor del perímetro
        MeshView ribete = new MeshView(PipingMesh.crearDefecto(ancho, fondo));
        ribete.setMaterial(materialRibete);
        ribete.setCullFace(javafx.scene.shape.CullFace.NONE);

        agregar(g, ribete, 0, 0, 0);

        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
