/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SofaBuilder
 * Descripción: Construye el sofa en 3D (cm, Y hacia arriba como valores negativos, frente hacia −Z): base de caja redondeada
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
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import pe.erp.sofas.model.ConfigSofa;
import pe.erp.sofas.model.Forma;
import pe.erp.sofas.model.Producto;
import pe.erp.sofas.model.Tela;
import pe.erp.sofas.model.TipoTela;

import java.util.HashMap;
import java.util.Map;

/**
 * Construye el sofa en 3D (cm, Y hacia arriba como valores negativos, frente hacia −Z): base de caja redondeada
 * (radio 3), cojines de asiento y respaldo por cuerpo separados 1 cm, brazos segun el tipo, chaise, patas o
 * zocalo y cojines decorativos inclinados 12°. El material de la tela usa specularPower segun el tipo
 * (cuero 40, terciopelo 25, resto 8).
 */
public final class SofaBuilder {

    /** Parametros del sofa a dibujar. */
    public record Opciones(Forma forma, int cuerpos, double largo, double fondo, double alto, ConfigSofa.Chaise chaise,
                           ConfigSofa.Brazo brazo, ConfigSofa.Patas patas, String colorPatas, String colorTela, TipoTela tipoTela,
                           int cojines, String colorCojines, TipoTela tipoCojines) {

        public static Opciones de(ConfigSofa c, Tela tela, Tela telaCojines) {
            return new Opciones(c.modelo().forma(), c.cuerpos(), c.largo(), c.profundidad(), c.alto(), c.chaise(), c.brazo(),
                    c.patas(), c.patas() == ConfigSofa.Patas.MADERA ? c.acabado().hex() : null,
                    tela == null ? "#B9B7B1" : tela.getHex(), tela == null ? TipoTela.CHENILLE : tela.getTipo(), c.cojines(),
                    telaCojines == null ? (tela == null ? "#C9972E" : tela.getHex()) : telaCojines.getHex(),
                    telaCojines == null ? (tela == null ? TipoTela.CHENILLE : tela.getTipo()) : telaCojines.getTipo());
        }

        /** Opciones por defecto para el render de un modelo del catalogo. */
        public static Opciones de(Producto p) {
            Forma f = p.getForma();
            TipoTela tipo = tipoPorNombre(p.getMaterial());
            String color = p.getColorHex() == null ? "#B9B7B1" : p.getColorHex();
            int n = f == Forma.JUEGO ? 3 : Math.max(1, Math.min(4, p.getCuerpos()));
            double l = p.getLargo() == null || p.getLargo() == 0 ? ConfigSofa.largoPara(n) : p.getLargo();
            double fondo = p.getFondo() == null || p.getFondo() == 0 ? 92 : p.getFondo();
            ConfigSofa.Chaise ch = f == Forma.SECCIONAL_L || f == Forma.SECCIONAL_U ? ConfigSofa.Chaise.DERECHA : ConfigSofa.Chaise.NINGUNA;
            if (ch != ConfigSofa.Chaise.NINGUNA) fondo = 92;
            if (f == Forma.SECCIONAL_U) {
                n = 3;
                l = 300;
            }
            ConfigSofa.Brazo brazo = switch (f) {
                case CHESTERFIELD, SILLON -> ConfigSofa.Brazo.REDONDEADO;
                case ESCANDINAVO -> ConfigSofa.Brazo.DELGADO;
                default -> ConfigSofa.Brazo.RECTO;
            };
            ConfigSofa.Patas patas = f == Forma.SECCIONAL_L ? ConfigSofa.Patas.METAL_NEGRO
                    : f == Forma.PUFF || f == Forma.RECLINABLE ? ConfigSofa.Patas.ZOCALO : ConfigSofa.Patas.MADERA;
            return new Opciones(f, n, l, fondo, p.getAlto() == null || p.getAlto() == 0 ? 85 : p.getAlto(), ch, brazo, patas,
                    f == Forma.CHESTERFIELD ? "#3B2A20" : "#C69A6D", color, tipo, f == Forma.MODERNO || f == Forma.ESCANDINAVO ? 2 : 0,
                    "#C9972E", TipoTela.CHENILLE);
        }

        private static TipoTela tipoPorNombre(String material) {
            if (material == null) return TipoTela.CHENILLE;
            for (TipoTela t : TipoTela.values()) if (t.nombre().startsWith(material.split(" ")[0])) return t;
            return TipoTela.CHENILLE;
        }
    }

