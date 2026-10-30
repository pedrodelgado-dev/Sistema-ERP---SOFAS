/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Modulo
 * Descripción: Modulos del ERP con su seccion del menu, icono, descripcion y vista FXML.
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

import java.util.Arrays;

/** Modulos del ERP con su seccion del menu, icono, descripcion y vista FXML. */
public enum Modulo {
    DASHBOARD("dashboard", "Dashboard", null, "DASHBOARD",
            "Resumen de ventas, producción y pedidos de SISTEMA ERP SOFAS al día de hoy."),
    MODELOS("modelos", "Modelos de sofá", "CATÁLOGO", "WEEKEND",
            "Catálogo de modelos con medidas, precios, costo estándar y stock."),
    TELAS("telas", "Telas y colores", "CATÁLOGO", "PALETTE",
            "Muestrario de telas por tipo, colores, metros en stock y calculadora de tela."),
    CONFIGURADOR("configurador", "Configurador 3D", "CATÁLOGO", "MDOAL_3D_ROTATION",
            "Diseña el sofá del cliente en 3D con precio y plazo en tiempo real."),
    COMPARADOR("comparador", "Comparar sofás", "CATÁLOGO", "VIEW_DAY",
            "Compara hasta 3 configuraciones lado a lado con rotación sincronizada."),
    CONSTRUCTOR("constructor", "Constructor modular", "CATÁLOGO", "BUILD",
            "Arma sofás pieza por pieza desde paleta de módulos."),
    SALA("sala", "Ver en mi sala", null, "MEETING_ROOM",
            "Ubica los sofás en la planta de la sala del cliente y revisa la circulación."),
    PRODUCCION("produccion", "Órdenes de producción", "PRODUCCIÓN", "BUILD",
            "Tablero del taller por etapa con reservas y consumo de materiales."),
    FICHAS("fichas", "Fichas técnicas", "PRODUCCIÓN", "DESCRIPTION",
            "Lista de materiales, mano de obra y costo estándar por versión."),
    MRP("mrp", "Requerimiento de materiales", "PRODUCCIÓN", "FACT_CHECK",
            "Materiales requeridos por las órdenes y pedidos, faltantes y compras en tránsito."),
    CORTE("corte", "Hojas de corte", "PRODUCCIÓN", "CONTENT_CUT",
            "Planos de corte optimizados de espuma, tela, madera y tableros."),
    CONSUMO("consumo", "Consumo y mermas", "PRODUCCIÓN", "ANALYTICS",
            "Consumo real frente al estimado por la ficha, desvíos y sobremerma."),
    PEDIDOS("pedidos", "Pedidos a medida", "COMERCIAL", "ASSIGNMENT",
            "Pedidos estándar y personalizados, adelantos, órdenes y entregas."),
    CARRITO("carrito", "Carrito de compras", "COMERCIAL", "SHOPPING_CART",
            "Carrito con configuraciones guardadas, cálculo de totales y generación de órdenes."),
    VENTAS("ventas", "Ventas (POS)", "COMERCIAL", "POINT_OF_SALE",
            "Punto de venta de la tienda con boleta o factura de prueba."),
    CLIENTES("clientes", "Clientes", "COMERCIAL", "PEOPLE",
            "Cartera de clientes, compras, pedidos y saldos."),
    POSVENTA("posventa", "Retapizado y garantías", "COMERCIAL", "BUILD_CIRCLE",
            "Retapizado de sofás de clientes y reclamos de garantía."),
    INVENTARIO("inventario", "Inventario", "ADMINISTRACIÓN", "ALL_INBOX",
            "Materiales del taller, reservas, kardex y alertas de stock."),
    PROVEEDORES("proveedores", "Proveedores", "ADMINISTRACIÓN", "LOCAL_SHIPPING",
            "Empresas que abastecen madera, espumas, telas e insumos."),
    COMPRAS("compras", "Compras", "ADMINISTRACIÓN", "SHOPPING_CART",
            "Órdenes de compra y recepción en almacén."),
    PERSONAL("personal", "Personal", "ADMINISTRACIÓN", "ENGINEERING",
            "Equipo del taller y la tienda, productividad y pago a destajo."),
    FINANZAS("finanzas", "Finanzas", "ADMINISTRACIÓN", "ACCOUNT_BALANCE_WALLET",
            "Ingresos, egresos, cuentas por cobrar y por pagar y rentabilidad."),
    REPORTES("reportes", "Reportes", "ADMINISTRACIÓN", "INSERT_CHART",
            "Reportes con vista previa y exportación a PDF y Excel."),
    CONFIGURACION("configuracion", "Configuración", "SISTEMA", "SETTINGS",
            "Empresa, usuarios y roles, parámetros de producción y preferencias.");

    private final String clave;
    private final String titulo;
    private final String seccion;
    private final String icono;
    private final String descripcion;

    Modulo(String clave, String titulo, String seccion, String icono, String descripcion) {
        this.clave = clave;
        this.titulo = titulo;
        this.seccion = seccion;
        this.icono = icono;
        this.descripcion = descripcion;
    }

    public String clave() {
        return clave;
    }

    public String titulo() {
        return titulo;
    }

    public String seccion() {
        return seccion;
    }

    public String icono() {
        return icono;
    }

    public String descripcion() {
        return descripcion;
    }

    /** "Ver en mi sala" no aparece en el menu: se abre desde el Configurador. */
    public boolean enMenu() {
        return this != SALA;
    }

    /** Las 5 vistas de Produccion comparten la barra de pestanas. */
    public boolean esProduccion() {
        return this == PRODUCCION || this == FICHAS || this == MRP || this == CORTE || this == CONSUMO;
    }

    public String fxml() {
        return "/fxml/" + clave + ".fxml";
    }

    public static Modulo porClave(String clave) {
        return Arrays.stream(values()).filter(m -> m.clave.equals(clave)).findFirst().orElse(DASHBOARD);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
