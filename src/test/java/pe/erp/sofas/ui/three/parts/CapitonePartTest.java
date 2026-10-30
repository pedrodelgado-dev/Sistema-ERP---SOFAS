/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CapitonePartTest
 * Descripción: Tests para CapitonePart (capitoné con botones).
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
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.junit.jupiter.api.Test;
import pe.erp.sofas.ui.three.parts.concrete.CapitonePart;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para CapitonePart (capitoné con botones).
 */
class CapitonePartTest {

    private PhongMaterial materialTela() {
        PhongMaterial m = new PhongMaterial(Color.web("#5B4135"));  // Cuero cognac
        m.setSpecularPower(40);
        return m;
    }

    @Test
    void testCapitonePartCreation() {
        var capitone = new CapitonePart(156, 85, 20, materialTela());
        assertNotNull(capitone);
        assertEquals("Capitoné", capitone.getName());
    }

    @Test
    void testCapitonePartDimensions() {
        var capitone = new CapitonePart(156, 85, 20, materialTela());
        var dims = capitone.getDimensions();

        assertEquals(156, dims.ancho());
        assertEquals(20, dims.fondo());
        assertEquals(85, dims.alto());
    }

    @Test
    void testCapitonePartBuild() {
        var capitone = new CapitonePart(156, 85, 20, materialTela());
        Group geom = capitone.build();

        assertNotNull(geom);
        assertTrue(geom.getChildren().size() > 1, "Debe tener malla + botones");
    }

    @Test
    void testCapitonePartAnchors() {
        var capitone = new CapitonePart(156, 85, 20, materialTela());
        var anchors = capitone.getAnchors();

        assertNotNull(anchors);
        assertTrue(anchors.containsKey("left"));
        assertTrue(anchors.containsKey("right"));
    }

    @Test
    void testCapitonePartChesterfield() {
        // Caso real: respaldo Chesterfield
        var material = new PhongMaterial(Color.web("#8B4513"));  // Saddle brown
        var capitone = new CapitonePart(156, 85, 20, material);

        Group geom = capitone.build();
        assertNotNull(geom);

        // Debe tener malla + botones
        int numNodos = geom.getChildren().size();
        assertTrue(numNodos > 10, "Chesterfield debe tener muchos botones");
    }

    @Test
    void testCapitonePartNullMaterial() {
        // Con material null debe usar default
        var capitone = new CapitonePart(100, 70, 15, null);
        Group geom = capitone.build();
        assertNotNull(geom);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
