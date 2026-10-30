/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Mallas
 * Descripción: Mallas para JavaFX 3D: caja redondeada (caja interior + esfera de radio r en las esquinas, con normales
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

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

/**
 * Mallas para JavaFX 3D: caja redondeada (caja interior + esfera de radio r en las esquinas, con normales
 * suaves) y tronco de cono para las patas. Coordenadas centradas en el origen.
 */
public final class Mallas {

    private Mallas() {
    }

    /** Caja redondeada de w × h × d con radio r; {@code seg} subdivisiones por cuarto de curva. */
    public static TriangleMesh cajaRedondeada(double w, double h, double d, double r, int seg) {
        r = Math.max(0.01, Math.min(r, Math.min(w, Math.min(h, d)) / 2 - 0.01));
        double hx = w / 2, hy = h / 2, hz = d / 2;
        double ix = hx - r, iy = hy - r, iz = hz - r;
        TriangleMesh m = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        // Valores de la grilla de cada cara: de -1 a -0 y de +0 a 1 (el cero duplicado forma la cara plana).
        int n = seg + 1;
        double[] t = new double[2 * n];
        int[] signo = new int[2 * n];
        for (int i = 0; i < n; i++) {
            double a = Math.tan(Math.PI / 4 * (1 - i / (double) seg));
            t[i] = -a;
            signo[i] = -1;
            t[n + i] = Math.tan(Math.PI / 4 * (i / (double) seg));
            signo[n + i] = 1;
        }
        int lado = 2 * n;
        // 6 caras: eje normal, signo
        int[][] caras = {{0, 1}, {0, -1}, {1, 1}, {1, -1}, {2, 1}, {2, -1}};
        int base = 0;
        for (int[] cara : caras) {
            int eje = cara[0], s = cara[1];
            int ea = (eje + 1) % 3, eb = (eje + 2) % 3;
            for (int j = 0; j < lado; j++) {
                for (int i = 0; i < lado; i++) {
                    double[] dir = new double[3];
                    int[] sg = new int[3];
                    dir[eje] = s;
                    sg[eje] = s;
                    dir[ea] = t[i];
                    sg[ea] = signo[i];
                    dir[eb] = t[j];
                    sg[eb] = signo[j];
                    double len = Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
                    double nx = dir[0] / len, ny = dir[1] / len, nz = dir[2] / len;
                    float px = (float) (sg[0] * ix + r * nx);
                    float py = (float) (sg[1] * iy + r * ny);
                    float pz = (float) (sg[2] * iz + r * nz);
                    m.getPoints().addAll(px, py, pz);
                    m.getNormals().addAll((float) nx, (float) ny, (float) nz);
                    // Coordenadas de textura en cm/40 para que el tejido tenga escala real.
                    double[] p = {px, py, pz};
                    m.getTexCoords().addAll((float) (p[ea] / 40.0), (float) (p[eb] / 40.0));
                }
            }
            for (int j = 0; j < lado - 1; j++) {
                for (int i = 0; i < lado - 1; i++) {
                    int a = base + j * lado + i, b = a + 1, c = a + lado, dd = c + 1;
                    // Orientacion segun el signo para que la cara mire hacia afuera
                    boolean invertir = (s > 0) == (eje == 1);
                    if (invertir) {
                        tri(m, a, c, b);
                        tri(m, b, c, dd);
                    } else {
                        tri(m, a, b, c);
                        tri(m, b, dd, c);
                    }
                }
            }
            base += lado * lado;
        }
        return m;
    }

    private static void tri(TriangleMesh m, int a, int b, int c) {
        m.getFaces().addAll(a, a, a, b, b, b, c, c, c);
    }

    /** Tronco de cono vertical (eje Y) de altura h, radio inferior r1 y superior r2. */
    public static TriangleMesh tronco(double r1, double r2, double h, int lados) {
        TriangleMesh m = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        m.getTexCoords().addAll(0, 0);
        double inclinacion = (r1 - r2) / h;
        for (int i = 0; i <= lados; i++) {
            double a = 2 * Math.PI * i / lados;
            double c = Math.cos(a), s = Math.sin(a);
            double nl = Math.sqrt(1 + inclinacion * inclinacion);
            m.getPoints().addAll((float) (r1 * c), (float) (h / 2), (float) (r1 * s));
            m.getNormals().addAll((float) (c / nl), (float) (-inclinacion / nl), (float) (s / nl));
            m.getPoints().addAll((float) (r2 * c), (float) (-h / 2), (float) (r2 * s));
            m.getNormals().addAll((float) (c / nl), (float) (-inclinacion / nl), (float) (s / nl));
        }
        for (int i = 0; i < lados; i++) {
            int a = 2 * i, b = a + 1, c = a + 2, d = a + 3;
            m.getFaces().addAll(a, a, 0, c, c, 0, b, b, 0);
            m.getFaces().addAll(b, b, 0, c, c, 0, d, d, 0);
        }
        // Tapas
        int base = m.getPoints().size() / 3;
        m.getPoints().addAll(0, (float) (-h / 2), 0, 0, (float) (h / 2), 0);
        m.getNormals().addAll(0, -1, 0, 0, 1, 0);
        int arriba = base, abajo = base + 1;
        for (int i = 0; i < lados; i++) {
            int a = 2 * i + 1, c = 2 * i + 3;
            m.getFaces().addAll(arriba, arriba, 0, a, arriba, 0, c, arriba, 0);
            int b = 2 * i, d = 2 * i + 2;
            m.getFaces().addAll(abajo, abajo, 0, d, abajo, 0, b, abajo, 0);
        }
        return m;
    }

    /** Plano horizontal (XZ) de w × d con coordenadas de textura 0..1. */
    public static TriangleMesh plano(double w, double d) {
        TriangleMesh m = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        float x = (float) (w / 2), z = (float) (d / 2);
        m.getPoints().addAll(-x, 0, -z, x, 0, -z, -x, 0, z, x, 0, z);
        m.getNormals().addAll(0, -1, 0);
        m.getTexCoords().addAll(0, 0, 1, 0, 0, 1, 1, 1);
        m.getFaces().addAll(0, 0, 0, 2, 0, 2, 1, 0, 1);
        m.getFaces().addAll(1, 0, 1, 2, 0, 2, 3, 0, 3);
        return m;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