    private final Map<String, PhongMaterial> materiales = new HashMap<>();

    public Group build(Opciones o) {
        Group g = new Group();
        if (o.forma() == Forma.PUFF) {
            puff(g, o);
            return g;
        }
        PhongMaterial tela = tela(o.colorTela(), o.tipoTela());
        PhongMaterial telaOscura = tela(Color.web(o.colorTela()).deriveColor(0, 1, 0.9, 1).toString(), o.tipoTela());
        boolean ches = o.forma() == Forma.CHESTERFIELD;
        boolean esc = o.forma() == Forma.ESCANDINAVO;
        boolean rec = o.forma() == Forma.RECLINABLE;
        int n = Math.max(1, o.cuerpos());
        double L = o.largo(), F = o.fondo(), A = o.alto();
        double hPatas = switch (o.patas()) {
            case ZOCALO -> 7;
            case METAL_NEGRO, METAL_DORADO -> 16;
            default -> esc ? 18 : 13;
        };
        double armW = switch (o.brazo()) {
            case DELGADO -> 9;
            case REDONDEADO -> ches ? 22 : 18;
            default -> rec ? 22 : 17;
        };
        double hBase = esc ? 14 : 18;
        double yAsiento = hPatas + hBase;
        double hCojin = esc ? 11 : 14;
        double fondoRespaldo = ches ? 22 : 20;
        double altoBrazo = ches ? A - 6 : switch (o.brazo()) {
            case DELGADO -> A * 0.62;
            default -> A * 0.7;
        };
        boolean chaise = o.chaise() != null && o.chaise() != ConfigSofa.Chaise.NINGUNA;
        boolean chaiseDerecha = o.chaise() == ConfigSofa.Chaise.DERECHA;
        double extra = chaise ? 65 : 0;

        // Base (armazon tapizado)
        double interior = L - 2 * armW;
        add(g, caja(L, hBase, F, 3, tela), 0, hPatas + hBase / 2, 0);
        double anchoCojin = interior / n;
        double zCojin = -fondoRespaldo / 2;
        double profCojin = F - fondoRespaldo - 2;
        for (int i = 0; i < n; i++) {
            double x = -interior / 2 + anchoCojin * (i + 0.5);
            boolean esChaise = chaise && ((chaiseDerecha && i == n - 1) || (!chaiseDerecha && i == 0));
            double prof = profCojin + (esChaise ? extra : 0);
            double z = zCojin - (esChaise ? extra / 2 : 0);
            // asiento
            add(g, caja(anchoCojin - 1, hCojin, prof, 5, tela), x, yAsiento + hCojin / 2, z);
            // respaldo (inclinado 8°)
            double hResp = A - yAsiento - hCojin - (ches ? 0 : 3);
            if (!ches) {
                Node r = add(g, caja(anchoCojin - 1, hResp, 16, 6, tela), x, yAsiento + hCojin + hResp / 2, F / 2 - fondoRespaldo - 4);
                r.getTransforms().add(new Rotate(-8, 0, 0, 0, Rotate.X_AXIS));
            }
        }
        // Chaise: base extendida bajo el ultimo cojin
        if (chaise) {
            double xCh = (chaiseDerecha ? 1 : -1) * (interior / 2 - anchoCojin / 2 + armW / 2);
            add(g, caja(anchoCojin + armW, hBase, extra + 4, 3, tela), xCh, hPatas + hBase / 2, -F / 2 - extra / 2 + 2);
        }
        // Respaldo (armazon trasero)
        double hRespaldoFrame = A - hPatas - (ches ? 0 : 6);
        add(g, caja(L, hRespaldoFrame, fondoRespaldo, ches ? 7 : 4, ches ? telaOscura : tela), 0, hPatas + hRespaldoFrame / 2, F / 2 - fondoRespaldo / 2);
        if (ches) capitone(g, L - 2 * armW, hPatas + hBase + hCojin, A - 6, F / 2 - fondoRespaldo, telaOscura);

        // Brazos
        for (int lado = -1; lado <= 1; lado += 2) {
            boolean sinBrazo = chaise && ((chaiseDerecha && lado == 1) || (!chaiseDerecha && lado == -1));
            double x = lado * (L / 2 - armW / 2);
            if (sinBrazo) {
                add(g, caja(armW, hBase + 6, F, 3, tela), x, hPatas + (hBase + 6) / 2, 0);
                continue;
            }
            double alto = altoBrazo - hPatas;
            double radio = switch (o.brazo()) {
                case REDONDEADO -> Math.min(armW / 2 - 0.5, 9);
                case DELGADO -> 3;
                default -> 4;
            };
            add(g, caja(armW, alto, F, radio, tela), x, hPatas + alto / 2, 0);
            if (ches || o.brazo() == ConfigSofa.Brazo.REDONDEADO) {
                // Brazo enrollado: cilindro a lo largo del fondo
                Cylinder rollo = new Cylinder(armW / 2 + (ches ? 3 : 1), F - 2, 28);
                rollo.setMaterial(tela);
                rollo.getTransforms().addAll(new javafx.scene.transform.Translate(x + lado * (ches ? 2 : 0), -(altoBrazo - 1), 0),
                        new Rotate(90, Rotate.X_AXIS));
                g.getChildren().add(rollo);
            }
        }

        // Patas
        patas(g, o, L, F + (chaise ? 0 : 0), hPatas, chaise, chaiseDerecha, anchoCojin + armW, extra);

        // Cojines decorativos inclinados 12°
        if (o.cojines() > 0) {
            PhongMaterial mc = tela(o.colorCojines(), o.tipoCojines());
            int c = o.cojines();
            double tam = Math.min(44, anchoCojin * 0.75);
            for (int i = 0; i < c; i++) {
                int lado = i % 2 == 0 ? -1 : 1;
                int fila = i / 2;
                double x = lado * (interior / 2 - tam / 2 - 4 - fila * (tam * 0.8));
                Node cj = add(g, caja(tam, tam, 12, 5, mc), x, yAsiento + hCojin + tam / 2 - 2, F / 2 - fondoRespaldo - 16 - fila * 3);
                cj.getTransforms().addAll(new Rotate(-12, Rotate.X_AXIS), new Rotate(lado * (6 + fila * 6), Rotate.Z_AXIS));
            }
        }
        return g;
    }

