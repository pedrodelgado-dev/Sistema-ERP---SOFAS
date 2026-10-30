/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ArmPart
 * Descripción: Brazo del sofá: puede ser recto, redondeado o delgado.
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
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import pe.erp.sofas.ui.three.parts.BaseSofaPart;

/**
 * Brazo del sofá: puede ser recto, redondeado o delgado.
 * Tipos: RECTO (17 cm ancho), REDONDEADO (18 cm), DELGADO (9 cm).
 * Altura: variable según el tipo (típico 70% del alto total del sofá).
 */
public class ArmPart extends BaseSofaPart {

    public enum TipoBrazo {
        RECTO(17, 4),
        REDONDEADO(18, 9),
        DELGADO(9, 3);

        final double ancho;
        final double radio;

        TipoBrazo(double ancho, double radio) {
            this.ancho = ancho;
            this.radio = radio;
        }
    }

    private final TipoBrazo tipo;
    private final PhongMaterial material;
    private final double altoBrazo;

    public ArmPart(TipoBrazo tipo, double altoBrazo, double fondo, PhongMaterial material) {
        super("Brazo " + tipo.name(), tipo.ancho, fondo, altoBrazo);
        this.tipo = tipo;
        this.material = material == null ? materialPorDefecto() : material;
        this.altoBrazo = altoBrazo;
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Cuerpo principal del brazo
        agregar(g, cajaRedondeada(ancho, altoBrazo, fondo, tipo.radio, material), 0, altoBrazo / 2, 0);

        // Si es redondeado, agregar un cilindro decorativo enrollado
        if (tipo == TipoBrazo.REDONDEADO) {
            Cylinder rollo = new Cylinder(ancho / 2 + 1, fondo - 2, 28);
            rollo.setMaterial(material);
            rollo.getTransforms().addAll(
                new Translate(0, -(altoBrazo - 1), 0),
                new Rotate(90, Rotate.X_AXIS)
            );
            g.getChildren().add(rollo);
        }

        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
