/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ModularFamily
 * Descripción: Familia Modular: constructor libre, pieza a pieza.
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
 * Familia Modular: constructor libre, pieza a pieza.
 * Código: SF-013
 */
public class ModularFamily implements FamiliaFactory.FamiliaConstructor {
    @Override
    public List<SofaPart> construir(ConfigSofa config, PhongMaterial materialTela, PhongMaterial materialRespaldo) {
        List<SofaPart> partes = new ArrayList<>();
        PhongMaterial matResp = materialRespaldo == null ? materialTela : materialRespaldo;

        // TODO: Implementar módulos libres
        partes.add(new BaseSeatFramePart(200, 92, 13, materialTela));
        return partes;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
