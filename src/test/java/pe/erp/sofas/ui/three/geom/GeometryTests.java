/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: GeometryTests
 * Descripción: Tests de validación para geometrías mejoradas (Fase 3).
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
package pe.erp.sofas.ui.three.geom;

import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de validación para geometrías mejoradas (Fase 3).
 * Aseguran que PillowMesh y PipingMesh generen mallas válidas
 * y que el rendimiento esté bajo control.
 */
class GeometryTests {

    @Test
    void testPillowMeshCreation() {
        TriangleMesh malla = PillowMesh.crear(52, 14, 92, 4, 0.8, 6);
        assertNotNull(malla);
        assertTrue(malla.getPoints().size() > 0);
        assertTrue(malla.getNormals().size() > 0);
        assertTrue(malla.getFaces().size() > 0);
    }

    @Test
    void testPillowMeshTriangleCount() {
        TriangleMesh malla = PillowMesh.crear(52, 14, 92, 4, 0.8, 6);
        // Malla tiene 6 caras, cada una subdividida en seg×seg
        // Cada cara: (2n)×(2n) vértices = (2n-1)×(2n-1)×2 triángulos
        // Con seg=6: n=7, lado=14: (14-1)×(14-1)×2 = 13×13×2 = 338 tri/cara
        // Total: 6×338 = 2028 triángulos ≈ 6084 caras (3 índices por cara)
        int numCaras = malla.getFaces().size() / 9;  // 9 índices por triángulo (3 vértices × 3)
        assertTrue(numCaras > 1000, "Debe haber >1000 triángulos para buena calidad");
        assertTrue(numCaras < 5000, "Debe tener <5000 triángulos para rendimiento");
    }

    @Test
    void testPillowMeshVariousSizes() {
        // Validar que funciona con diferentes tamaños
        PillowMesh.crear(100, 20, 120, 5, 1.0, 8);  // Grande
        PillowMesh.crear(40, 10, 60, 3, 0.6, 4);    // Pequeño
        PillowMesh.crear(200, 85, 200, 8, 1.2, 10); // Muy grande
        // Si no hay excepción, pasó
        assertTrue(true);
    }

    @Test
    void testPillowMeshDefaults() {
        TriangleMesh malla = PillowMesh.crearDefecto(52, 14, 92, 4);
        assertNotNull(malla);
        assertTrue(malla.getPoints().size() > 0);
    }

    @Test
    void testPipingMeshCreation() {
        TriangleMesh malla = PipingMesh.crearRibeteRectangular(52, 92, 0.4, 8);
        assertNotNull(malla);
        assertTrue(malla.getPoints().size() > 0);
        assertTrue(malla.getNormals().size() > 0);
        assertTrue(malla.getFaces().size() > 0);
    }

    @Test
    void testPipingMeshDefaults() {
        TriangleMesh malla = PipingMesh.crearDefecto(52, 92);
        assertNotNull(malla);
        assertTrue(malla.getPoints().size() > 100);
    }

    @Test
    void testCombinedGeometryPerformance() {
        // Simular crear geometría para un sofá Moderno 3 cuerpos
        // 3 cojines de asiento + 1 respaldo = 4 mallas PillowMesh
        // Total triángulos debería estar bajo 60k (límite recomendado)

        int totalTriangulos = 0;

        // 3 cojines de asiento (52×14×92)
        for (int i = 0; i < 3; i++) {
            TriangleMesh cojin = PillowMesh.crearDefecto(52, 14, 92, 4);
            totalTriangulos += cojin.getFaces().size() / 9;
        }

        // 1 respaldo (156×85×20)
        TriangleMesh respaldo = PillowMesh.crearDefecto(156, 85, 20, 4);
        totalTriangulos += respaldo.getFaces().size() / 9;

        System.out.println("Sofá Moderno 3 cuerpos: " + totalTriangulos + " triángulos");

        // Debe estar bajo 60k triángulos
        assertTrue(totalTriangulos < 60000,
            "Sofá Moderno 3 cuerpos debe tener <60k triángulos, tiene " + totalTriangulos);
        assertTrue(totalTriangulos > 2000,
            "Sofá debe tener >2k triángulos para buena calidad");
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
