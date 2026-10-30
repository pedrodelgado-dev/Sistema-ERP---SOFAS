/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CalculoPrecioServiceTest
 * Descripción: Archivo fuente Java de la clase CalculoPrecioServiceTest.
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
package pe.erp.sofas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.erp.sofas.model.ConfigSofa;
import pe.erp.sofas.repository.FichaTecnicaRepository;
import pe.erp.sofas.repository.TelaRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CalculoPrecioServiceTest {

    private CalculoPrecioService service;

    @Mock
    private FichaTecnicaRepository fichaTecnicaRepo;

    @Mock
    private TelaRepository telaRepo;

    private ConfigSofa configTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CalculoPrecioService(fichaTecnicaRepo, telaRepo);

        configTest = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO,
            3,
            ConfigSofa.Chaise.NINGUNA,
            92,
            null,
            ConfigSofa.Brazo.RECTO,
            ConfigSofa.Patas.MADERA,
            ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23,
            1,
            null,
            ConfigSofa.Mecanismo.FIJO
        );
    }

    @Test
    void testCalcularPrecioSinFicha() {
        when(fichaTecnicaRepo.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        BigDecimal precio = service.calcularPrecio(configTest);

        assertNotNull(precio);
        assertTrue(precio.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalcularPrecioConDescuento() {
        when(fichaTecnicaRepo.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        BigDecimal precioSinDescuento = service.calcularPrecio(configTest, BigDecimal.ZERO);
        BigDecimal precioConDescuento10 = service.calcularPrecio(configTest, BigDecimal.TEN);

        assertTrue(precioSinDescuento.compareTo(precioConDescuento10) > 0);
    }

    @Test
    void testCalcularPrecioConEspumaD30() {
        when(fichaTecnicaRepo.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        ConfigSofa conD30 = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 3, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D30, 1, null, ConfigSofa.Mecanismo.FIJO
        );

        BigDecimal precioD30 = service.calcularPrecio(conD30);

        assertTrue(precioD30.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalcularPrecioConReclinableManual() {
        when(fichaTecnicaRepo.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        ConfigSofa conReclinable = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 3, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 1, null, ConfigSofa.Mecanismo.RECLINABLE_MANUAL
        );

        BigDecimal precioReclinable = service.calcularPrecio(conReclinable);

        assertTrue(precioReclinable.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testDesglosePositivo() {
        when(fichaTecnicaRepo.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        CalculoPrecioService.DesglosePrecio desglose = service.desglosar(configTest, BigDecimal.TEN);

        assertNotNull(desglose);
        assertTrue(desglose.costoBOM().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(desglose.precioFinal().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testPrecioEsPositivo() {
        when(fichaTecnicaRepo.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        BigDecimal precio = service.calcularPrecio(configTest);

        assertTrue(precio.compareTo(BigDecimal.ZERO) > 0);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
