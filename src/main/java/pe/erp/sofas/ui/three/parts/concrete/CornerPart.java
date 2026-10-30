/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CornerPart
 * Descripción: Esquinero: módulo cuadrado de 90° para seccionales en L y U.
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
 * Esquinero: módulo cuadrado de 90° para seccionales en L y U.
 * Tiene respaldo en sus dos lados interiores (norte y este por ejemplo).
 * Cojín de asiento cuadrado en el centro.
 * Tamaño típico: 95×95 o 105×105 cm.
 */
public class CornerPart extends BaseSofaPart {

    private final PhongMaterial materialAsiento;
    private final PhongMaterial materialRespaldo;

    public CornerPart(double tamanio, PhongMaterial materialAsiento, PhongMaterial materialRespaldo) {
        super("Esquinero", tamanio, tamanio, 85);  // Cuadrado, altura 85 cm estándar
        this.materialAsiento = materialAsiento == null ? materialPorDefecto() : materialAsiento;
        this.materialRespaldo = materialRespaldo == null ? materialPorDefecto() : materialRespaldo;
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Base del esquinero (igual al asiento)
        agregar(g, cajaRedondeada(ancho, 18, fondo, 3, materialAsiento), 0, 18 / 2, 0);

        // Cojín de asiento cuadrado en el centro
        double alturaCojin = 14;
        agregar(g, cajaRedondeada(ancho - 4, alturaCojin, fondo - 4, 5, materialAsiento),
                0, 18 + alturaCojin / 2, 0);

        // Respaldos: dos paneles perpendiculares (norte y este)
        double altoResp = alto - 18 - 6;  // Alto total menos base y espacio

        // Respaldo norte (eje +Z)
        agregar(g, cajaRedondeada(ancho - 4, altoResp, 20, 6, materialRespaldo),
                0, 18 + altoResp / 2, fondo / 2 - 10);

        // Respaldo este (eje +X)
        agregar(g, cajaRedondeada(20, altoResp, fondo - 4, 6, materialRespaldo),
                ancho / 2 - 10, 18 + altoResp / 2, 0);

        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
