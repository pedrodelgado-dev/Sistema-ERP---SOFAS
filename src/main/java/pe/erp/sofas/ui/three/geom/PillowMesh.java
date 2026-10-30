/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PillowMesh
 * Descripción: Malla de cojín "inflado": una caja donde las caras están abombadas hacia afuera.
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
package pe.erp.sofas.ui.three.geom;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

/**
 * Malla de cojín "inflado": una caja donde las caras están abombadas hacia afuera.
 * Crea un aspecto mullido, como si el cojín estuviera presurizado.
 *
 * Algoritmo: genera una caja redondeada y luego deforma las caras con una función senoidal
 * hacia el centro de cada cara. El resultado es una geometría más orgánica.
 *
 * Parámetros:
 * - w, h, d: ancho, alto, profundidad en cm
 * - r: radio de redondeo en las esquinas
 * - bulge: deformación máxima (típico 0.6-1.2 cm), que tanto se abomba
 * - seg: segmentos de subdivisión por cara (4+ para suavidad)
 */
public final class PillowMesh {

    public static TriangleMesh crear(double w, double h, double d, double r, double bulge, int seg) {
        // Primero generar una caja redondeada base
        TriangleMesh malla = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);

        r = Math.max(0.01, Math.min(r, Math.min(w, Math.min(h, d)) / 2 - 0.01));
        double hx = w / 2, hy = h / 2, hz = d / 2;
        double ix = hx - r, iy = hy - r, iz = hz - r;

        // Grid de puntos para cada cara
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
        int[][] caras = {{0, 1}, {0, -1}, {1, 1}, {1, -1}, {2, 1}, {2, -1}};
        int base = 0;

        for (int[] cara : caras) {
            int eje = cara[0], s = cara[1];
            int ea = (eje + 1) % 3, eb = (eje + 2) % 3;

            for (int j = 0; j < lado; j++) {
                for (int i = 0; i < lado; i++) {
                    // Posición base (caja redondeada)
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

                    // DEFORMACIÓN: Abombamiento senoidal hacia el centro de la cara
                    // Calculamos distancia relativa en esta cara (0 a 1 desde borde a centro)
                    double u = Math.abs(t[i]) / Math.abs(t[0]);  // normalizado 0..1
                    double v = Math.abs(t[j]) / Math.abs(t[0]);  // normalizado 0..1
                    double deformacion = bulge * Math.sin(Math.PI * u) * Math.sin(Math.PI * v);

                    // Aplicar deformación en dirección de la normal
                    px += (float) (deformacion * nx);
                    py += (float) (deformacion * ny);
                    pz += (float) (deformacion * nz);

                    malla.getPoints().addAll(px, py, pz);
                    malla.getNormals().addAll((float) nx, (float) ny, (float) nz);
                    malla.getTexCoords().addAll((float) (px / 40.0), (float) (pz / 40.0));
                }
            }

            // Triángulos
            for (int j = 0; j < lado - 1; j++) {
                for (int i = 0; i < lado - 1; i++) {
                    int a = base + j * lado + i, b = a + 1, c = a + lado, dd = c + 1;
                    boolean invertir = (s > 0) == (eje == 1);
                    if (invertir) {
                        tri(malla, a, c, b);
                        tri(malla, b, c, dd);
                    } else {
                        tri(malla, a, b, c);
                        tri(malla, b, dd, c);
                    }
                }
            }
            base += lado * lado;
        }

        return malla;
    }

    private static void tri(TriangleMesh m, int a, int b, int c) {
        m.getFaces().addAll(a, a, a, b, b, b, c, c, c);
    }

    /**
     * Crea un PillowMesh con parámetros recomendados por defecto.
     * bulge = 0.8 cm, seg = 6 (buena relación calidad/rendimiento)
     */
    public static TriangleMesh crearDefecto(double w, double h, double d, double r) {
        return crear(w, h, d, r, 0.8, 6);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
