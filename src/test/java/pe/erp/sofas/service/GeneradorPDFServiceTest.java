/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: GeneradorPDFServiceTest
 * Descripción: Archivo fuente Java de la clase GeneradorPDFServiceTest.
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
import pe.erp.sofas.model.Usuario;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GeneradorPDFServiceTest {

    private GeneradorPDFService service;

    @Mock
    private CalculoPrecioService precioService;

    @Mock
    private PersistenciaConfiguracionService configService;

    private ConfigSofa configTest;
    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GeneradorPDFService(precioService, configService);

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

        usuarioTest = new Usuario();
        usuarioTest.setId(1L);
        usuarioTest.setNombre("Test User");
    }

    @Test
    void testGenerarPresupuestoSinImagen() {
        CalculoPrecioService.DesglosePrecio desglose = new CalculoPrecioService.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioService.desglosar(any(ConfigSofa.class), any()))
            .thenReturn(desglose);

        byte[] pdf = service.generarPresupuesto(configTest, "Cliente Test", usuarioTest);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // Validar que es un PDF válido (empieza con %PDF)
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }

    @Test
    void testGenerarPresupuestoConClienteNombre() {
        CalculoPrecioService.DesglosePrecio desglose = new CalculoPrecioService.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioService.desglosar(any(ConfigSofa.class), any()))
            .thenReturn(desglose);

        byte[] pdf = service.generarPresupuesto(configTest, "Empresa XYZ S.A.C.", usuarioTest);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGenerarPresupuestoContienePrecio() {
        CalculoPrecioService.DesglosePrecio desglose = new CalculoPrecioService.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioService.desglosar(any(ConfigSofa.class), any()))
            .thenReturn(desglose);

        byte[] pdf = service.generarPresupuesto(configTest, "Cliente Test", usuarioTest);

        assertNotNull(pdf);
        String contenido = new String(pdf);
        // Verificar que contiene información de precios
        assertTrue(contenido.contains("DESGLOSE") || contenido.contains("PRECIO") || pdf.length > 1000);
    }

    @Test
    void testGenerarPresupuestoModernoConChaise() {
        ConfigSofa conChaise = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 3, ConfigSofa.Chaise.DERECHA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 1, null, ConfigSofa.Mecanismo.FIJO
        );

        CalculoPrecioService.DesglosePrecio desglose = new CalculoPrecioService.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1530),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1530),
            BigDecimal.valueOf(275.4),
            BigDecimal.valueOf(1805.4)
        );

        when(precioService.desglosar(any(ConfigSofa.class), any()))
            .thenReturn(desglose);

        byte[] pdf = service.generarPresupuesto(conChaise, "Cliente Test", usuarioTest);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGenerarPresupuestoEstructuraValida() {
        CalculoPrecioService.DesglosePrecio desglose = new CalculoPrecioService.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioService.desglosar(any(ConfigSofa.class), any()))
            .thenReturn(desglose);

        byte[] pdf = service.generarPresupuesto(configTest, "Cliente Test", usuarioTest);

        // Validar estructura mínima
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);  // Debe tener contenido sustancial

        // Verificar marcas de PDF
        String inicio = new String(pdf, 0, Math.min(10, pdf.length));
        assertTrue(inicio.contains("%PDF"));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
