/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: FamiliaFactory
 * Descripción: Factory para construir familias de sofás.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory para construir familias de sofás.
 * Dado una familia, configuración y materiales, retorna la lista de partes que componen el sofá.
 *
 * Patrón: Strategy + Factory. Cada familia tiene su propio constructor (ej: ModernoFamily).
 */
public class FamiliaFactory {

    private final Map<SofaFamilia, FamiliaConstructor> constructores = new HashMap<>();

    public FamiliaFactory() {
        // Registrar cada familia con su constructor
        constructores.put(SofaFamilia.MODERNO, new ModernoFamily());
        constructores.put(SofaFamilia.SECCIONAL_L_CHAISE, new SeccionalLChaiseFamily());
        constructores.put(SofaFamilia.SECCIONAL_L_ESQUINERO, new SeccionalLEsquineroFamily());
        constructores.put(SofaFamilia.SECCIONAL_U, new SeccionalUFamily());
        constructores.put(SofaFamilia.MODULAR, new ModularFamily());
        constructores.put(SofaFamilia.CHESTERFIELD, new ChesterfieldFamily());
        constructores.put(SofaFamilia.ESCANDINAVO, new EscandinavoFamily());
        constructores.put(SofaFamilia.SOFA_CAMA, new SofaCamaFamily());
        constructores.put(SofaFamilia.RECLINABLE, new ReclinableFamily());
        constructores.put(SofaFamilia.RETRO, new RetroFamily());
        constructores.put(SofaFamilia.CLOUD, new CloudFamily());
        constructores.put(SofaFamilia.TUXEDO, new TuxedoFamily());
        constructores.put(SofaFamilia.SILLON, new SillonFamily());
    }

    /**
     * Construye la lista de partes para una familia.
     *
     * @param familia la familia (Moderno, L, etc)
     * @param config configuración del sofá (cuerpos, chaise, brazos, etc)
     * @param materialTela material de la tela principal
     * @param materialRespaldo material del respaldo (si es diferente)
     * @return List<SofaPart> ordenadas para ensamblar
     */
    public List<SofaPart> construir(SofaFamilia familia, ConfigSofa config,
                                   PhongMaterial materialTela, PhongMaterial materialRespaldo) {
        FamiliaConstructor constructor = constructores.get(familia);
        if (constructor == null) {
            throw new IllegalArgumentException("Familia no registrada: " + familia);
        }
        return constructor.construir(config, materialTela, materialRespaldo);
    }

    /**
     * Interface que deben implementar todas las familias.
     */
    public interface FamiliaConstructor {
        /**
         * Construye la lista de partes para esta familia.
         */
        List<SofaPart> construir(ConfigSofa config, PhongMaterial materialTela, PhongMaterial materialRespaldo);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
