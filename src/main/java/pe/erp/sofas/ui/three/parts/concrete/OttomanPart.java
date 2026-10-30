/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: OttomanPart
 * Descripción: Puff u ottomán: módulo bajo sin respaldo, usado como posapies o asiento extra.
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
 * Puff u ottomán: módulo bajo sin respaldo, usado como posapies o asiento extra.
 * Típicamente cuadrado o rectangular, altura baja (40-50 cm).
 */
public class OttomanPart extends BaseSofaPart {

    private final PhongMaterial material;

    public OttomanPart(double ancho, double fondo, PhongMaterial material) {
        super("Puff", ancho, fondo, 40);  // Altura baja
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Cuerpo principal del puff: caja redondeada
        agregar(g, cajaRedondeada(ancho, alto, fondo, 6, material), 0, alto / 2, 0);

        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
