/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ModernoFamilyTest
 * Descripción: Tests de validación para la familia Moderno.
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
package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.junit.jupiter.api.Test;
import pe.erp.sofas.model.ConfigSofa;
import pe.erp.sofas.ui.three.parts.PartLayout;
import pe.erp.sofas.ui.three.parts.SofaPartAssembler;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de validación para la familia Moderno.
 * Aseguran que la composición modular produzca las mismas dimensiones que el SofaBuilder.
 */
class ModernoFamilyTest {

    private PhongMaterial materialTela() {
        PhongMaterial m = new PhongMaterial(Color.web("#B9B7B1"));
        m.setSpecularPower(8);
        return m;
    }

    @Test
    void testModerno1Cuerpo() {
        var factory = new FamiliaFactory();
        var config = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 1, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 0, null, ConfigSofa.Mecanismo.FIJO
        );

        var partes = factory.construir(SofaFamilia.MODERNO, config, materialTela(), null);

        // Moderno 1 cuerpo: brazo izq (17) + 1 cojín (52) + brazo der (17) = 86 cm
        assertEquals(3, partes.size(), "Debe tener exactamente 3 partes");

        var ensamblador = new SofaPartAssembler()
            .conLayout(PartLayout.LINEAL)
            .agregarTodas(partes);

        var resultado = ensamblador.ensamblar();
        assertEquals(85, resultado.largoTotal());  // 17 + 51 + 17 (cojín es ANCHO_CUERPO - 1)
        assertEquals(92, resultado.fondoTotal());
    }

    @Test
    void testModerno3Cuerpos() {
        var factory = new FamiliaFactory();
        var config = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 3, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 0, null, ConfigSofa.Mecanismo.FIJO
        );

        var partes = factory.construir(SofaFamilia.MODERNO, config, materialTela(), null);

        // Moderno 3 cuerpos: brazo (17) + 3×cojín (51 c/u) + brazo (17) = 187 cm
        assertEquals(5, partes.size(), "Debe tener exactamente 5 partes");

        var ensamblador = new SofaPartAssembler()
            .conLayout(PartLayout.LINEAL)
            .agregarTodas(partes);

        var resultado = ensamblador.ensamblar();
        assertEquals(187, resultado.largoTotal());  // 17 + 51 + 51 + 51 + 17
        assertEquals(92, resultado.fondoTotal());
        assertEquals(59.5, resultado.altoTotal(), 0.1);  // Brazo: 85 * 0.7 = 59.5
    }

    @Test
    void testModerno4Cuerpos() {
        var factory = new FamiliaFactory();
        var config = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 4, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 0, null, ConfigSofa.Mecanismo.FIJO
        );

        var partes = factory.construir(SofaFamilia.MODERNO, config, materialTela(), null);

        // Moderno 4 cuerpos: brazo (17) + 4×cojín (51 c/u) + brazo (17) = 238 cm
        var ensamblador = new SofaPartAssembler()
            .conLayout(PartLayout.LINEAL)
            .agregarTodas(partes);

        var resultado = ensamblador.ensamblar();
        assertEquals(238, resultado.largoTotal());  // 17 + 51 + 51 + 51 + 51 + 17
        assertEquals(92, resultado.fondoTotal());
    }

    @Test
    void testFamiliaFactoryRegistradoModerno() {
        var factory = new FamiliaFactory();
        var config = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 3, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 0, null, ConfigSofa.Mecanismo.FIJO
        );

        // Factory debe poder construir Moderno sin error
        var partes = factory.construir(SofaFamilia.MODERNO, config, materialTela(), null);
        assertNotNull(partes);
        assertTrue(partes.size() > 0, "Debe retornar al menos 1 parte");
    }

    @Test
    void testSeccionalLEsquinero() {
        var factory = new FamiliaFactory();
        var config = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 3, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 0, null, ConfigSofa.Mecanismo.FIJO
        );

        // Familia nueva: L esquinero
        var partes = factory.construir(SofaFamilia.SECCIONAL_L_ESQUINERO, config, materialTela(), null);
        assertNotNull(partes);
        assertTrue(partes.size() > 5, "L esquinero debe tener varios módulos");
    }

    @Test
    void testTodas13FamiliasCompilan() {
        var factory = new FamiliaFactory();
        var config = new ConfigSofa(
            ConfigSofa.Modelo.MODERNO, 2, ConfigSofa.Chaise.NINGUNA, 92, null,
            ConfigSofa.Brazo.RECTO, ConfigSofa.Patas.MADERA, ConfigSofa.AcabadoMadera.NATURAL,
            ConfigSofa.Espuma.D23, 0, null, ConfigSofa.Mecanismo.FIJO
        );

        for (SofaFamilia familia : SofaFamilia.values()) {
            var partes = factory.construir(familia, config, materialTela(), null);
            assertNotNull(partes, "Familia " + familia + " debe compilar");
            assertTrue(partes.size() > 0, "Familia " + familia + " debe retornar partes");
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
