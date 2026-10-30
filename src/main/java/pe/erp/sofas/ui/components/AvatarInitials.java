/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: AvatarInitials
 * Descripción: Avatar circular con iniciales; el color lo define una variante de estilo o un color explicito.
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
package pe.erp.sofas.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/** Avatar circular con iniciales; el color lo define una variante de estilo o un color explicito. */
public class AvatarInitials extends StackPane {

    public AvatarInitials(String nombre, double size, String variante) {
        Label l = new Label(iniciales(nombre));
        l.getStyleClass().add("avatar-text");
        l.setStyle("-fx-font-size: " + Math.round(size * 0.38) + "px;");
        getChildren().add(l);
        getStyleClass().addAll("avatar", "avatar-" + variante);
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
    }

    /** Avatar con color de fondo dado (p. ej. color del area del trabajador). */
    public static AvatarInitials conColor(String nombre, double size, String color) {
        AvatarInitials a = new AvatarInitials(nombre, size, "custom");
        a.setStyle("-fx-background-color: " + color + ";");
        return a;
    }

    public static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] p = nombre.replaceAll("[^\\p{L} ]", " ").trim().split("\\s+");
        if (p.length == 1) return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
