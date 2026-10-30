/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Ico
 * Descripción: Iconos Material Design 2 (variante outlined) por nombre: {@code Ico.of("DASHBOARD", 18)}.
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

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

/** Iconos Material Design 2 (variante outlined) por nombre: {@code Ico.of("DASHBOARD", 18)}. */
public final class Ico {

    private Ico() {
    }

    public static Ikon ikon(String nombre) {
        for (String n : new String[]{nombre, "MDOAL_" + nombre}) {
            try {
                return Material2OutlinedAL.valueOf(n);
            } catch (IllegalArgumentException ignored) {
                // se prueba la otra mitad del alfabeto
            }
            try {
                return Material2OutlinedMZ.valueOf(n);
            } catch (IllegalArgumentException ignored) {
                // siguiente variante
            }
        }
        return Material2OutlinedAL.INFO;
    }

    public static FontIcon of(String nombre) {
        return of(nombre, 18);
    }

    public static FontIcon of(String nombre, int size) {
        FontIcon icon = new FontIcon(ikon(nombre));
        icon.setIconSize(size);
        icon.getStyleClass().add("ico");
        return icon;
    }

    public static FontIcon of(String nombre, int size, String color) {
        FontIcon icon = of(nombre, size);
        icon.setStyle("-fx-icon-color: " + color + ";");
        return icon;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
