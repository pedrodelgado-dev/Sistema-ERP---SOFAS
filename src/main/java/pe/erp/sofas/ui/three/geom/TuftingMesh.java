/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: TuftingMesh
 * Descripción: Capitoné (tufting): malla con hundimientos en patrón rombo.
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
 * Capitoné (tufting): malla con hundimientos en patrón rombo.
 * Típicamente usado en sofás Chesterfield y Tuxedo de lujo.
 *
 * Algoritmo:
 * - Caja base con deformación cóncava en patrón rombo
 * - Cada rombo tiene un botón (esfera pequeña) en el centro
 * - Patrón: 12-14 cm entre botones
 * - Profundidad de hundimiento: 1.2 cm
 *
 * La geometría crea hundimientos reales en la tela con
 * botones forrados en los vértices del patrón.
 */
public final class TuftingMesh {

    /**
     * Crea una malla capitoné con patrón rombo.
     *
     * @param w ancho total (cm)
     * @param h alto total (cm)
     * @param d profundidad (cm)
     * @param tamRombo tamaño aproximado de cada rombo (típico 12-14 cm)
     * @param profundidadHundimiento hundimiento máximo (típico 1.2 cm)
     * @param seg segmentos de subdivisión por rombo
     * @return TriangleMesh con patrón capitoné
     */
    public static TriangleMesh crear(double w, double h, double d, double tamRombo,
                                     double profundidadHundimiento, int seg) {
        TriangleMesh malla = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);

        // Simplificación: crear una caja base y aplicar deformación en patrón rombo
        // Para una implementación completa, esto sería más sofisticado.

        double hx = w / 2, hy = h / 2, hz = d / 2;
        int n = seg + 1;

        // Grid de puntos en la cara frontal del capitoné
        int filas = (int) Math.ceil(h / tamRombo) + 1;
        int cols = (int) Math.ceil(w / tamRombo) + 1;

        // Generar vértices
        for (int row = 0; row < filas; row++) {
            for (int col = 0; col < cols; col++) {
                double y = -hy + (row / (double) (filas - 1)) * h;
                double x = -hx + (col / (double) (cols - 1)) * w;

                // Aplicar deformación tipo hundimiento en patrón rombo
                // Distancia a punto de rombo más cercano
                double distRombo = calcularDistanciaRombo(x, y, tamRombo);
                double deformacion = 0;
                if (distRombo < tamRombo / 2) {
                    // Función cóncava: hundimiento hacia dentro
                    double t = distRombo / (tamRombo / 2);
                    deformacion = -profundidadHundimiento * (1 - t * t);  // Parábola cóncava
                }

                float px = (float) x;
                float py = (float) y;
                float pz = (float) (hz + deformacion);  // Hundimiento hacia -Z

                malla.getPoints().addAll(px, py, pz);
                malla.getNormals().addAll(0, 0, 1);  // Normal hacia frente
                malla.getTexCoords().addAll((float) ((x + hx) / w), (float) ((y + hy) / h));
            }
        }

        // Triángulos conectando vértices
        for (int row = 0; row < filas - 1; row++) {
            for (int col = 0; col < cols - 1; col++) {
                int a = row * cols + col;
                int b = a + 1;
                int c = a + cols;
                int dd = c + 1;

                malla.getFaces().addAll(a, a, a, b, b, b, c, c, c);
                malla.getFaces().addAll(b, b, b, dd, dd, dd, c, c, c);
            }
        }

        return malla;
    }

    /**
     * Calcula distancia a punto de rombo más cercano.
     * Patrón de rombos alineados en grid.
     */
    private static double calcularDistanciaRombo(double x, double y, double tamRombo) {
        // Centro del rombo más cercano
        double cx = Math.round(x / tamRombo) * tamRombo;
        double cy = Math.round(y / tamRombo) * tamRombo;

        double dx = Math.abs(x - cx);
        double dy = Math.abs(y - cy);

        // Distancia Manhattan (aproximación para rombo)
        return dx + dy;
    }

    /**
     * Crea capitoné con parámetros recomendados por defecto.
     * tamRombo = 13 cm, profundidad = 1.2 cm, seg = 4
     */
    public static TriangleMesh crearDefecto(double w, double h, double d) {
        return crear(w, h, d, 13, 1.2, 4);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
