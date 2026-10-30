/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: LegsPart
 * Descripción: Patas del sofá: pueden ser madera cónica, metal, o zócalo oculto.
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
import pe.erp.sofas.ui.three.parts.BaseSofaPart;
import pe.erp.sofas.ui.three.Mallas;

/**
 * Patas del sofá: pueden ser madera cónica, metal, o zócalo oculto.
 * Se distribuyen en las esquinas del sofá.
 * Esta parte no tiene visualización directa (es accesoria);
 * más bien, es un placeholder para que otras partes sepan dónde van las patas.
 */
public class LegsPart extends BaseSofaPart {

    public enum TipoPata {
        MADERA_CONICA(13),
        METAL_NEGRO(16),
        METAL_DORADO(16),
        ZOCALO(7);

        final double altura;

        TipoPata(double altura) {
            this.altura = altura;
        }
    }

    private final TipoPata tipo;
    private final PhongMaterial material;

    public LegsPart(double ancho, double fondo, TipoPata tipo, PhongMaterial material) {
        super("Patas " + tipo.name(), ancho, fondo, tipo.altura);
        this.tipo = tipo;
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group g = new Group();

        // Las patas son pequeñas, solo dibujamos 4 en las esquinas
        double[] xs = {-ancho / 2 + 7, ancho / 2 - 7};
        double[] zs = {-fondo / 2 + 7, fondo / 2 - 7};

        for (double x : xs) {
            for (double z : zs) {
                pata(g, x, z, alto);
            }
        }

        return g;
    }

    private void pata(Group g, double x, double z, double h) {
        switch (tipo) {
            case ZOCALO -> {
                // Zócalo: solo un pequeño rectángulo oculto
                agregar(g, cajaRedondeada(2, h, 2, 0.5, material), x, h / 2, z);
            }
            default -> {
                // Cono o cilindro de metal
                Cylinder pata = new Cylinder(1.1, h, 14);
                pata.setMaterial(material);
                pata.setTranslateX(x);
                pata.setTranslateY(-h / 2);
                pata.setTranslateZ(z);
                g.getChildren().add(pata);
            }
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
