/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConstructorModularControllerTest
 * Descripción: Archivo fuente Java de la clase ConstructorModularControllerTest.
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
package pe.erp.sofas.ui.views.constructor;

import org.junit.jupiter.api.Test;
import pe.erp.sofas.core.Session;
import pe.erp.sofas.core.Toast;
import pe.erp.sofas.service.ConfiguradorService;
import pe.erp.sofas.service.GestorCarritoService;
import pe.erp.sofas.service.PersistenciaConfiguracionService;
import pe.erp.sofas.service.TelaService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConstructorModularControllerTest {

    @Test
    void testConstructorCreation() {
        ConfiguradorService configuradorMock = mock(ConfiguradorService.class);
        TelaService telasMock = mock(TelaService.class);
        Toast toastMock = mock(Toast.class);
        GestorCarritoService gestorMock = mock(GestorCarritoService.class);
        PersistenciaConfiguracionService persistenciaMock = mock(PersistenciaConfiguracionService.class);
        Session sessionMock = mock(Session.class);

        ConstructorModularController controller = new ConstructorModularController(
            configuradorMock, telasMock, toastMock, gestorMock, persistenciaMock, sessionMock
        );
        assertNotNull(controller);
    }

    @Test
    void testConstructorWithMocks() {
        ConfiguradorService configuradorMock = mock(ConfiguradorService.class);
        TelaService telasMock = mock(TelaService.class);
        Toast toastMock = mock(Toast.class);
        GestorCarritoService gestorMock = mock(GestorCarritoService.class);
        PersistenciaConfiguracionService persistenciaMock = mock(PersistenciaConfiguracionService.class);
        Session sessionMock = mock(Session.class);

        ConstructorModularController controller = new ConstructorModularController(
            configuradorMock, telasMock, toastMock, gestorMock, persistenciaMock, sessionMock
        );

        assertNotNull(controller);
        verify(configuradorMock, never()).defecto(any());
    }

    @Test
    void testConstructorNotNull() {
        ConfiguradorService configuradorMock = mock(ConfiguradorService.class);
        TelaService telasMock = mock(TelaService.class);
        Toast toastMock = mock(Toast.class);
        GestorCarritoService gestorMock = mock(GestorCarritoService.class);
        PersistenciaConfiguracionService persistenciaMock = mock(PersistenciaConfiguracionService.class);
        Session sessionMock = mock(Session.class);

        ConstructorModularController controller = new ConstructorModularController(
            configuradorMock, telasMock, toastMock, gestorMock, persistenciaMock, sessionMock
        );

        assertTrue(controller != null);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
