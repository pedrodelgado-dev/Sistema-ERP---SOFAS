/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: RoomBuilder
 * Descripción: Sala en 3D para "Ver en mi sala": piso de madera, paredes de media altura, puerta y ventana, y los muebles
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
package pe.erp.sofas.ui.three;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

import java.util.List;

/**
 * Sala en 3D para "Ver en mi sala": piso de madera, paredes de media altura, puerta y ventana, y los muebles
 * del plano (sofas generados con el mismo SofaBuilder, puff, alfombra y planta).
 */
public final class RoomBuilder {

    /** Mueble ubicado en planta: x, z del centro (cm, origen en la esquina de la sala), rotacion en grados. */
    public record Ubicado(Node nodo, double x, double z, double rotacion) {
    }

    /** Puerta o ventana en una pared (0 = frente/abajo, 1 = derecha, 2 = fondo/arriba, 3 = izquierda). */
    public record Abertura(int pared, double posicion, double ancho, boolean puerta) {
    }

    private RoomBuilder() {
    }

    /** Material mate; siempre con color especular (sin el, algunas GPU lo pintan negro con luces direccionales). */
    static PhongMaterial mate(String hex) {
        PhongMaterial m = new PhongMaterial(Color.web(hex));
        m.setSpecularColor(Color.gray(0.06));
        m.setSpecularPower(8);
        return m;
    }

    public static Group build(double ancho, double largo, double alto, List<Abertura> aberturas, List<Ubicado> muebles) {
        Group g = new Group();
        MeshView piso = new MeshView(Mallas.plano(ancho, largo));
        PhongMaterial mp = new PhongMaterial(Color.web("#E9D9C2"));
        mp.setDiffuseMap(Texturas.piso(true));
        mp.setSpecularColor(Color.gray(0.08));
        piso.setMaterial(mp);
        piso.setCullFace(CullFace.NONE);
        piso.setTranslateX(ancho / 2);
        piso.setTranslateZ(largo / 2);
        g.getChildren().add(piso);

        double hPared = alto / 2;
        PhongMaterial pared = new PhongMaterial(Color.web("#F4F1EC"));
        pared.setSpecularColor(Color.gray(0.04));
        PhongMaterial zocalo = mate("#D6CFC4");
        double e = 10;
        // paredes: fondo (z = largo), izquierda (x = 0), derecha (x = ancho); el frente queda abierto para mirar
        g.getChildren().add(muro(ancho + 2 * e, hPared, e, ancho / 2, largo + e / 2, pared, aberturas, 2, ancho));
        g.getChildren().add(muro(e, hPared, largo, -e / 2, largo / 2, pared, aberturas, 3, largo));
        g.getChildren().add(muro(e, hPared, largo, ancho + e / 2, largo / 2, pared, aberturas, 1, largo));
        Box z1 = new Box(ancho, 8, 1.5);
        z1.setMaterial(zocalo);
        z1.setTranslateX(ancho / 2);
        z1.setTranslateY(-4);
        z1.setTranslateZ(largo - 1);
        g.getChildren().add(z1);

        for (Abertura a : aberturas) {
            Box panel;
            PhongMaterial m;
            if (a.puerta()) {
                panel = new Box(a.ancho(), Math.min(hPared + 40, 205), 4);
                m = mate("#A47148");
            } else {
                panel = new Box(a.ancho(), hPared * 0.55, 2);
                m = new PhongMaterial(Color.web("#93C5FD"));
                m.setSpecularColor(Color.WHITE);
                m.setSpecularPower(60);
            }
            panel.setMaterial(m);
            double y = a.puerta() ? -panel.getHeight() / 2 : -(hPared * 0.45 + panel.getHeight() / 2);
            switch (a.pared()) {
                case 2 -> {
                    panel.setTranslateX(a.posicion() + a.ancho() / 2);
                    panel.setTranslateZ(largo - 2);
                }
                case 1 -> {
                    panel.setRotationAxis(Rotate.Y_AXIS);
                    panel.setRotate(90);
                    panel.setTranslateX(ancho - 2);
                    panel.setTranslateZ(a.posicion() + a.ancho() / 2);
                }
                case 3 -> {
                    panel.setRotationAxis(Rotate.Y_AXIS);
                    panel.setRotate(90);
                    panel.setTranslateX(2);
                    panel.setTranslateZ(a.posicion() + a.ancho() / 2);
                }
                default -> {
                    panel.setTranslateX(a.posicion() + a.ancho() / 2);
                    panel.setTranslateZ(1);
                    panel.setHeight(4);
                    y = -2;
                }
            }
            panel.setTranslateY(y);
            g.getChildren().add(panel);
        }

        for (Ubicado u : muebles) {
            Group cont = new Group(u.nodo());
            cont.setTranslateX(u.x());
            cont.setTranslateZ(u.z());
            cont.getTransforms().add(new Rotate(u.rotacion(), Rotate.Y_AXIS));
            g.getChildren().add(cont);
        }
        return g;
    }

    private static Node muro(double w, double h, double d, double x, double z, PhongMaterial m, List<Abertura> ab, int pared, double largoPared) {
        Box b = new Box(w, h, d);
        b.setMaterial(m);
        b.setTranslateX(x);
        b.setTranslateY(-h / 2);
        b.setTranslateZ(z);
        return b;
    }

    public static Node alfombra(double w, double d) {
        MeshView b = new MeshView(Mallas.plano(w, d));
        PhongMaterial m = mate("#CBB89B");
        m.setDiffuseMap(Texturas.tejido(pe.erp.sofas.model.TipoTela.BOUCLE));
        b.setMaterial(m);
        b.setCullFace(CullFace.NONE);
        b.setTranslateY(-0.8);
        return b;
    }

    public static Node planta() {
        Group g = new Group();
        Cylinder maceta = new Cylinder(18, 34, 24);
        maceta.setMaterial(mate("#E2E8F0"));
        maceta.setTranslateY(-17);
        g.getChildren().add(maceta);
        PhongMaterial hoja = mate("#4F8A5B");
        for (int i = 0; i < 7; i++) {
            Box h = new Box(10, 62 + i * 4, 2);
            h.setMaterial(hoja);
            h.setTranslateY(-(34 + 30 + i * 2));
            h.getTransforms().addAll(new Rotate(i * 51, Rotate.Y_AXIS), new Rotate(12 + (i % 3) * 6, Rotate.Z_AXIS));
            g.getChildren().add(h);
        }
        return g;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
