/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ComparadorControllerTest
 * Descripción: Archivo fuente Java de la clase ComparadorControllerTest.
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
package pe.erp.sofas.ui.views.comparador;

import org.junit.jupiter.api.Test;
import pe.erp.sofas.core.Session;
import pe.erp.sofas.core.Toast;
import pe.erp.sofas.service.ConfiguradorService;
import pe.erp.sofas.service.GestorCarritoService;
import pe.erp.sofas.service.PersistenciaConfiguracionService;
import pe.erp.sofas.service.TelaService;
import pe.erp.sofas.ui.AppWindow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComparadorControllerTest {

    @Test
    void testComparadorCreation() {
        ConfiguradorService configuradorMock = mock(ConfiguradorService.class);
        TelaService telasMock = mock(TelaService.class);
        Toast toastMock = mock(Toast.class);
        AppWindow appWindowMock = mock(AppWindow.class);
        GestorCarritoService gestorMock = mock(GestorCarritoService.class);
        PersistenciaConfiguracionService persistenciaMock = mock(PersistenciaConfiguracionService.class);
        Session sessionMock = mock(Session.class);

        ComparadorController controller = new ComparadorController(
            configuradorMock, telasMock, toastMock, appWindowMock, gestorMock, persistenciaMock, sessionMock
        );
        assertNotNull(controller);
    }

    @Test
    void testComparisonRow() {
        ComparadorController.ComparisonRow row = new ComparadorController.ComparisonRow(
            "Precio", "S/ 5000", "S/ 6000", "S/ 7000"
        );
        assertEquals("Precio", row.propiedad);
        assertEquals("S/ 5000", row.opcion1);
        assertEquals("S/ 6000", row.opcion2);
        assertEquals("S/ 7000", row.opcion3);
    }

    @Test
    void testComparisonRowAllFields() {
        ComparadorController.ComparisonRow row = new ComparadorController.ComparisonRow(
            "Modelo", "Moderno", "Chesterfield", "Cloud"
        );
        assertNotNull(row.propiedad);
        assertNotNull(row.opcion1);
        assertNotNull(row.opcion2);
        assertNotNull(row.opcion3);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
