/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Prefs
 * Descripción: Preferencias locales del usuario (java.util.prefs).
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
package pe.erp.sofas.core;

import java.util.prefs.Preferences;

/** Preferencias locales del usuario (java.util.prefs). */
public final class Prefs {

    public static final String TEMA_OSCURO = "temaOscuro";
    public static final String DENSIDAD = "densidad";
    public static final String MENU_CONTRAIDO = "menuContraido";
    public static final String RECORDAR = "recordarSesion";
    public static final String ULTIMO_USUARIO = "ultimoUsuario";
    public static final String VISTA_MODELOS = "vistaModelos";
    public static final String VISTA_TABLERO = "vistaTablero";

    private static final Preferences NODO = Preferences.userRoot().node("pe/innovasur/sofas");

    private Prefs() {
    }

    public static boolean bool(String clave, boolean defecto) {
        return NODO.getBoolean(clave, defecto);
    }

    public static void setBool(String clave, boolean valor) {
        NODO.putBoolean(clave, valor);
    }

    public static String str(String clave, String defecto) {
        return NODO.get(clave, defecto);
    }

    public static void setStr(String clave, String valor) {
        NODO.put(clave, valor);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
