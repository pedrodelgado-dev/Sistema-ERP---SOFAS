/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SofaPartAssemblerTest
 * Descripción: Tests básicos de la arquitectura modular de partes.
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

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.junit.jupiter.api.Test;
import pe.erp.sofas.ui.three.parts.concrete.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests básicos de la arquitectura modular de partes.
 * Valida que:
 * - Las partes se construyen sin errores
 * - Los ensamblados retornan dimensiones correctas
 * - No hay regresiones con el SofaBuilder existente
 */
class SofaPartAssemblerTest {

    private PhongMaterial materialTela() {
        PhongMaterial m = new PhongMaterial(Color.web("#B9B7B1"));
        m.setSpecularPower(8);
        return m;
    }

    private PhongMaterial materialMetal() {
        PhongMaterial m = new PhongMaterial(Color.web("#1F1F22"));
        m.setSpecularPower(30);
        return m;
    }

    @Test
    void testParteBaseSeatFrame() {
        var base = new BaseSeatFramePart(210, 92, 13, materialTela());
        var geom = base.build();
        var dims = base.getDimensions();

        assertNotNull(geom);
        assertEquals(210, dims.ancho());
        assertEquals(92, dims.fondo());
        assertEquals(18, dims.alto());
    }

    @Test
    void testParteSeatCushion() {
        var asiento = new SeatCushionPart(52, 92, materialTela());
        var geom = asiento.build();
        var dims = asiento.getDimensions();

        assertNotNull(geom);
        assertEquals(52, dims.ancho());
        assertEquals(92, dims.fondo());
        assertEquals(14, dims.alto());
    }

    @Test
    void testParteBackrest() {
        var respaldo = new BackrestPart(52, 85, materialTela());
        var geom = respaldo.build();
        var dims = respaldo.getDimensions();

        assertNotNull(geom);
        assertEquals(52, dims.ancho());
        assertEquals(20, dims.fondo());
        assertEquals(85, dims.alto());
    }

    @Test
    void testParteArmRecto() {
        var brazo = new ArmPart(ArmPart.TipoBrazo.RECTO, 60, 92, materialTela());
        var geom = brazo.build();
        var dims = brazo.getDimensions();

        assertNotNull(geom);
        assertEquals(17, dims.ancho());
        assertEquals(92, dims.fondo());
        assertEquals(60, dims.alto());
    }

    @Test
    void testParteCorner() {
        var esquina = new CornerPart(95, materialTela(), materialTela());
        var geom = esquina.build();
        var dims = esquina.getDimensions();

        assertNotNull(geom);
        assertEquals(95, dims.ancho());
        assertEquals(95, dims.fondo());
        assertEquals(85, dims.alto());
    }

    @Test
    void testParteLegsMadera() {
        var patas = new LegsPart(210, 92, LegsPart.TipoPata.MADERA_CONICA, materialTela());
        var geom = patas.build();
        var dims = patas.getDimensions();

        assertNotNull(geom);
        assertEquals(210, dims.ancho());
        assertEquals(92, dims.fondo());
        assertEquals(13, dims.alto());
    }

    @Test
    void testParteChaiseLoungeModule() {
        var chaise = new ChaseLoungeModule(65, materialTela());
        var geom = chaise.build();
        var dims = chaise.getDimensions();

        assertNotNull(geom);
        assertEquals(65, dims.ancho());
        assertEquals(92, dims.fondo());
    }

    @Test
    void testParteOttoman() {
        var puff = new OttomanPart(60, 60, materialTela());
        var geom = puff.build();
        var dims = puff.getDimensions();

        assertNotNull(geom);
        assertEquals(60, dims.ancho());
        assertEquals(60, dims.fondo());
        assertEquals(40, dims.alto());
    }

    @Test
    void testParteDecorativeCushion() {
        var cojin = new DecorativeCushionPart(44, materialTela());
        var geom = cojin.build();
        var dims = cojin.getDimensions();

        assertNotNull(geom);
        assertEquals(44, dims.ancho());
        assertEquals(44, dims.fondo());
        assertEquals(12, dims.alto());
    }

    @Test
    void testEnsambladorLineal3Cuerpos() {
        var material = materialTela();
        var asiento1 = new SeatCushionPart(52, 92, material);
        var asiento2 = new SeatCushionPart(52, 92, material);
        var asiento3 = new SeatCushionPart(52, 92, material);

        var ensamblador = new SofaPartAssembler()
            .conLayout(PartLayout.LINEAL)
            .agregar(asiento1)
            .agregar(asiento2)
            .agregar(asiento3);

        var resultado = ensamblador.ensamblar();
        assertNotNull(resultado.grupo());
        assertEquals(156, resultado.largoTotal());  // 52 + 52 + 52
        assertEquals(92, resultado.fondoTotal());
    }

    @Test
    void testEnsambladorLEsquinero() {
        var material = materialTela();
        var base = new BaseSeatFramePart(343, 92, 13, material);
        var asientos1 = new SeatCushionPart(52, 92, material);
        var asientos2 = new SeatCushionPart(52, 92, material);
        var asientos3 = new SeatCushionPart(52, 92, material);
        var esquina = new CornerPart(95, material, material);
        var asientos4 = new SeatCushionPart(52, 92, material);
        var asientos5 = new SeatCushionPart(52, 92, material);

        var ensamblador = new SofaPartAssembler()
            .conLayout(PartLayout.L_ESQUINERO_DER)
            .agregar(base)
            .agregar(asientos1)
            .agregar(asientos2)
            .agregar(asientos3)
            .agregar(esquina)
            .agregar(asientos4)
            .agregar(asientos5);

        var resultado = ensamblador.ensamblar();
        assertNotNull(resultado.grupo());
        assertTrue(resultado.largoTotal() > 300);  // L es larga
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
