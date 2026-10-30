/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PersistenciaConfiguracionServiceTest
 * Descripción: Archivo fuente Java de la clase PersistenciaConfiguracionServiceTest.
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
import pe.erp.sofas.model.ConfiguracionGuardada;
import pe.erp.sofas.model.Usuario;
import pe.erp.sofas.repository.ConfiguracionGuardadaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersistenciaConfiguracionServiceTest {

    private PersistenciaConfiguracionService service;

    @Mock
    private ConfiguracionGuardadaRepository repository;

    private Usuario usuarioTest;
    private ConfigSofa configTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PersistenciaConfiguracionService(repository);

        usuarioTest = new Usuario();
        usuarioTest.setId(1L);
        usuarioTest.setNombre("Test User");

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
    void testGuardarConfiguracion() {
        ConfiguracionGuardada guardada = ConfiguracionGuardada.builder()
            .id(1L)
            .usuario(usuarioTest)
            .nombre("Mi Moderno")
            .modelo(ConfigSofa.Modelo.MODERNO)
            .cuerpos(3)
            .build();

        when(repository.save(any(ConfiguracionGuardada.class))).thenReturn(guardada);

        ConfiguracionGuardada resultado = service.guardar(usuarioTest, configTest, "Mi Moderno", "Descripción");

        assertNotNull(resultado);
        assertEquals("Mi Moderno", resultado.getNombre());
        assertEquals(usuarioTest, resultado.getUsuario());
        verify(repository).save(any(ConfiguracionGuardada.class));
    }

    @Test
    void testCargarConfiguracion() {
        ConfiguracionGuardada guardada = ConfiguracionGuardada.builder()
            .id(1L)
            .usuario(usuarioTest)
            .nombre("Mi Moderno")
            .modelo(ConfigSofa.Modelo.MODERNO)
            .cuerpos(3)
            .build();

        when(repository.findByIdAndUsuario(1L, usuarioTest)).thenReturn(Optional.of(guardada));

        ConfigSofa resultado = service.cargar(1L, usuarioTest);

        assertNotNull(resultado);
        assertEquals(ConfigSofa.Modelo.MODERNO, resultado.modelo());
        verify(repository).findByIdAndUsuario(1L, usuarioTest);
    }

    @Test
    void testListarPorUsuario() {
        ConfiguracionGuardada guardada1 = ConfiguracionGuardada.builder()
            .id(1L)
            .usuario(usuarioTest)
            .nombre("Config 1")
            .build();

        ConfiguracionGuardada guardada2 = ConfiguracionGuardada.builder()
            .id(2L)
            .usuario(usuarioTest)
            .nombre("Config 2")
            .build();

        when(repository.findByUsuarioOrderByFechaCreacionDesc(usuarioTest))
            .thenReturn(List.of(guardada2, guardada1));

        List<ConfiguracionGuardada> resultado = service.listarPorUsuario(usuarioTest);

        assertEquals(2, resultado.size());
        verify(repository).findByUsuarioOrderByFechaCreacionDesc(usuarioTest);
    }

    @Test
    void testMarcarFavorito() {
        ConfiguracionGuardada guardada = ConfiguracionGuardada.builder()
            .id(1L)
            .usuario(usuarioTest)
            .nombre("Mi Moderno")
            .esFavorito(false)
            .build();

        when(repository.findByIdAndUsuario(1L, usuarioTest)).thenReturn(Optional.of(guardada));
        when(repository.save(guardada)).thenReturn(guardada);

        service.marcarFavorito(1L, usuarioTest, true);

        assertTrue(guardada.getEsFavorito());
        verify(repository).save(guardada);
    }

    @Test
    void testEliminar() {
        ConfiguracionGuardada guardada = ConfiguracionGuardada.builder()
            .id(1L)
            .usuario(usuarioTest)
            .nombre("Mi Moderno")
            .build();

        when(repository.findByIdAndUsuario(1L, usuarioTest)).thenReturn(Optional.of(guardada));

        service.eliminar(1L, usuarioTest);

        verify(repository).delete(guardada);
    }

    @Test
    void testContarPorUsuario() {
        when(repository.countByUsuario(usuarioTest)).thenReturn(5L);

        long resultado = service.contarPorUsuario(usuarioTest);

        assertEquals(5L, resultado);
        verify(repository).countByUsuario(usuarioTest);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
