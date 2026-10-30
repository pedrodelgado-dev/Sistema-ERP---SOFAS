/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SeccionalLEsquineroFamily
 * Descripción: Familia Seccional L con Esquinero: lado A + módulo esquina + lado B.
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

import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.model.ConfigSofa;
import pe.erp.sofas.ui.three.parts.SofaPart;
import pe.erp.sofas.ui.three.parts.concrete.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Familia Seccional L con Esquinero: lado A + módulo esquina + lado B.
 * El esquinero es cuadrado con respaldos en dos lados (NUEVO en Fase 2).
 * Código: SF-012
 */
public class SeccionalLEsquineroFamily implements FamiliaFactory.FamiliaConstructor {

    private static final double ANCHO_CUERPO = 52;
    private static final double TAMANIO_ESQUINERO = 95;
    private static final double FONDO_ESTANDAR = 92;
    private static final double ALTO_ESTANDAR = 85;

    @Override
    public List<SofaPart> construir(ConfigSofa config, PhongMaterial materialTela, PhongMaterial materialRespaldo) {
        List<SofaPart> partes = new ArrayList<>();

        int cuerposA = 3;  // Lado A: 3 cuerpos típico
        int cuerposB = 2;  // Lado B: 2 cuerpos típico
        double largoA = cuerposA * ANCHO_CUERPO;
        double largoB = cuerposB * ANCHO_CUERPO;
        double largoTotal = largoA + TAMANIO_ESQUINERO + largoB;
        double fondo = config.profundidad();
        double alto = ALTO_ESTANDAR;

        PhongMaterial matResp = materialRespaldo == null ? materialTela : materialRespaldo;

        // Base de asiento
        partes.add(new BaseSeatFramePart(largoTotal, fondo, 13, materialTela));

        // Lado A: cojines principales
        for (int i = 0; i < cuerposA; i++) {
            partes.add(new SeatCushionPart(ANCHO_CUERPO - 1, fondo, materialTela));
        }

        // Esquinero en el centro
        partes.add(new CornerPart(TAMANIO_ESQUINERO, materialTela, matResp));

        // Lado B: cojines secundarios
        for (int i = 0; i < cuerposB; i++) {
            partes.add(new SeatCushionPart(ANCHO_CUERPO - 1, fondo, materialTela));
        }

        // Respaldo lado A
        partes.add(new BackrestPart(largoA - 34, alto - 18 - 3, matResp));

        // Brazo lado A (extremo)
        partes.add(new ArmPart(ArmPart.TipoBrazo.RECTO, alto * 0.7, fondo, materialTela));

        // Brazo lado B (extremo)
        partes.add(new ArmPart(ArmPart.TipoBrazo.RECTO, alto * 0.7, fondo, materialTela));

        // Patas
        partes.add(new LegsPart(largoTotal, fondo, LegsPart.TipoPata.MADERA_CONICA, materialTela));

        return partes;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
