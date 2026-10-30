/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: TuftingMeshTest
 * Descripción: Tests para capitoné (TuftingMesh).
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
 * Tests para capitoné (TuftingMesh).
 */
class TuftingMeshTest {

    @Test
    void testTuftingMeshCreation() {
        TriangleMesh malla = TuftingMesh.crear(156, 85, 20, 13, 1.2, 4);
        assertNotNull(malla);
        assertTrue(malla.getPoints().size() > 0);
        assertTrue(malla.getNormals().size() > 0);
        assertTrue(malla.getFaces().size() > 0);
    }

    @Test
    void testTuftingMeshDefaults() {
        TriangleMesh malla = TuftingMesh.crearDefecto(156, 85, 20);
        assertNotNull(malla);
        assertTrue(malla.getPoints().size() > 100);
    }

    @Test
    void testTuftingMeshTriangleCount() {
        // Un respaldo Chesterfield (156×85)
        TriangleMesh malla = TuftingMesh.crearDefecto(156, 85, 20);
        int numTriangulos = malla.getFaces().size() / 9;

        System.out.println("TuftingMesh triángulos: " + numTriangulos);

        // Debe tener suficientes triángulos para la geometría capitoné
        assertTrue(numTriangulos > 100, "Capitoné debe tener >100 triángulos");
        assertTrue(numTriangulos < 5000, "Capitoné debe tener <5k triángulos");
    }

    @Test
    void testTuftingMeshVariousSizes() {
        // Validar múltiples tamaños
        TuftingMesh.crear(100, 70, 15, 12, 1.0, 3);
        TuftingMesh.crear(200, 100, 25, 14, 1.5, 5);
        TuftingMesh.crear(50, 50, 10, 10, 0.8, 2);
        // Si no hay excepción, pasó
        assertTrue(true);
    }

    @Test
    void testTuftingMeshPerformance() {
        // Capitoné para Chesterfield 3 cuerpos (respaldo largo)
        TriangleMesh respaldo = TuftingMesh.crearDefecto(156, 85, 20);

        int numTriangulos = respaldo.getFaces().size() / 9;
        System.out.println("Respaldo Chesterfield capitoné: " + numTriangulos + " triángulos");

        // Debe estar en rango aceptable
        assertTrue(numTriangulos < 3000,
            "Respaldo Chesterfield debe tener <3000 triángulos para perf, tiene " + numTriangulos);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
