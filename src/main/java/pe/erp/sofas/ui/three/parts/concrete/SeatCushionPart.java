/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SeatCushionPart
 * Descripción: Cojín de asiento: malla "inflada" (PillowMesh) para aspecto mullido.
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
import pe.erp.sofas.ui.three.geom.PillowMesh;
import pe.erp.sofas.ui.three.parts.BaseSofaPart;

/**
 * Cojín de asiento: malla "inflada" (PillowMesh) para aspecto mullido.
 * Altura típica: 14 cm (asiento confortable).
 * Se posiciona sobre la base.
 *
 * Usa PillowMesh en lugar de cajas redondeadas para un aspecto más orgánico
 * y realista, con caras levemente abombadas hacia afuera.
 */
public class SeatCushionPart extends BaseSofaPart {

    private final PhongMaterial material;

    public SeatCushionPart(double ancho, double fondo, PhongMaterial material) {
        super("Cojín de asiento", ancho, fondo, 14);  // 14 cm de alto
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Usar PillowMesh (inflado) en lugar de caja redondeada
        MeshView cojin = new MeshView(PillowMesh.crearDefecto(ancho, alto, fondo, 4));
        cojin.setMaterial(material);
        cojin.setCullFace(javafx.scene.shape.CullFace.NONE);

        agregar(g, cojin, 0, alto / 2, 0);

        // Opcionalmente, agregar ribete decorativo en el borde (comentado para rendimiento)
        // MeshView ribete = new MeshView(PipingMesh.crearDefecto(ancho, fondo));
        // ribete.setMaterial(material);
        // agregar(g, ribete, 0, alto / 2, 0);

        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
