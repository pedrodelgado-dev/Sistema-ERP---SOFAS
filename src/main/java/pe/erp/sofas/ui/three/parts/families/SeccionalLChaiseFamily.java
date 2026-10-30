/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SeccionalLChaiseFamily
 * Descripción: Familia Seccional L con Chaise: sofá principal + chaise lounge extendida.
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
 * Familia Seccional L con Chaise: sofá principal + chaise lounge extendida.
 * La chaise puede estar a la derecha o izquierda.
 * Código: SF-005
 */
public class SeccionalLChaiseFamily implements FamiliaFactory.FamiliaConstructor {

    private static final double ANCHO_CUERPO = 52;
    private static final double LARGO_CHAISE = 65;
    private static final double FONDO_ESTANDAR = 92;
    private static final double ALTO_ESTANDAR = 85;

    @Override
    public List<SofaPart> construir(ConfigSofa config, PhongMaterial materialTela, PhongMaterial materialRespaldo) {
        List<SofaPart> partes = new ArrayList<>();

        int cuerpos = config.cuerpos();
        double largoSofa = cuerpos * ANCHO_CUERPO;
        double largoTotal = largoSofa + LARGO_CHAISE;
        double fondo = config.profundidad();
        double alto = ALTO_ESTANDAR;
        boolean chaiseDerecha = config.chaise() == ConfigSofa.Chaise.DERECHA;

        PhongMaterial matResp = materialRespaldo == null ? materialTela : materialRespaldo;

        // 1. Base de asiento (armazón largo para sofá + chaise)
        partes.add(new BaseSeatFramePart(largoTotal, fondo, 13, materialTela));

        // 2. Cojines de asiento principales (uno por cuerpo del sofá principal)
        for (int i = 0; i < cuerpos; i++) {
            partes.add(new SeatCushionPart(ANCHO_CUERPO - 1, fondo, materialTela));
        }

        // 3. Respaldo del sofá principal
        partes.add(new BackrestPart(largoSofa - 34, alto - 18 - 3, matResp));

        // 4. Brazos (izquierdo y derecho)
        // Brazo del lado sin chaise (siempre completo)
        if (chaiseDerecha) {
            // Brazo izquierdo completo
            partes.add(new ArmPart(ArmPart.TipoBrazo.RECTO, alto * 0.7, fondo, materialTela));
        } else {
            // Brazo derecho completo
            partes.add(new ArmPart(ArmPart.TipoBrazo.RECTO, alto * 0.7, fondo, materialTela));
        }

        // 5. Chaise lounge
        partes.add(new ChaseLoungeModule(LARGO_CHAISE, materialTela));

        // 6. Patas
        partes.add(new LegsPart(largoTotal, fondo, LegsPart.TipoPata.MADERA_CONICA, materialTela));

        // 7. Cojines decorativos (si está configurado)
        if (config.cojines() > 0) {
            double tamCojin = Math.min(44, ANCHO_CUERPO * 0.75);
            for (int i = 0; i < config.cojines(); i++) {
                partes.add(new DecorativeCushionPart(tamCojin, materialTela));
            }
        }

        return partes;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
