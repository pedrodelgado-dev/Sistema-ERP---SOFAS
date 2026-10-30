/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: PipingMesh
 * Descripción: Ribete (piping): tubo decorativo delgado que sigue los bordes de un cojín.
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

import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Ribete (piping): tubo decorativo delgado que sigue los bordes de un cojín.
 * Típicamente usado en sofás elegantes para delinear cojines.
 *
 * Esta implementación crea un tubo cilíndrico simple alrededor del perímetro
 * de una cara de un cojín. El radio del tubo es pequeño (típico 0.4 cm).
 *
 * Para una implementación completa, seguiría un camino Bézier 3D, pero
 * aquí simplificamos a aristas rectas.
 */
public final class PipingMesh {

    /**
     * Crea un tubo de ribete rectangular (alrededor de ancho × profundidad).
     *
     * @param w ancho del rectángulo exterior (cm)
     * @param d profundidad del rectángulo exterior (cm)
     * @param radioTubo radio del tubo cilíndrico (típico 0.3-0.5 cm)
     * @param lados número de segmentos del círculo del tubo (8+ para suavidad)
     * @return TriangleMesh del ribete
     */
    public static TriangleMesh crearRibeteRectangular(double w, double d, double radioTubo, int lados) {
        TriangleMesh malla = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);

        // Cuatro lados del rectángulo: superior, derecho, inferior, izquierdo
        // (aproximación: tubo cilíndrico siguiendo cada lado)

        List<Point3D> puntos = new ArrayList<>();

        // Lado superior (x: -w/2 a w/2, z: -d/2)
        for (double x = -w / 2; x <= w / 2; x += w / 10.0) {
            puntos.add(new Point3D(x, 0, -d / 2));
        }

        // Lado derecho (x: w/2, z: -d/2 a d/2)
        for (double z = -d / 2; z <= d / 2; z += d / 10.0) {
            puntos.add(new Point3D(w / 2, 0, z));
        }

        // Lado inferior (x: w/2 a -w/2, z: d/2)
        for (double x = w / 2; x >= -w / 2; x -= w / 10.0) {
            puntos.add(new Point3D(x, 0, d / 2));
        }

        // Lado izquierdo (x: -w/2, z: d/2 a -d/2)
        for (double z = d / 2; z >= -d / 2; z -= d / 10.0) {
            puntos.add(new Point3D(-w / 2, 0, z));
        }

        // Para cada punto en el camino, crear un círculo de vértices alrededor
        int n = puntos.size();
        for (int i = 0; i < n; i++) {
            Point3D p = puntos.get(i);

            // Dirección del siguiente punto (tangente)
            Point3D siguiente = puntos.get((i + 1) % n);
            Point3D tangente = siguiente.subtract(p).normalize();

            // Normal perpendicular (hacia arriba y perpendicular a tangente)
            Point3D normal = new Point3D(0, 1, 0);  // Simplificado: hacia arriba
            if (Math.abs(tangente.getY()) > 0.9) {
                normal = new Point3D(1, 0, 0);  // Si tangente es casi vertical, usar X
            }

            // Crear círculo de puntos alrededor
            for (int j = 0; j < lados; j++) {
                double angulo = 2 * Math.PI * j / lados;
                double cos = Math.cos(angulo);
                double sin = Math.sin(angulo);

                // Punto en el círculo
                Point3D offset = normal.multiply(radioTubo * cos).add(
                    normal.crossProduct(tangente).multiply(radioTubo * sin)
                );
                Point3D ptubo = p.add(offset);

                malla.getPoints().addAll((float) ptubo.getX(), (float) ptubo.getY(), (float) ptubo.getZ());
                malla.getNormals().addAll((float) offset.getX(), (float) offset.getY(), (float) offset.getZ());
                malla.getTexCoords().addAll(j / (float) lados, i / (float) n);
            }
        }

        // Triángulos conectando círculos adyacentes
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < lados; j++) {
                int a = i * lados + j;
                int b = i * lados + (j + 1) % lados;
                int c = (i + 1) * lados + j;
                int dd = (i + 1) * lados + (j + 1) % lados;

                malla.getFaces().addAll(a, a, a, b, b, b, c, c, c);
                malla.getFaces().addAll(b, b, b, dd, dd, dd, c, c, c);
            }
        }

        return malla;
    }

    /**
     * Crea un ribete con parámetros recomendados por defecto.
     * radioTubo = 0.4 cm, lados = 8
     */
    public static TriangleMesh crearDefecto(double w, double d) {
        return crearRibeteRectangular(w, d, 0.4, 8);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
