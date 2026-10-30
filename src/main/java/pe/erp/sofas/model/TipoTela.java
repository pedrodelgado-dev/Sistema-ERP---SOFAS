/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: TipoTela
 * Descripción: Tipos de tela del muestrario con sus caracteristicas comunes a todos los colores
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
package pe.erp.sofas.model;

/**
 * Tipos de tela del muestrario con sus caracteristicas comunes a todos los colores
 * (precio por metro, recargo al cliente, ciclos Martindale y material de inventario vinculado).
 */
public enum TipoTela {
    CHENILLE("Chenille", 28, 0, 30000, true, false, false, "MP-003", "PR-03", 8),
    LINO("Lino", 34, 8, 20000, false, false, false, "MP-006", "PR-02", 8),
    CUERO("Cuero sintético", 35, 12, 100000, true, true, true, "MP-004", "PR-03", 40),
    TERCIOPELO("Terciopelo", 42, 15, 40000, false, false, false, "MP-007", "PR-02", 25),
    BOUCLE("Bouclé", 48, 20, 35000, false, false, false, "MP-008", "PR-02", 8),
    MICROFIBRA("Microfibra antimascotas", 32, 6, 60000, true, true, true, "MP-005", "PR-03", 8);

    private final String nombre;
    private final double costoMetro;
    private final double recargoPct;
    private final int martindale;
    private final boolean antimanchas;
    private final boolean mascotas;
    private final boolean impermeable;
    private final String materialCodigo;
    private final String proveedorCodigo;
    private final double brillo;

    TipoTela(String nombre, double costoMetro, double recargoPct, int martindale, boolean antimanchas,
             boolean mascotas, boolean impermeable, String materialCodigo, String proveedorCodigo, double brillo) {
        this.nombre = nombre;
        this.costoMetro = costoMetro;
        this.recargoPct = recargoPct;
        this.martindale = martindale;
        this.antimanchas = antimanchas;
        this.mascotas = mascotas;
        this.impermeable = impermeable;
        this.materialCodigo = materialCodigo;
        this.proveedorCodigo = proveedorCodigo;
        this.brillo = brillo;
    }

    public String nombre() {
        return nombre;
    }

    public double costoMetro() {
        return costoMetro;
    }

    /** Recargo al precio de venta del sofa, en %. */
    public double recargoPct() {
        return recargoPct;
    }

    public int martindale() {
        return martindale;
    }

    public boolean antimanchas() {
        return antimanchas;
    }

    public boolean mascotas() {
        return mascotas;
    }

    public boolean impermeable() {
        return impermeable;
    }

    public String materialCodigo() {
        return materialCodigo;
    }

    public String proveedorCodigo() {
        return proveedorCodigo;
    }

    /** specularPower del material 3D (cuero 40, terciopelo 25, resto 8). */
    public double brillo() {
        return brillo;
    }

    /** El terciopelo no se puede girar en el plano de corte por el sentido del pelo. */
    public boolean permiteGiro() {
        return this != TERCIOPELO;
    }

    public static TipoTela porMaterial(String codigoMaterial) {
        for (TipoTela t : values()) {
            if (t.materialCodigo.equals(codigoMaterial)) return t;
        }
        return null;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