    private void puff(Group g, Opciones o) {
        double L = o.largo(), A = o.alto();
        PhongMaterial tela = tela(o.colorTela(), o.tipoTela());
        add(g, caja(L, A - 6, L, 6, tela), 0, 6 + (A - 6) / 2, 0);
        add(g, caja(L - 6, 6, L - 6, 1, color("#1F2937", 10)), 0, 3, 0);
    }

    private void capitone(Group g, double ancho, double y0, double y1, double z, PhongMaterial base) {
        PhongMaterial boton = color(Color.web(base.getDiffuseColor().toString()).deriveColor(0, 1, 0.6, 1).toString(), 20);
        int cols = Math.max(4, (int) (ancho / 22));
        int filas = 3;
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c <= cols; c++) {
                double x = -ancho / 2 + ancho * c / cols + (f % 2 == 0 ? 0 : ancho / cols / 2);
                if (x > ancho / 2) continue;
                double y = y0 + 8 + (y1 - y0 - 16) * f / (filas - 1.0);
                Sphere s = new Sphere(1.4, 8);
                s.setMaterial(boton);
                s.setTranslateX(x);
                s.setTranslateY(-y);
                s.setTranslateZ(z - 0.4);
                g.getChildren().add(s);
            }
        }
    }

    private void patas(Group g, Opciones o, double L, double F, double h, boolean chaise, boolean derecha, double anchoChaise, double extra) {
        if (o.patas() == ConfigSofa.Patas.ZOCALO) {
            add(g, caja(L - 8, h, F - 8, 1, color("#1F2937", 12)), 0, h / 2, 0);
            if (chaise) add(g, caja(anchoChaise - 8, h, extra, 1, color("#1F2937", 12)), (derecha ? 1 : -1) * (L / 2 - anchoChaise / 2), h / 2, -F / 2 - extra / 2 + 4);
            return;
        }
        int cuantas = L <= 160 ? 4 : 6;
        double[] xs = cuantas == 4 ? new double[]{-L / 2 + 7, L / 2 - 7} : new double[]{-L / 2 + 7, 0, L / 2 - 7};
        double[] zs = {-F / 2 + 7, F / 2 - 7};
        PhongMaterial m = switch (o.patas()) {
            case METAL_NEGRO -> color("#1F1F22", 30);
            case METAL_DORADO -> color("#C8A24A", 60);
            default -> color(o.colorPatas() == null ? "#C69A6D" : o.colorPatas(), 14);
        };
        for (double x : xs) for (double z : zs) pata(g, o.patas(), m, x, z, h);
        if (chaise) {
            double x = (derecha ? 1 : -1) * (L / 2 - 7);
            pata(g, o.patas(), m, x, -F / 2 - extra + 7, h);
            pata(g, o.patas(), m, x - (derecha ? 1 : -1) * (anchoChaise - 14), -F / 2 - extra + 7, h);
        }
    }

    private void pata(Group g, ConfigSofa.Patas tipo, PhongMaterial m, double x, double z, double h) {
        Node n;
        if (tipo == ConfigSofa.Patas.MADERA) {
            MeshView mv = new MeshView(Mallas.tronco(1.6, 2.6, h, 18));
            mv.setMaterial(m);
            mv.setCullFace(javafx.scene.shape.CullFace.NONE);
            n = mv;
        } else {
            Cylinder c = new Cylinder(1.1, h, 14);
            c.setMaterial(m);
            n = c;
        }
        n.setTranslateX(x);
        n.setTranslateY(-h / 2);
        n.setTranslateZ(z);
        g.getChildren().add(n);
    }

    private Node add(Group g, Node n, double x, double yCentro, double z) {
        n.setTranslateX(x);
        n.setTranslateY(-yCentro);
        n.setTranslateZ(z);
        g.getChildren().add(n);
        return n;
    }

    private MeshView caja(double w, double h, double d, double r, PhongMaterial m) {
        MeshView mv = new MeshView(Mallas.cajaRedondeada(w, h, d, r, 4));
        mv.setMaterial(m);
        mv.setCullFace(javafx.scene.shape.CullFace.NONE);
        return mv;
    }

    public PhongMaterial tela(String hex, TipoTela tipo) {
        String k = "t" + hex + tipo;
        return materiales.computeIfAbsent(k, x -> {
            PhongMaterial m = new PhongMaterial(ColorSwatchColor.parse(hex));
            double brillo = tipo == null ? 8 : tipo.brillo();
            m.setSpecularPower(brillo);
            double sc = tipo == TipoTela.CUERO ? 0.38 : tipo == TipoTela.TERCIOPELO ? 0.22 : 0.06;
            m.setSpecularColor(Color.gray(sc));
            m.setDiffuseMap(Texturas.tejido(tipo));
            return m;
        });
    }

    public PhongMaterial color(String hex, double brillo) {
        return materiales.computeIfAbsent("c" + hex + brillo, x -> {
            PhongMaterial m = new PhongMaterial(ColorSwatchColor.parse(hex));
            m.setSpecularPower(brillo);
            m.setSpecularColor(Color.gray(brillo > 20 ? 0.5 : 0.15));
            return m;
        });
    }

    /** Parseo tolerante de colores (evita depender de componentes de UI). */
    static final class ColorSwatchColor {
        static Color parse(String hex) {
            try {
                return Color.web(hex == null ? "#B9B7B1" : hex);
            } catch (IllegalArgumentException e) {
                return Color.web("#B9B7B1");
            }
        }
    }

    @SuppressWarnings("unused")
    private static Box guia(double w, double h, double d) {
        return new Box(w, h, d);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
