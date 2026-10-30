/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SofaPartAssembler
 * Descripción: Ensamblador de partes modulares de sofá.
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
package pe.erp.sofas.ui.three.parts;

import javafx.scene.Group;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Ensamblador de partes modulares de sofá.
 * Toma una lista de SofaPart, las construye y las posiciona en el espacio
 * encadenando sus anclajes para evitar huecos o solapes.
 *
 * Responsabilidades:
 * - Iterar sobre las partes en orden
 * - Llamar a build() en cada una
 * - Usar anclajes para determinar posición relativa
 * - Retornar un Group con todas las partes posicionadas
 * - Calcular dimensiones totales del sofá resultante
 */
public class SofaPartAssembler {

    private final List<SofaPart> partes = new ArrayList<>();
    private PartLayout layout = PartLayout.LINEAL;

    public SofaPartAssembler() {
    }

    public SofaPartAssembler conLayout(PartLayout layout) {
        this.layout = layout;
        return this;
    }

    public SofaPartAssembler agregar(SofaPart parte) {
        this.partes.add(parte);
        return this;
    }

    public SofaPartAssembler agregarTodas(List<SofaPart> partes) {
        this.partes.addAll(partes);
        return this;
    }

    /**
     * Ensambla todas las partes agregadas en un Group posicionado correctamente.
     * Retorna un record con el Group y sus dimensiones totales.
     */
    public Ensamblado ensamblar() {
        if (partes.isEmpty()) {
            return new Ensamblado(new Group(), new SofaPart.PartDimensions(0, 0, 0), 0, 0, 0);
        }

        Group resultado = new Group();
        double posX = 0;  // Posición actual en X (ancho)
        double maxZ = 0;  // Máximo fondo de todas las partes
        double maxY = 0;  // Máximo alto de todas las partes

        for (SofaPart parte : partes) {
            Group geom = parte.build();
            SofaPart.PartDimensions dim = parte.getDimensions();

            // Posicionar la parte en X (alinear a la izquierda de donde empieza)
            geom.setTranslateX(posX + dim.ancho() / 2);
            // Centrar en Y (el piso es Y=0, el centro es Y=dim.alto()/2)
            geom.setTranslateY(-dim.alto() / 2);
            // No mover en Z (alineación frontal)
            geom.setTranslateZ(0);

            resultado.getChildren().add(geom);

            // Actualizar posición para la siguiente parte
            posX += dim.ancho();
            maxZ = Math.max(maxZ, dim.fondo());
            maxY = Math.max(maxY, dim.alto());
        }

        double largoTotal = posX;
        double fondoTotal = maxZ;
        double altoTotal = maxY;

        return new Ensamblado(resultado, new SofaPart.PartDimensions(largoTotal, fondoTotal, altoTotal),
                largoTotal, fondoTotal, altoTotal);
    }

    /**
     * Resultado del ensamblado: el Group y sus dimensiones.
     */
    public record Ensamblado(
        Group grupo,
        SofaPart.PartDimensions dimensiones,
        double largoTotal,
        double fondoTotal,
        double altoTotal
    ) {}

    public String resumen() {
        return "Ensamblador con " + partes.size() + " partes, layout " + layout;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
