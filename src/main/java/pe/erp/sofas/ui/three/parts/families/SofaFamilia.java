/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SofaFamilia
 * Descripción: Las 13 familias de sofás del catálogo SISTEMA ERP SOFAS.
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
package pe.erp.sofas.ui.three.parts.families;

/**
 * Las 13 familias de sofás del catálogo SISTEMA ERP SOFAS.
 * Cada familia tiene su constructores y reglas de composición.
 */
public enum SofaFamilia {
    /**
     * Sofá Moderno: brazo recto ancho, cojines sueltos, respaldo cojines.
     * Código: SF-001/002/003
     */
    MODERNO("Sofá Moderno", "SF-001", true),

    /**
     * Seccional L con chaise: sofá + chaise extendida a la derecha o izquierda.
     * Código: SF-005
     */
    SECCIONAL_L_CHAISE("Seccional L con chaise", "SF-005", true),

    /**
     * Seccional L con esquinero: lado A + módulo esquina cuadrado + lado B.
     * NUEVO: Módulo de esquina real con respaldos en dos lados.
     * Código: SF-012
     */
    SECCIONAL_L_ESQUINERO("Seccional L con esquinero", "SF-012", false),

    /**
     * Seccional U: lado B + esquinero + lado A + esquinero + lado B.
     * NUEVO: Forma completa de U.
     * Código: SF-011
     */
    SECCIONAL_U("Seccional U", "SF-011", false),

    /**
     * Sofá Modular: constructor libre, pieza a pieza.
     * NUEVO: Permite composiciones personalizadas.
     * Código: SF-013
     */
    MODULAR("Sofá Modular", "SF-013", false),

    /**
     * Chesterfield: brazo enrollado, capitoné con botones, ribete, patas bola.
     * Código: SF-006
     */
    CHESTERFIELD("Chesterfield", "SF-006", true),

    /**
     * Escandinavo: perfil delgado, patas inclinadas, asiento banco corrido.
     * Código: SF-009
     */
    ESCANDINAVO("Escandinavo", "SF-009", true),

    /**
     * Sofá cama: respaldo abatible tipo clic-clac, colchoneta plegable.
     * Código: SF-007
     */
    SOFA_CAMA("Sofá cama", "SF-007", true),

    /**
     * Reclinable: respaldo acolchado alto, reposapiés desplegable por cuerpo.
     * Código: SF-008
     */
    RECLINABLE("Reclinable", "SF-008", true),

    /**
     * Retro / Mid-century: brazos inclinados, respaldo con canales verticales.
     * NUEVO: Estilo retro distintivo.
     * Código: SF-014
     */
    RETRO("Retro / Mid-century", "SF-014", false),

    /**
     * Cloud / Modular profundo: muy profundo (110 cm), cojines gruesos inflados, sin patas visibles.
     * NUEVO: Ultra confortable, aspecto premium.
     * Código: SF-015
     */
    CLOUD("Cloud / Modular profundo", "SF-015", false),

    /**
     * Tuxedo: brazos y respaldo a la misma altura, líneas rectas, capitoné opcional.
     * NUEVO: Diseño minimalista elegante.
     * Código: SF-016
     */
    TUXEDO("Tuxedo", "SF-016", false),

    /**
     * Sillón: variante simple (1 cuerpo). Existen versiones: Moderno, Chesterfield, Escandinavo, Bergère.
     * Código: SF-003
     */
    SILLON("Sillón", "SF-003", true);

    private final String nombre;
    private final String codigoProducto;
    private final boolean yaExistia;  // true = existía en SofaBuilder, false = nueva en Fase 2

    SofaFamilia(String nombre, String codigoProducto, boolean yaExistia) {
        this.nombre = nombre;
        this.codigoProducto = codigoProducto;
        this.yaExistia = yaExistia;
    }

    public String nombre() {
        return nombre;
    }

    public String codigoProducto() {
        return codigoProducto;
    }

    public boolean yaExistia() {
        return yaExistia;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
