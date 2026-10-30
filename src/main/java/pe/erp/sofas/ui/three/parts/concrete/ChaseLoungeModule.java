/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ChaseLoungeModule
 * Descripción: Chaise lounge: módulo extendido para apoyar las piernas.
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
package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.ui.three.parts.BaseSofaPart;

/**
 * Chaise lounge: módulo extendido para apoyar las piernas.
 * Es como un asiento adicional pero más largo (típicamente 65 cm de largo).
 * Se posiciona a la derecha o izquierda del sofá principal.
 */
public class ChaseLoungeModule extends BaseSofaPart {

    private final PhongMaterial material;

    public ChaseLoungeModule(double largo, PhongMaterial material) {
        super("Chaise lounge", largo, 92, 85);  // 65 cm de largo típico, 92 cm de fondo
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Base de la chaise
        agregar(g, cajaRedondeada(ancho, 18, fondo, 3, material), 0, 18 / 2, 0);

        // Cojín de asiento alargado
        double largoCojin = ancho - 4;
        agregar(g, cajaRedondeada(largoCojin, 14, fondo - 4, 5, material),
                0, 18 + 14 / 2, 0);

        // Brazo en una punta (pequeño remate)
        agregar(g, cajaRedondeada(17, 14, 20, 4, material),
                ancho / 2 - 17 / 2, 18 + 14 / 2, -fondo / 2 + 10);

        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
