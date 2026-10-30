/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: SeedData
 * Descripción: Datos del prototipo, coherentes entre modulos. Solo se cargan si la base esta vacia.
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
package pe.erp.sofas.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClock;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.service.calculo.Explosion;
import pe.erp.sofas.service.calculo.Formula;
import pe.erp.sofas.service.calculo.Medidas;
import pe.erp.sofas.service.calculo.Opciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Datos del prototipo, coherentes entre modulos. Solo se cargan si la base esta vacia.
 * <ul>
 *   <li>21 colores de tela que suman 277 m; el stock de cada material de tela es la suma de sus colores.</li>
 *   <li>OP-1019 a OP-1030: 10 ordenes en etapas productivas con 17 sofas; tres con reserva parcial
 *       (triplay, fibra y barniz).</li>
 *   <li>Ventas de octubre por S/ 86,420 y 34 sofas; 9 pedidos a medida pendientes.</li>
 * </ul>
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SeedData implements ApplicationRunner {

    private static final LocalDate HOY = AppClock.HOY;

    private final EmpresaRepository empresaRepository;
    private final ParametroRepository parametroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;
    private final ProveedorRepository proveedorRepository;
    private final MaterialRepository materialRepository;
    private final TelaRepository telaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final FichaTecnicaRepository fichaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final OrdenProduccionRepository opRepository;
    private final ReservaRepository reservaRepository;
    private final ConsumoRealRepository consumoRepository;
    private final VentaRepository ventaRepository;
    private final OrdenCompraRepository compraRepository;
    private final RetapizadoRepository retapizadoRepository;
    private final ReclamoGarantiaRepository reclamoRepository;
    private final GastoRepository gastoRepository;
    private final ResumenMensualRepository resumenRepository;
    private final CatalogoService catalogo;
    private final FichaService fichaService;
    private final ProduccionService produccionService;
    private final RetapizadoService retapizadoService;

    @Value("${app.seeder.enabled:true}")
    private boolean habilitado;

    private final Map<String, Proveedor> prov = new HashMap<>();
    private final Map<String, Material> mat = new HashMap<>();
    private final Map<String, Tela> tel = new HashMap<>();
    private final Map<String, Producto> prod = new HashMap<>();
    private final Map<String, Cliente> cli = new HashMap<>();
    private final Map<String, Empleado> emp = new HashMap<>();
    private final Map<String, OrdenProduccion> ops = new LinkedHashMap<>();
    /** Movimientos fisicos posteriores al saldo inicial (para calcularlo). */
    private final List<Movimiento> movimientos = new ArrayList<>();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!habilitado || productoRepository.count() > 0) return;
        log.info("Cargando datos semilla de SISTEMA ERP SOFAS Sofás…");
        empresa();
        usuarios();
        proveedores();
        materiales();
        telas();
        productos();
        fichas();
        clientes();
        empleados();
        pedidos();
        ordenes();
        retapizados();
        reclamos();
        consumos();
        compras();
        ventas();
        finanzas();
        stocksFinales();
        log.info("Datos semilla cargados.");
    }

    // ================================================================ empresa y usuarios

    private void empresa() {
        empresaRepository.save(Empresa.builder().id(1L).razonSocial("SISTEMA ERP SOFAS S.A.C.").nombreComercial("SISTEMA ERP SOFAS Sofás")
                .ruc("20601458732").direccion("Jr. Mariano Núñez 418, Juliaca").ciudad("Juliaca, San Román, Puno")
                .telefono("051 32 1478 · 951 204 118").email("ventas@innovasur.pe").web("www.innovasur.pe").build());
        ParametroService.MERMAS.forEach((k, v) -> param("merma." + k, v));
        for (EtapaMO e : EtapaMO.values()) param("costoHora." + e.name(), e.costoHora());
        param(ParametroService.INDIRECTOS, 6);
        param(ParametroService.MARGEN_OBJETIVO, 35);
        ParametroService.DESTAJO.forEach((k, v) -> param("destajo." + k.name(), v));
    }

    private void param(String k, double v) {
        parametroRepository.save(new Parametro(k, String.valueOf(v)));
    }

    private void usuarios() {
        String hash = AuthService.hash("innova2026");
        usuario("cmendoza", "Carlos Mendoza", "Gerente General", Rol.GERENTE, hash);
        usuario("mcahuana", "Milagros Cahuana Ponce", "Administradora", Rol.ADMINISTRADOR, hash);
        usuario("rvilca", "Roxana Vilca Mendoza", "Vendedora", Rol.VENDEDOR, hash);
        usuario("rquispe", "Rubén Quispe Flores", "Supervisor de taller", Rol.SUPERVISOR, hash);
        usuario("fchoque", "Fredy Choque Mamani", "Carpintero", Rol.CARPINTERO, hash);
        usuario("yccori", "Yolanda Ccori Huamán", "Costurera", Rol.COSTURERA, hash);
        usuario("emamani", "Edwin Mamani Ticona", "Tapicero", Rol.TAPICERO, hash);
        usuario("larocutipa", "Luis Arocutipa Ramos", "Almacenero", Rol.ALMACENERO, hash);

        Map<Rol, Set<String>> ver = new EnumMap<>(Rol.class);
        Map<Rol, Set<String>> editar = new EnumMap<>(Rol.class);
        Set<String> todos = Set.of("dashboard", "modelos", "telas", "configurador", "produccion", "fichas", "mrp", "corte", "consumo",
                "pedidos", "ventas", "clientes", "posventa", "inventario", "proveedores", "compras", "personal", "finanzas", "reportes", "configuracion");
        ver.put(Rol.ADMINISTRADOR, todos);
        editar.put(Rol.ADMINISTRADOR, todos);
        ver.put(Rol.VENDEDOR, Set.of("dashboard", "modelos", "telas", "configurador", "pedidos", "ventas", "clientes", "posventa"));
        editar.put(Rol.VENDEDOR, Set.of("configurador", "pedidos", "ventas", "clientes", "posventa"));
        ver.put(Rol.SUPERVISOR, Set.of("dashboard", "modelos", "telas", "configurador", "produccion", "fichas", "mrp", "corte", "consumo", "pedidos", "posventa", "inventario", "compras", "personal", "reportes"));
        editar.put(Rol.SUPERVISOR, Set.of("produccion", "fichas", "mrp", "corte", "consumo", "posventa", "inventario"));
        Set<String> taller = Set.of("dashboard", "produccion", "fichas", "corte");
        for (Rol r : List.of(Rol.CARPINTERO, Rol.COSTURERA, Rol.TAPICERO)) {
            ver.put(r, taller);
            editar.put(r, Set.of("produccion", "corte"));
        }
        ver.put(Rol.ALMACENERO, Set.of("dashboard", "telas", "inventario", "mrp", "compras", "proveedores", "produccion"));
        editar.put(Rol.ALMACENERO, Set.of("telas", "inventario", "compras"));
        for (Rol r : Rol.values()) {
            if (r == Rol.GERENTE) continue;
            for (String m : todos) {
                permisoRepository.save(Permiso.builder().rol(r).modulo(m)
                        .ver(ver.getOrDefault(r, Set.of()).contains(m)).editar(editar.getOrDefault(r, Set.of()).contains(m)).build());
            }
        }
    }

    private void usuario(String u, String nombre, String cargo, Rol rol, String hash) {
        usuarioRepository.save(Usuario.builder().username(u).nombre(nombre).cargo(cargo).rol(rol)
                .email(u + "@innovasur.pe").passwordHash(hash).activo(true).build());
    }

    // ================================================================ proveedores y materiales

    private void proveedores() {
        p("PR-01", "Maderera Altiplano E.I.R.L.", "20448123671", "Wilfredo Mamani", "951 332 410", "Av. Huancané 1820, Juliaca", "madera, triplay, patas de madera", "Crédito 15 días", "48250");
        p("PR-02", "Telas Andinas Import S.A.C.", "20512378945", "Patricia Rojas", "054 28 4471", "Calle Mercaderes 312, Arequipa", "lino, terciopelo, bouclé", "Crédito 30 días", "36420");
        p("PR-03", "Textiles Titicaca S.R.L.", "20406654128", "Jorge Quispe", "951 778 205", "Jr. Lima 640, Puno", "chenille, cuero sintético, microfibra, guata, notex, hilo, cierres, botones", "Crédito 30 días", "61880");
        p("PR-04", "Espumas Andinas S.A.C.", "20455987613", "Rocío Cutipa", "951 600 128", "Parque Industrial Taparachi Mz C, Juliaca", "espumas, cincha, fibra siliconada", "Contado", "44310");
        p("PR-05", "Ferretería Industrial Puno", "10418827364", "Hernán Apaza", "951 451 087", "Jr. Moquegua 210, Juliaca", "grapas, clavos, patas metálicas, embalaje", "Contado", "12890");
        p("PR-06", "Pinturas y Barnices del Sur", "20447719852", "Elena Choquehuanca", "951 239 664", "Av. El Sol 455, Puno", "barniz, tinte, cola PVA, pegamento de contacto", "Crédito 15 días", "9870");
        p("PR-07", "Mecanismos del Sur S.A.C.", "20539912455", "Raúl Paredes", "054 43 9902", "Av. Parra 287, Arequipa", "mecanismos reclinables y de sofá cama, colchonetas", "Crédito 30 días", "15620");
    }

    private void p(String codigo, String rs, String ruc, String contacto, String tel, String dir, String rubro, String cond, String total) {
        prov.put(codigo, proveedorRepository.save(Proveedor.builder().codigo(codigo).razonSocial(rs).ruc(ruc).contacto(contacto)
                .telefono(tel).email(contacto.split(" ")[0].toLowerCase(Locale.ROOT) + "@" + rs.split(" ")[0].toLowerCase(Locale.ROOT) + ".pe")
                .direccion(dir).rubro(rubro).condicionPago(cond).activo(true).totalComprado(new BigDecimal(total)).build()));
    }

    /** Disponible deseado (stock − reservado) de cada material al cierre de la semilla. */
    private final Map<String, Double> disponibleFinal = new LinkedHashMap<>();

    private void materiales() {
        m("MP-001", "Madera tornillo cepillado", "Madera", "pt", 4.20, 400, "Almacén A · Rack 1", "PR-01", 1350);
        m("MP-002", "Triplay 4 mm (244 × 122)", "Tableros", "plancha", 38, 10, "Almacén A · Rack 2", "PR-01", 14);
        m("MP-003", "Tela chenille", "Tapicería", "m", TipoTela.CHENILLE.costoMetro(), 0, "Almacén B · Telas", "PR-03", -1);
        m("MP-004", "Tela cuero sintético", "Tapicería", "m", TipoTela.CUERO.costoMetro(), 0, "Almacén B · Telas", "PR-03", -1);
        m("MP-005", "Tela microfibra antimascotas", "Tapicería", "m", TipoTela.MICROFIBRA.costoMetro(), 0, "Almacén B · Telas", "PR-03", -1);
        m("MP-006", "Tela lino", "Tapicería", "m", TipoTela.LINO.costoMetro(), 0, "Almacén B · Telas", "PR-02", -1);
        m("MP-007", "Tela terciopelo", "Tapicería", "m", TipoTela.TERCIOPELO.costoMetro(), 0, "Almacén B · Telas", "PR-02", -1);
        m("MP-008", "Tela bouclé", "Tapicería", "m", TipoTela.BOUCLE.costoMetro(), 0, "Almacén B · Telas", "PR-02", -1);
        m("MP-009", "Cincha elástica 2\"", "Tapicería", "m", 2.0, 150, "Almacén B · Estante 3", "PR-04", 260);
        m("MP-010", "Resorte zigzag", "Tapicería", "unidad", 3.5, 60, "Almacén B · Estante 3", "PR-04", 140);
        m("MP-011", "Guata 150 g", "Tapicería", "m", 4.5, 60, "Almacén B · Estante 2", "PR-03", 120);
        m("MP-012", "Notex", "Tapicería", "m", 2.2, 40, "Almacén B · Estante 2", "PR-03", 95);
        m("MP-013", "Fibra siliconada", "Tapicería", "kg", 14, 10, "Almacén B · Estante 4", "PR-04", 1.5);
        m("MP-014", "Cierre 60 cm", "Tapicería", "unidad", 1.8, 40, "Almacén B · Estante 4", "PR-03", 110);
        m("MP-015", "Hilo de tapicería", "Tapicería", "unidad", 6, 12, "Almacén B · Estante 4", "PR-03", 34);
        m("MP-016", "Botón forrado", "Tapicería", "unidad", 0.6, 100, "Almacén B · Estante 4", "PR-03", 260);
        m("MP-017", "Espuma D18 2\"", "Espuma", "plancha", 45, 8, "Almacén C · Espumas", "PR-04", 16);
        m("MP-018", "Espuma D18 4\"", "Espuma", "plancha", 85, 8, "Almacén C · Espumas", "PR-04", 14);
        m("MP-019", "Espuma D23 5\"", "Espuma", "plancha", 120, 8, "Almacén C · Espumas", "PR-04", 15);
        m("MP-020", "Espuma D30 5\"", "Espuma", "plancha", 155, 4, "Almacén C · Espumas", "PR-04", 7);
        m("MP-021", "Pata de madera cónica", "Patas", "unidad", 9, 40, "Almacén A · Rack 4", "PR-01", 86);
        m("MP-022", "Pata metálica negra", "Patas", "unidad", 23, 16, "Almacén A · Rack 4", "PR-05", 30);
        m("MP-023", "Pata metálica dorada", "Patas", "unidad", 29, 12, "Almacén A · Rack 4", "PR-05", 18);
        m("MP-024", "Cola PVA", "Insumos", "galón", 45, 3, "Almacén A · Químicos", "PR-06", 6);
        m("MP-025", "Pegamento de contacto", "Insumos", "galón", 55, 3, "Almacén A · Químicos", "PR-06", 5.5);
        m("MP-026", "Grapas 80/10", "Insumos", "caja", 18, 6, "Almacén B · Estante 1", "PR-05", 14);
        m("MP-027", "Clavos 2\"", "Insumos", "kg", 8, 5, "Almacén A · Rack 3", "PR-05", 11);
        m("MP-028", "Tinte para madera", "Insumos", "galón", 60, 2, "Almacén A · Químicos", "PR-06", 3.4);
        m("MP-029", "Barniz para patas", "Acabados", "galón", 70, 2, "Almacén A · Químicos", "PR-06", 0);
        m("MP-030", "Mecanismo reclinable manual", "Mecanismos", "unidad", 180, 3, "Almacén C · Mecanismos", "PR-07", 6);
        m("MP-031", "Mecanismo sofá cama plegable", "Mecanismos", "unidad", 320, 2, "Almacén C · Mecanismos", "PR-07", 3);
        m("MP-032", "Colchoneta 12 cm", "Mecanismos", "unidad", 140, 2, "Almacén C · Mecanismos", "PR-07", 3);
        m("MP-033", "Embalaje (stretch + esquineros)", "Embalaje", "unidad", 25, 10, "Almacén A · Despacho", "PR-05", 24);
        Material visco = m("MP-034", "Espuma viscoelástica 5\"", "Espuma", "plancha", 260, 0, "Bajo pedido", "PR-04", 0);
        Material zocalo = m("MP-035", "Zócalo oculto de madera", "Patas", "juego", 85, 0, "Bajo pedido", "PR-01", 0);
        Material motor = m("MP-036", "Mecanismo reclinable eléctrico", "Mecanismos", "unidad", 520, 0, "Bajo pedido", "PR-07", 0);
        for (Material b : List.of(visco, zocalo, motor)) {
            b.setBajoPedido(true);
            materialRepository.save(b);
        }
    }

    private Material m(String codigo, String nombre, String cat, String unidad, double costo, double minimo, String ubic,
                       String proveedor, double disponible) {
        Material x = materialRepository.save(Material.builder().codigo(codigo).nombre(nombre).categoria(cat).unidad(unidad)
                .costo(costo).minimo(minimo).ubicacion(ubic).proveedor(prov.get(proveedor)).stock(0d).reservado(0d).build());
        mat.put(codigo, x);
        if (disponible >= 0) disponibleFinal.put(codigo, disponible);
        return x;
    }

    // ================================================================ telas

    private void telas() {
        Object[][] t = {
                {TipoTela.CHENILLE, "Gris perla", "#B9B7B1", 14}, {TipoTela.CHENILLE, "Grafito", "#4A4D52", 27},
                {TipoTela.CHENILLE, "Arena", "#CDB89A", 11}, {TipoTela.CHENILLE, "Azul petróleo", "#1F4E5F", 26},
                {TipoTela.CHENILLE, "Verde oliva", "#6B7045", 9}, {TipoTela.CHENILLE, "Terracota", "#B5603C", 9},
                {TipoTela.CHENILLE, "Mostaza", "#C9972E", 6}, {TipoTela.CHENILLE, "Marfil", "#ECE4D3", 8},
                {TipoTela.LINO, "Natural", "#D8CBB3", 19}, {TipoTela.LINO, "Piedra", "#A59D90", 9}, {TipoTela.LINO, "Salvia", "#9AA88E", 0},
                {TipoTela.CUERO, "Caoba", "#6E3524", 9}, {TipoTela.CUERO, "Cognac", "#9A5B2E", 15}, {TipoTela.CUERO, "Negro", "#1E1E20", 13},
                {TipoTela.TERCIOPELO, "Esmeralda", "#1F5E4B", 9}, {TipoTela.TERCIOPELO, "Azul noche", "#1E2A4F", 10},
                {TipoTela.TERCIOPELO, "Burdeos", "#6B1F2E", 7},
                {TipoTela.BOUCLE, "Crema", "#EFE8DA", 12}, {TipoTela.BOUCLE, "Avena", "#D6C7AE", 7},
                {TipoTela.MICROFIBRA, "Gris", "#8E9196", 24}, {TipoTela.MICROFIBRA, "Beige", "#CBB89B", 33}};
        int n = 0;
        for (Object[] r : t) {
            n++;
            TipoTela tipo = (TipoTela) r[0];
            String codigo = String.format("TL-%02d", n);
            String color = (String) r[1];
            double minimo = tipo == TipoTela.TERCIOPELO || tipo == TipoTela.BOUCLE ? 6 : 8;
            String prefijo = switch (tipo) {
                case CHENILLE -> "CH-210";
                case LINO -> "LN-88";
                case CUERO -> "CS-450";
                case TERCIOPELO -> "TV-320";
                case BOUCLE -> "BC-115";
                case MICROFIBRA -> "MF-PET";
            };
            tel.put(codigo, telaRepository.save(Tela.builder().codigo(codigo).tipo(tipo).color(color).hex((String) r[2])
                    .codigoProveedor(prefijo + " " + color.substring(0, Math.min(3, color.length())).toUpperCase(Locale.ROOT))
                    .stock(((Integer) r[3]).doubleValue()).minimo(minimo).build()));
        }
    }

    // ================================================================ productos

    private void productos() {
        pr("SF-001", "Sofá Moderno 3 cuerpos", "Sofá", Forma.MODERNO, "Chenille", "Gris perla", "#B9B7B1", 210, 92, 85, 3, null, 2490, 4,
                "Sofá de líneas rectas con cojines de asiento en espuma D23 de 5\" y respaldo en D18. Estructura de tornillo cepillado y patas cónicas de madera.");
        pr("SF-002", "Sofá Moderno 2 cuerpos", "Sofá", Forma.MODERNO, "Chenille", "Grafito", "#4A4D52", 160, 92, 85, 2, null, 1990, 3,
                "La versión de dos cuerpos del modelo más vendido, ideal para salas pequeñas y departamentos.");
        pr("SF-003", "Sillón Moderno", "Sillón", Forma.SILLON, "Chenille", "Arena", "#CDB89A", 95, 90, 85, 1, null, 1190, 2,
                "Sillón individual que combina con la línea Moderno. Brazos rectos y asiento firme.");
        pr("SF-004", "Juego de sala 3-2-1 Moderno", "Juego de sala", Forma.JUEGO, "Chenille", "Grafito", "#4A4D52", 210, 92, 85, 6, "3 + 2 + 1 cuerpos", 5490, 1,
                "Juego completo: sofá de 3 cuerpos, sofá de 2 cuerpos y sillón, en la misma tela.");
        pr("SF-005", "Seccional L con chaise", "Seccional", Forma.SECCIONAL_L, "Lino", "Natural", "#D8CBB3", 280, 157, 85, 3, null, 5490, 1,
                "Seccional en L con chaise reversible de 157 cm de fondo. Perfecto para salas familiares.");
        pr("SF-006", "Chesterfield 3 cuerpos", "Chesterfield", Forma.CHESTERFIELD, "Cuero sintético", "Cognac", "#9A5B2E", 215, 90, 78, 3, null, 3890, 2,
                "Clásico Chesterfield con capitoné de 48 botones, brazos enrollados y resortes zigzag.");
        pr("SF-007", "Sofá cama 3 cuerpos", "Sofá cama", Forma.SOFA_CAMA, "Microfibra antimascotas", "Beige", "#CBB89B", 200, 95, 88, 3, null, 2890, 0,
                "Sofá que se convierte en cama de 1.40 m con mecanismo plegable y colchoneta de 12 cm.");
        pr("SF-008", "Reclinable 3 cuerpos", "Reclinable", Forma.RECLINABLE, "Cuero sintético", "Negro", "#1E1E20", 205, 98, 100, 3, null, 4290, 1,
                "Tres cuerpos reclinables con mecanismo manual independiente por cuerpo.");
        pr("SF-009", "Escandinavo 2 cuerpos", "Sofá", Forma.ESCANDINAVO, "Bouclé", "Crema", "#EFE8DA", 170, 85, 80, 2, null, 2190, 3,
                "Diseño nórdico en bouclé, brazos delgados y patas de madera natural.");
        pr("SF-010", "Puff cubo", "Puff", Forma.PUFF, "Chenille", "Mostaza", "#C9972E", 45, 45, 42, 1, null, 260, 12,
                "Puff tapizado de 45 cm con zócalo negro. Combina con cualquier sala.");
        pr("SF-011", "Seccional U modular", "Seccional", Forma.SECCIONAL_U, "Terciopelo", "Azul noche", "#1E2A4F", 320, 220, 85, 5, null, 7490, 0,
                "Seccional en U de cinco módulos para salas amplias, lobbies y restaurantes.");
    }

    private void pr(String codigo, String nombre, String cat, Forma forma, String material, String color, String hex, int l, int f, int a,
                    int cuerpos, String medidas, double precio, int stock, String desc) {
        prod.put(codigo, productoRepository.save(Producto.builder().codigo(codigo).nombre(nombre).categoria(cat).forma(forma)
                .material(material).color(color).colorHex(hex).largo(l).fondo(f).alto(a).cuerpos(cuerpos).medidasTexto(medidas)
                .precio(BigDecimal.valueOf(precio).setScale(2, RoundingMode.HALF_UP)).costo(BigDecimal.valueOf(precio * 0.6).setScale(2, RoundingMode.HALF_UP))
                .stock(stock).descripcion(desc).imagenes(imagenesPorDefecto(codigo)).activo(true).build()));
    }

    /** Fotos incluidas en resources/images/sofas (si existen). */
    private String imagenesPorDefecto(String codigo) {
        return getClass().getResource("/images/sofas/" + codigo + ".jpg") != null ? "classpath:/images/sofas/" + codigo + ".jpg" : null;
    }

    // ================================================================ fichas tecnicas

    /** Linea de la plantilla del sofa de 3 cuerpos (210 × 92 × 85). */
    private record T(String material, Componente comp, double cant, double merma, Dependencia dep, boolean opcional) {
    }

    private static final List<T> PLANTILLA = List.of(
            new T("MP-001", Componente.ESTRUCTURA, 38, 18, Dependencia.L, false),
            new T("MP-002", Componente.ESTRUCTURA, 1.2, 8, Dependencia.LF, false),
            new T("MP-009", Componente.ASIENTO, 22, 10, Dependencia.L, false),
            new T("MP-019", Componente.ASIENTO, 1.6, 12, Dependencia.LF, false),
            new T("MP-018", Componente.RESPALDO, 1.2, 12, Dependencia.LA, false),
            new T("MP-017", Componente.BRAZOS, 0.8, 12, Dependencia.F, false),
            new T("MP-003", Componente.TAPIZADO, 9.5, 10, Dependencia.T, false),
            new T("MP-011", Componente.TAPIZADO, 8, 10, Dependencia.T, false),
            new T("MP-012", Componente.TAPIZADO, 4, 10, Dependencia.LF, false),
            new T("MP-015", Componente.TAPIZADO, 2, 5, Dependencia.F, false),
            new T("MP-013", Componente.COJINES, 2.5, 5, Dependencia.N, false),
            new T("MP-014", Componente.COJINES, 6, 0, Dependencia.N, false),
            new T("MP-016", Componente.COJINES, 6, 0, Dependencia.N, true),
            new T("MP-027", Componente.ENSAMBLAJE, 0.6, 5, Dependencia.L, false),
            new T("MP-024", Componente.ENSAMBLAJE, 0.25, 5, Dependencia.L, false),
            new T("MP-025", Componente.ENSAMBLAJE, 0.4, 5, Dependencia.T, false),
            new T("MP-026", Componente.ENSAMBLAJE, 0.8, 5, Dependencia.T, false),
            new T("MP-021", Componente.PATAS, 6, 0, Dependencia.P, false),
            new T("MP-028", Componente.ACABADO, 0.1, 5, Dependencia.F, false),
            new T("MP-029", Componente.ACABADO, 0.12, 5, Dependencia.F, false),
            new T("MP-033", Componente.EMBALAJE, 1, 0, Dependencia.F, false));

    private static final double[] HORAS_BASE = {3, 8, 6, 10, 2, 1};

    private void fichas() {
        Medidas base = new Medidas(210, 92, 85, 3);
        // FT-001 v2 (obsoleta) y v3 (aprobada)
        FichaTecnica v2 = ficha("FT-001", 2, EstadoFicha.OBSOLETA, "SF-001", "Sofá Moderno 3 cuerpos", 210, 92, 85, 3, false, "Recto",
                LocalDate.of(2026, 3, 16));
        agregarPlantilla(v2, base, base, Map.of("MP-003", 10.0, "MP-001", 40.0, "MP-019", 1.7), Set.of());
        operaciones(v2, new double[]{3, 8.5, 6, 10.5, 2, 1});
        fichaRepository.save(v2);
        FichaTecnica v3 = ficha("FT-001", 3, EstadoFicha.APROBADA, "SF-001", "Sofá Moderno 3 cuerpos", 210, 92, 85, 3, false, "Recto",
                LocalDate.of(2026, 8, 10));
        agregarPlantilla(v3, base, base, Map.of(), Set.of());
        operaciones(v3, HORAS_BASE);
        v3.setNotas("v3: se redujo la tela de 10 a 9.5 m con el nuevo plano de corte y la madera de 40 a 38 pt.");
        fichaRepository.save(v3);

        simple("FT-002", "SF-002", "Sofá Moderno 2 cuerpos", 160, 92, 85, 2, Map.of(), Set.of(), Map.of(), 0, LocalDate.of(2026, 8, 10));
        simple("FT-003", "SF-003", "Sillón Moderno", 95, 90, 85, 1, Map.of(), Set.of(), Map.of(), 0, LocalDate.of(2026, 8, 10));

        FichaTecnica juego = ficha("FT-004", 1, EstadoFicha.APROBADA, "SF-004", "Juego de sala 3-2-1 Moderno", 210, 92, 85, 6, false, "Recto",
                LocalDate.of(2026, 8, 12));
        juego.setCompuesta(true);
        juego.setHijos(new ArrayList<>(List.of("FT-001", "FT-002", "FT-003")));
        juego.setNotas("Ficha compuesta: suma las fichas aprobadas del sofá de 3, el de 2 y el sillón.");
        fichaRepository.save(juego);

        // Seccional L: largo 280 con chaise (extras de la chaise y +25 % de mano de obra)
        FichaTecnica sec = simple("FT-005", "SF-005", "Seccional L con chaise", 280, 157, 85, 3, Map.of("MP-003", "MP-006"), Set.of(),
                Map.of("MP-001", 10.0, "MP-019", 0.6, "MP-006", 3.5, "MP-009", 8.0, "MP-011", 2.0), 25, LocalDate.of(2026, 7, 2));
        sec.setChaise(true);
        sec.setTipoCorte("Seccional");
        fichaRepository.save(sec);

        // Chesterfield: cuero ×1.2, 48 botones de capitone y resortes zigzag
        FichaTecnica ch = simple("FT-006", "SF-006", "Chesterfield 3 cuerpos", 215, 90, 78, 3, Map.of("MP-003", "MP-004"), Set.of("MP-016"),
                Map.of("MP-016", 48.0, "MP-010", 14.0), 0, LocalDate.of(2026, 6, 20));
        for (LineaBOM l : ch.getLineas()) if (l.getMaterial().getCodigo().equals("MP-004")) l.setCantidad(r2(l.getCantidad() * 1.2));
        ch.getOperaciones().stream().filter(o -> o.getEtapa() == EtapaMO.TAPIZADO).forEach(o -> o.setHoras(o.getHoras() + 6));
        ch.setTipoCorte("Chesterfield");
        ch.setNotas("Cuero ×1.2 por el capitoné; 48 botones forrados.");
        fichaRepository.save(ch);

        // Sofa cama: mecanismo plegable y colchoneta, sin cincha
        FichaTecnica cama = simple("FT-007", "SF-007", "Sofá cama 3 cuerpos", 200, 95, 88, 3, Map.of("MP-003", "MP-005"), Set.of("MP-009"),
                Map.of("MP-031", 1.0, "MP-032", 1.0), 0, LocalDate.of(2026, 5, 28));
        cama.getOperaciones().stream().filter(o -> o.getEtapa() == EtapaMO.CARPINTERIA).forEach(o -> o.setHoras(o.getHoras() + 2));
        cama.setNotas("Sin cincha: el asiento apoya en el mecanismo plegable.");
        fichaRepository.save(cama);

        // Reclinable: un mecanismo por cuerpo
        FichaTecnica rec = simple("FT-008", "SF-008", "Reclinable 3 cuerpos", 205, 98, 100, 3, Map.of("MP-003", "MP-004"), Set.of(),
                Map.of(), 0, LocalDate.of(2026, 6, 5));
        rec.agregarLinea(LineaBOM.builder().material(mat.get("MP-030")).componente(Componente.MECANISMO).cantidad(3d).mermaPct(0d)
                .dependencia(Dependencia.N).opcional(false).build());
        rec.getOperaciones().stream().filter(o -> o.getEtapa() == EtapaMO.CARPINTERIA).forEach(o -> o.setHoras(o.getHoras() + 4));
        rec.setNotas("Un mecanismo reclinable manual por cuerpo.");
        fichaRepository.save(rec);

        FichaTecnica esc = simple("FT-009", "SF-009", "Escandinavo 2 cuerpos", 170, 85, 80, 2, Map.of("MP-003", "MP-008"), Set.of(),
                Map.of(), 0, LocalDate.of(2026, 7, 14));
        for (LineaBOM l : esc.getLineas()) if (l.getMaterial().getCodigo().equals("MP-017")) l.setCantidad(0.5);
        esc.setNotas("Tela bouclé; brazos delgados.");
        fichaRepository.save(esc);

        // Puff: despiece propio
        FichaTecnica puff = ficha("FT-010", 1, EstadoFicha.APROBADA, "SF-010", "Puff cubo", 45, 45, 42, 1, false, "Puff", LocalDate.of(2026, 4, 8));
        linea(puff, "MP-001", Componente.ESTRUCTURA, 4, 18, Dependencia.L);
        linea(puff, "MP-002", Componente.ESTRUCTURA, 0.15, 8, Dependencia.LF);
        linea(puff, "MP-019", Componente.ASIENTO, 0.12, 12, Dependencia.LF);
        linea(puff, "MP-017", Componente.TAPIZADO, 0.25, 12, Dependencia.T);
        linea(puff, "MP-003", Componente.TAPIZADO, 1.6, 10, Dependencia.T);
        linea(puff, "MP-011", Componente.TAPIZADO, 1.2, 10, Dependencia.T);
        linea(puff, "MP-012", Componente.TAPIZADO, 0.3, 10, Dependencia.LF);
        linea(puff, "MP-025", Componente.ENSAMBLAJE, 0.05, 5, Dependencia.T);
        linea(puff, "MP-026", Componente.ENSAMBLAJE, 0.1, 5, Dependencia.T);
        linea(puff, "MP-033", Componente.EMBALAJE, 1, 0, Dependencia.F);
        operaciones(puff, new double[]{0.5, 1, 0.5, 1.5, 0.3, 0.2});
        fichaRepository.save(puff);

        // Seccional U en Borrador (para demostrar el bloqueo y las OP sin ficha)
        FichaTecnica u = ficha("FT-011", 1, EstadoFicha.BORRADOR, "SF-011", "Seccional U modular", 320, 220, 85, 5, true, "Seccional", null);
        agregarPlantilla(u, new Medidas(320, 92, 85, 5), base, Map.of(), Set.of());
        for (LineaBOM l : u.getLineas()) if (l.getMaterial().getCodigo().equals("MP-003")) l.setMaterial(mat.get("MP-007"));
        linea(u, "MP-001", Componente.ESTRUCTURA, 20, 18, Dependencia.F);
        linea(u, "MP-019", Componente.ASIENTO, 1.2, 12, Dependencia.F);
        linea(u, "MP-007", Componente.TAPIZADO, 7, 10, Dependencia.F);
        linea(u, "MP-010", Componente.ASIENTO, 20, 0, Dependencia.N);
        operaciones(u, new double[]{6, 16, 11, 19, 3, 2});
        u.setNotas("Borrador: falta validar el despiece de las dos chaises.");
        fichaRepository.save(u);

        // Costo del catalogo = costo estandar de su ficha aprobada (redondeado)
        for (Producto p : prod.values()) {
            FichaTecnica f = catalogo.fichaAprobada(fichaDe(p.getCodigo()));
            if (f != null) {
                double c = Formula.redondearDecena(fichaService.costoEstandar(f));
                p.setCosto(BigDecimal.valueOf(c).setScale(2, RoundingMode.HALF_UP));
            } else {
                p.setCosto(new BigDecimal("4620.00"));
            }
            productoRepository.save(p);
        }
    }

    private static String fichaDe(String producto) {
        return "FT-" + producto.substring(3);
    }

    private FichaTecnica simple(String codigo, String producto, String nombre, int l, int f, int a, int n, Map<String, String> reemplazos,
                                Set<String> quitar, Map<String, Double> extras, double recargoMO, LocalDate aprobada) {
        FichaTecnica ft = ficha(codigo, 1, EstadoFicha.APROBADA, producto, nombre, l, f, a, n, false, "Recto", aprobada);
        Medidas destino = new Medidas(l, f > 110 ? 92 : f, a, n);
        agregarPlantilla(ft, destino, new Medidas(210, 92, 85, 3), Map.of(), quitar);
        for (LineaBOM lb : ft.getLineas()) {
            String r = reemplazos.get(lb.getMaterial().getCodigo());
            if (r != null) lb.setMaterial(mat.get(r));
        }
        extras.forEach((m, q) -> {
            LineaBOM existente = ft.getLineas().stream().filter(x -> x.getMaterial().getCodigo().equals(m) && !x.getOpcional()).findFirst().orElse(null);
            if (existente != null && !m.equals("MP-016")) {
                existente.setCantidad(r2(existente.getCantidad() + q));
            } else {
                Material mm = mat.get(m);
                ft.agregarLinea(LineaBOM.builder().material(mm).componente(componentePara(mm)).cantidad(q)
                        .mermaPct(mm.isEntera() ? 0d : 5d).dependencia(mm.isEntera() ? Dependencia.F : Dependencia.L).opcional(false).build());
            }
        });
        double fmo = Formula.factorManoObra(destino, new Medidas(210, 92, 85, 3)) * (1 + recargoMO / 100);
        double[] h = new double[6];
        for (int i = 0; i < 6; i++) h[i] = Math.max(0.5, Math.round(HORAS_BASE[i] * fmo * 2) / 2.0);
        operaciones(ft, h);
        return fichaRepository.save(ft);
    }

    private static Componente componentePara(Material m) {
        return switch (m.getCategoria()) {
            case "Madera", "Tableros" -> Componente.ESTRUCTURA;
            case "Espuma" -> Componente.ASIENTO;
            case "Mecanismos" -> Componente.MECANISMO;
            case "Patas" -> Componente.PATAS;
            default -> m.getCodigo().equals("MP-016") ? Componente.TAPIZADO : m.getCodigo().equals("MP-010") ? Componente.ASIENTO
                    : m.isTela() ? Componente.TAPIZADO : Componente.TAPIZADO;
        };
    }

    private FichaTecnica ficha(String codigo, int version, EstadoFicha estado, String producto, String nombre, int l, int f, int a, int n,
                               boolean chaise, String corte, LocalDate aprobada) {
        return FichaTecnica.builder().codigo(codigo).version(version).uid(FichaTecnica.uid(codigo, version)).estado(estado)
                .producto(prod.get(producto)).nombre(nombre).largo(l).fondo(f).alto(a).cuerpos(n).chaise(chaise).tipoCorte(corte)
                .indirectosPct(6d).aprobadoPor(aprobada == null ? null : "Carlos Mendoza").fechaAprobacion(aprobada).build();
    }

    /** Lineas de la plantilla escaladas de {@code base} a {@code destino} con la misma formula de la explosion. */
    private void agregarPlantilla(FichaTecnica f, Medidas destino, Medidas base, Map<String, Double> fijas, Set<String> quitar) {
        for (T t : PLANTILLA) {
            if (quitar.contains(t.material())) continue;
            double q = fijas.containsKey(t.material()) ? fijas.get(t.material())
                    : t.cant() * Formula.factor(t.dep(), destino, base);
            Material m = mat.get(t.material());
            if (m.isEntera()) q = Math.ceil(q - 1e-9);
            f.agregarLinea(LineaBOM.builder().material(m).componente(t.comp()).cantidad(r2(q)).mermaPct(t.merma())
                    .dependencia(t.dep()).opcional(t.opcional()).build());
        }
    }

    private void linea(FichaTecnica f, String m, Componente c, double q, double merma, Dependencia d) {
        f.agregarLinea(LineaBOM.builder().material(mat.get(m)).componente(c).cantidad(q).mermaPct(merma).dependencia(d).opcional(false).build());
    }

    private void operaciones(FichaTecnica f, double[] horas) {
        f.getOperaciones().clear();
        EtapaMO[] e = EtapaMO.values();
        for (int i = 0; i < e.length; i++) {
            f.agregarOperacion(OperacionMO.builder().etapa(e[i]).horas(horas[i]).costoHora(e[i].costoHora()).build());
        }
    }

    // ================================================================ clientes y personal

    private void clientes() {
        c("C-0101", Cliente.Tipo.PERSONA, "Rosa Quispe Mamani", "42315678", "951 284 330", "Jr. Moquegua 345", "Juliaca", "2024-03-12");
        c("C-0102", Cliente.Tipo.PERSONA, "Juan Carlos Apaza Condori", "40128876", "973 115 902", "Av. Circunvalación 1120", "Juliaca", "2023-11-04");
        c("C-0103", Cliente.Tipo.PERSONA, "Lucía Huanca Ticona", "45567123", "950 776 410", "Jr. Huancané 218", "Juliaca", "2025-01-20");
        c("C-0104", Cliente.Tipo.EMPRESA, "Hotel Royal Inn Juliaca S.A.C.", "20448812345", "051 32 6655", "Jr. San Román 158", "Juliaca", "2022-08-15");
        c("C-0105", Cliente.Tipo.PERSONA, "Pedro Mamani Coaquira", "41890234", "984 330 117", "Urb. La Capilla Mz B Lt 7", "Juliaca", "2024-06-02");
        c("C-0106", Cliente.Tipo.PERSONA, "Carmen Flores Pari", "43678901", "951 902 667", "Jr. Lambayeque 560", "Puno", "2025-04-18");
        c("C-0107", Cliente.Tipo.PERSONA, "Miguel Ángel Ccama Quispe", "46012345", "966 210 054", "Av. Independencia 890", "Juliaca", "2025-09-01");
        c("C-0108", Cliente.Tipo.PERSONA, "Gladys Turpo Mamani", "44123987", "951 448 213", "Jr. Arequipa 233", "Puno", "2026-02-10");
        c("C-0109", Cliente.Tipo.PERSONA, "Ronald Chura Apaza", "47234561", "974 556 801", "Av. Huancané 1450", "Juliaca", "2026-05-22");
        c("C-0110", Cliente.Tipo.EMPRESA, "Restaurante El Altiplano E.I.R.L.", "20601234871", "051 36 9044", "Jr. Lima 412", "Puno", "2023-02-27");
        c("C-0111", Cliente.Tipo.PERSONA, "Silvia Cutipa Larico", "42987650", "951 121 774", "Jr. 2 de Mayo 175", "Juliaca", "2026-07-08");
        c("C-0112", Cliente.Tipo.EMPRESA, "I.E.P. San Martín de Porres", "20406789123", "051 32 8810", "Av. Tacna 760", "Juliaca", "2024-12-03");
    }

    private void c(String codigo, Cliente.Tipo tipo, String nombre, String doc, String tel, String dir, String ciudad, String fecha) {
        String email = nombre.split(" ")[0].toLowerCase(Locale.ROOT).replace("á", "a").replace(".", "") + "." + codigo.substring(2) + "@gmail.com";
        cli.put(codigo, clienteRepository.save(Cliente.builder().codigo(codigo).tipo(tipo).nombre(nombre).documento(doc).telefono(tel)
                .email(tipo == Cliente.Tipo.EMPRESA ? "compras@" + nombre.split(" ")[1].toLowerCase(Locale.ROOT).replace(".", "") + ".pe" : email)
                .direccion(dir).ciudad(ciudad).fechaRegistro(LocalDate.parse(fecha)).build()));
    }

    private void empleados() {
        e("E-01", "Rubén Quispe Flores", "40211873", "Supervisor de taller", "Taller · Tapizado", Rol.SUPERVISOR, "951 300 412", "2014-02-03", Etapa.CONTROL_CALIDAD, "#14213D");
        e("E-02", "Fredy Choque Mamani", "41876230", "Carpintero", "Taller · Carpintería", Rol.CARPINTERO, "951 412 908", "2016-05-16", Etapa.ARMADO, "#A47148");
        e("E-03", "Wilber Condori Apaza", "43902157", "Carpintero", "Taller · Carpintería", Rol.CARPINTERO, "973 220 145", "2019-08-01", Etapa.CORTE_MADERA, "#C69A6D");
        e("E-04", "Yolanda Ccori Huamán", "42557310", "Costurera", "Taller · Costura", Rol.COSTURERA, "951 667 203", "2015-03-09", Etapa.COSTURA, "#F59E0B");
        e("E-05", "Marleny Huanca Quispe", "45110298", "Costurera", "Taller · Costura", Rol.COSTURERA, "984 102 776", "2020-10-12", Etapa.CORTE_TELA, "#EC4899");
        e("E-06", "Edwin Mamani Ticona", "41023987", "Tapicero", "Taller · Tapizado", Rol.TAPICERO, "951 883 041", "2013-07-22", Etapa.TAPIZADO, "#2563EB");
        e("E-07", "Hugo Pari Calsín", "44670231", "Tapicero", "Taller · Tapizado", Rol.TAPICERO, "966 340 812", "2018-01-15", Etapa.TAPIZADO, "#0EA5E9");
        e("E-08", "Nilton Apaza Cutipa", "46781203", "Tapicero", "Taller · Tapizado", Rol.TAPICERO, "951 776 390", "2021-04-05", Etapa.ACABADO, "#14B8A6");
        e("E-09", "Jhon Coaquira Vilca", "48012376", "Ayudante de taller", "Taller · Carpintería", Rol.CARPINTERO, "974 118 560", "2023-02-20", Etapa.CINCHADO, "#8B5CF6");
        e("E-10", "Elvis Ticona Larico", "47309812", "Ayudante de espumado", "Taller · Espumado", Rol.TAPICERO, "951 230 778", "2022-09-12", Etapa.ESPUMADO, "#0EA5E9");
        e("E-11", "Roxana Vilca Mendoza", "45890123", "Vendedora", "Tienda", Rol.VENDEDOR, "951 456 120", "2017-11-06", null, "#DB2777");
        e("E-12", "Katherine Flores Chambi", "48123450", "Vendedora", "Tienda", Rol.VENDEDOR, "973 905 331", "2024-03-01", null, "#7C3AED");
        e("E-13", "Luis Arocutipa Ramos", "43345612", "Almacenero", "Almacén", Rol.ALMACENERO, "951 558 207", "2018-06-18", null, "#64748B");
        e("E-14", "Milagros Cahuana Ponce", "44781209", "Administradora", "Administración", Rol.ADMINISTRADOR, "951 220 984", "2016-01-11", null, "#16A34A");
        empleadoRepository.findByCodigo("E-12").ifPresent(x -> {
            x.setEstado("Vacaciones");
            empleadoRepository.save(x);
        });
    }

    private void e(String codigo, String nombre, String dni, String cargo, String area, Rol rol, String tel, String ingreso, Etapa etapa, String color) {
        emp.put(codigo, empleadoRepository.save(Empleado.builder().codigo(codigo).nombre(nombre).dni(dni).cargo(cargo).area(area).rol(rol)
                .telefono(tel).ingreso(LocalDate.parse(ingreso)).estado("Activo").etapa(etapa).color(color).build()));
    }

    // ================================================================ pedidos

    private Pedido pe(String codigo, String cliente, String producto, Pedido.Tipo tipo, int cant, String fecha, String entrega, double precio,
                      double adelanto, EstadoPedido estado, String tela, Integer l, Integer f, Integer a, String obs, String espec) {
        Tela t = tela == null ? null : tel.get(tela);
        Pedido p = pedidoRepository.save(Pedido.builder().codigo(codigo).cliente(cli.get(cliente)).producto(prod.get(producto)).tipo(tipo)
                .cantidad(cant).fecha(LocalDate.parse(fecha)).fechaEntrega(LocalDate.parse(entrega))
                .precio(BigDecimal.valueOf(precio).setScale(2, RoundingMode.HALF_UP)).adelanto(BigDecimal.valueOf(adelanto).setScale(2, RoundingMode.HALF_UP))
                .estado(estado).tela(t).material(t == null ? null : t.getTipo().nombre()).largo(l).fondo(f).alto(a)
                .observaciones(obs).especificacion(espec).build());
        if (adelanto > 0) {
            boolean total = adelanto >= precio;
            if (total && estado == EstadoPedido.ENTREGADO && adelanto > precio * 0.5) {
                pago(p, LocalDate.parse(fecha), Math.round(precio * 0.5), "Transferencia", "Adelanto");
                pago(p, LocalDate.parse(entrega), precio - Math.round(precio * 0.5), "Efectivo", "Cancelación");
            } else {
                pago(p, LocalDate.parse(fecha), adelanto, codigo.endsWith("1") || codigo.endsWith("5") ? "Yape" : "Transferencia", "Adelanto");
            }
        }
        return p;
    }

    private void pago(Pedido p, LocalDate fecha, double monto, String metodo, String concepto) {
        pagoRepository.save(Pago.builder().cliente(p.getCliente()).referencia(p.getCodigo()).fecha(fecha)
                .monto(BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP)).metodo(metodo).concepto(concepto).build());
    }

    private void pedidos() {
        pe("PE-0237", "C-0105", "SF-002", Pedido.Tipo.ESTANDAR, 1, "2026-09-18", "2026-09-28", 1990, 1990, EstadoPedido.ENTREGADO, "TL-06", null, null, null,
                "Entregado e instalado.", null);
        pe("PE-0238", "C-0110", "SF-010", Pedido.Tipo.ESTANDAR, 6, "2026-09-22", "2026-10-02", 1560, 1560, EstadoPedido.ENTREGADO, "TL-12", null, null, null,
                "Seis puffs para la terraza del restaurante.", null);
        pe("PE-0239", "C-0107", "SF-009", Pedido.Tipo.PERSONALIZADO, 1, "2026-09-25", "2026-10-20", 2350, 0, EstadoPedido.CANCELADO, "TL-19", 180, 85, 80,
                "Cliente desistió: se mudará fuera de Juliaca.", "Escandinavo a medida 180 cm");
        pe("PE-0240", "C-0109", "SF-001", Pedido.Tipo.PERSONALIZADO, 1, "2026-09-29", "2026-10-16", 2690, 1345, EstadoPedido.LISTO, "TL-03", 220, 92, 85,
                "Coordinar entrega por la tarde.", "Sofá Moderno de 220 cm en chenille Arena");
        pe("PE-0241", "C-0101", "SF-001", Pedido.Tipo.PERSONALIZADO, 1, "2026-10-01", "2026-10-20", 2690, 1500, EstadoPedido.EN_PRODUCCION, "TL-01", 220, 92, 85,
                "Cojines decorativos con vivo color Mostaza. Proyecto de sala: sofá + sillón.", "Sofá Moderno de 220 cm en chenille Gris perla");
        pe("PE-0242", "C-0102", "SF-005", Pedido.Tipo.PERSONALIZADO, 1, "2026-09-30", "2026-10-18", 5930, 3000, EstadoPedido.EN_PRODUCCION, "TL-09", 280, 157, 85,
                "Chaise al lado derecho mirando de frente.", "Seccional L con chaise derecha en lino Natural");
        pe("PE-0243", "C-0104", "SF-003", Pedido.Tipo.ESTANDAR, 4, "2026-10-02", "2026-10-16", 4760, 2380, EstadoPedido.EN_PRODUCCION, "TL-20", null, null, null,
                "Entregar en recepción del hotel; coordinar con administración.", "4 sillones para el lobby");
        pe("PE-0244", "C-0106", "SF-008", Pedido.Tipo.PERSONALIZADO, 1, "2026-09-26", "2026-10-14", 4800, 2400, EstadoPedido.EN_PRODUCCION, "TL-14", null, null, null,
                "Mecanismo reclinable en los 3 cuerpos.", "Reclinable 3 cuerpos en cuero sintético Negro");
        pe("PE-0245", "C-0103", "SF-006", Pedido.Tipo.PERSONALIZADO, 1, "2026-10-03", "2026-10-22", 4360, 2500, EstadoPedido.EN_PRODUCCION, "TL-13", null, null, null,
                "4.º piso sin ascensor: subir por escalera, medir el pasadizo.", "Chesterfield en cuero sintético Cognac");
        pe("PE-0246", "C-0105", "SF-004", Pedido.Tipo.PERSONALIZADO, 1, "2026-10-06", "2026-10-30", 5490, 3000, EstadoPedido.EN_PRODUCCION, "TL-02", null, null, null,
                "Misma tela para todo el juego.", "Juego 3-2-1 en chenille Grafito");
        pe("PE-0247", "C-0112", "SF-002", Pedido.Tipo.PERSONALIZADO, 3, "2026-10-07", "2026-10-26", 5970, 2985, EstadoPedido.EN_PRODUCCION, "TL-04", null, null, null,
                "Sala de profesores. Factura a nombre de la I.E.P.; entregar de 8 a 13 h.", "3 sofás de 2 cuerpos en chenille Azul petróleo");
        pe("PE-0248", "C-0108", "SF-003", Pedido.Tipo.PERSONALIZADO, 1, "2026-10-12", "2026-11-05", 1370, 700, EstadoPedido.CONFIRMADO, "TL-15", null, null, null,
                "Brazos redondeados.", "Sillón en terciopelo Esmeralda");
        pe("PE-0249", "C-0111", "SF-009", Pedido.Tipo.PERSONALIZADO, 1, "2026-10-14", "2026-11-12", 2630, 0, EstadoPedido.COTIZADO, "TL-19", null, null, null,
                "Envió fotos de su sala por WhatsApp.", "Escandinavo 2 cuerpos en bouclé Avena");
    }

    // ================================================================ ordenes de produccion

    private OrdenProduccion op(String codigo, String producto, String nombre, int cant, String cliente, String responsable, Prioridad prio,
                               String inicio, String entrega, Etapa etapa, String tela, String pedido, Integer largo) {
        Producto p = producto == null ? null : prod.get(producto);
        FichaTecnica f = p == null ? null : fichaService.aprobadaDe(p);
        Tela t = tela == null ? null : tel.get(tela);
        Map<String, String> sust = new HashMap<>();
        if (t != null && f != null) for (String g : produccionService.materialesTela(f)) sust.put(g, t.getTipo().materialCodigo());
        OrdenProduccion o = OrdenProduccion.builder().codigo(codigo).producto(p).nombrePersonalizado(nombre).cantidad(cant)
                .cliente(cliente == null ? null : cli.get(cliente)).responsable(emp.get(responsable)).prioridad(prio)
                .fechaInicio(LocalDate.parse(inicio)).fechaEntrega(LocalDate.parse(entrega)).etapa(etapa)
                .fichaUid(f == null ? null : f.getUid()).tela(t).sustituciones(sust).pedidoCodigo(pedido)
                .largo(largo).fondo(largo == null ? null : f == null ? null : f.getFondo()).alto(largo == null ? null : f == null ? null : f.getAlto())
                .cuerpos(largo == null ? null : f == null ? null : f.getCuerpos()).build();
        o = opRepository.save(o);
        o.setCosto(produccionService.costoCalculado(o));
        o = opRepository.save(o);
        ops.put(codigo, o);
        if (pedido != null) pedidoRepository.findByCodigo(pedido).ifPresent(x -> {
            x.setOpCodigo(codigo);
            pedidoRepository.save(x);
        });
        return o;
    }

    private void ordenes() {
        op("OP-1019", "SF-001", null, 1, "C-0101", "E-06", Prioridad.MEDIA, "2026-10-05", "2026-10-20", Etapa.TAPIZADO, "TL-01", "PE-0241", 220);
        op("OP-1020", "SF-005", null, 1, "C-0102", "E-04", Prioridad.ALTA, "2026-10-03", "2026-10-18", Etapa.COSTURA, "TL-09", "PE-0242", null);
        op("OP-1021", "SF-003", null, 4, "C-0104", "E-01", Prioridad.ALTA, "2026-10-04", "2026-10-16", Etapa.CONTROL_CALIDAD, "TL-20", "PE-0243", null);
        op("OP-1022", "SF-008", null, 1, "C-0106", "E-10", Prioridad.ALTA, "2026-09-29", "2026-10-14", Etapa.ESPUMADO, "TL-14", "PE-0244", null);
        op("OP-1023", "SF-006", null, 1, "C-0103", "E-08", Prioridad.MEDIA, "2026-10-06", "2026-10-22", Etapa.ACABADO, "TL-13", "PE-0245", null);
        op("OP-1024", "SF-007", null, 3, null, "E-02", Prioridad.BAJA, "2026-10-08", "2026-10-28", Etapa.ARMADO, "TL-21", null, null);
        op("OP-1025", "SF-004", null, 1, "C-0105", "E-03", Prioridad.MEDIA, "2026-10-13", "2026-10-30", Etapa.CORTE_MADERA, "TL-02", "PE-0246", null);
        OrdenProduccion retap = op("OP-1026", null, "Retapizado · Sofá 2 cuerpos (Silvia Cutipa)", 1, "C-0111", "E-05", Prioridad.MEDIA,
                "2026-10-12", "2026-10-19", Etapa.CORTE_TELA, "TL-10", null, null);
        retap.setRetapizadoCodigo("RT-032");
        retap.setAutorizacion("Retapizado RT-032 · no requiere ficha");
        retap.setEspecificacion("Retapizado 160 × 88 × 82 cm · espuma hundida · lino Piedra");
        retap.setCosto(new BigDecimal("640.00"));
        opRepository.save(retap);
        op("OP-1027", "SF-002", null, 3, "C-0112", "E-09", Prioridad.MEDIA, "2026-10-09", "2026-10-26", Etapa.CINCHADO, "TL-04", "PE-0247", null);
        op("OP-1028", "SF-003", null, 1, null, "E-06", Prioridad.BAJA, "2026-10-19", "2026-11-03", Etapa.PENDIENTE, "TL-15", null, null);
        OrdenProduccion listo = op("OP-1029", "SF-001", null, 1, "C-0109", "E-07", Prioridad.MEDIA, "2026-09-30", "2026-10-16", Etapa.LISTO, "TL-03", "PE-0240", 220);
        listo.setFechaFin(LocalDate.of(2026, 10, 13));
        opRepository.save(listo);
        OrdenProduccion u = op("OP-1030", "SF-011", null, 1, "C-0110", "E-03", Prioridad.ALTA, "2026-10-14", "2026-11-05", Etapa.CORTE_MADERA, "TL-16", null, null);
        u.setAutorizacion("Autorizado por Rubén Quispe Flores (Supervisor) el 14/10/2026: ficha FT-011 en revisión.");
        opRepository.save(u);

        // Reservas al arrancar: las OP en etapas productivas ya reservaron; tres quedan parciales a proposito.
        Map<String, Double> parcial = Map.of("OP-1024|MP-002", 1.0, "OP-1027|MP-013", 3.0, "OP-1025|MP-029", 1.0);
        for (OrdenProduccion o : ops.values()) {
            if (!o.getEtapa().productiva() || o.isSinFicha()) continue;
            Explosion.Resultado r = produccionService.requerimiento(o);
            for (Explosion.PorMaterial pm : r.porMaterial()) {
                Material m = mat.get(pm.material().getCodigo());
                Double faltaFrac = parcial.get(o.getCodigo() + "|" + m.getCodigo());
                double cant = pm.cantidad();
                if (faltaFrac != null) cant = m.getCodigo().equals("MP-029") ? 0 : Math.max(0, cant - faltaFrac);
                cant = Math.ceil(cant * 100 - 1e-6) / 100.0;
                if (cant <= 0) continue;
                reservaRepository.save(Reserva.builder().op(o).material(m).cantidad(cant).build());
                m.setReservado(m.getReservado() + cant);
                materialRepository.save(m);
                movimientoRepository.save(Movimiento.builder().material(m).fecha(LocalDateTime.of(o.getFechaInicio(), LocalTime.of(8, 30)))
                        .tipo(TipoMovimiento.RESERVA).cantidad(cant).referencia(o.getCodigo()).concepto("Reserva " + o.getProductoNombre())
                        .usuario("rquispe").build());
            }
        }
    }

    // ================================================================ retapizado y garantias

    private void retapizados() {
        rt("RT-030", "C-0107", "Sofá 3 cuerpos antiguo, tela floreada gastada", 3, 200, 90, 85, "Buena", "Hundida", "TL-06",
                "2026-09-20", "2026-10-05", "2026-10-12", EstadoRetapizado.ENTREGADO, null);
        rt("RT-031", "C-0108", "Sillón bergère heredado", 1, 85, 80, 95, "Regular", "Cambio total", "TL-17",
                "2026-10-09", null, null, EstadoRetapizado.COTIZADO, null);
        rt("RT-032", "C-0111", "Sofá 2 cuerpos color vino, espuma hundida", 2, 160, 88, 82, "Buena", "Hundida", "TL-10",
                "2026-10-06", "2026-10-11", "2026-10-19", EstadoRetapizado.EN_TALLER, "OP-1026");
        rt("RT-033", "C-0109", "Seccional en L de 3 cuerpos con brazo roto", 3, 240, 90, 85, "Requiere refuerzo", "Cambio total", "TL-20",
                "2026-10-13", "2026-10-17", null, EstadoRetapizado.RECOJO_PROGRAMADO, null);
        rt("RT-034", "C-0101", "Sillón de la sala de Rosa (proyecto sofá + sillón)", 1, 90, 85, 88, "Buena", "Buena", null,
                "2026-10-15", null, null, EstadoRetapizado.POR_COTIZAR, null);
    }

    private void rt(String codigo, String cliente, String desc, int cuerpos, int l, int f, int a, String estr, String esp, String tela,
                    String registro, String recojo, String entrega, EstadoRetapizado estado, String op) {
        Tela t = tela == null ? null : tel.get(tela);
        Retapizado r = Retapizado.builder().codigo(codigo).cliente(cli.get(cliente)).descripcion(desc).cuerpos(cuerpos).largo(l).fondo(f).alto(a)
                .estructura(estr).espuma(esp).tela(t).fechaRegistro(LocalDate.parse(registro))
                .fechaRecojo(recojo == null ? null : LocalDate.parse(recojo)).fechaEntrega(entrega == null ? null : LocalDate.parse(entrega))
                .estado(estado).opCodigo(op).build();
        if (t != null) {
            RetapizadoService.Cotizacion c = retapizadoService.cotizar(r);
            r.setMetros(r2(c.metrosTela()));
            r.setTotal(BigDecimal.valueOf(c.precio()).setScale(2, RoundingMode.HALF_UP));
        }
        retapizadoRepository.save(r);
        if (estado == EstadoRetapizado.ENTREGADO && r.getTotal() != null) {
            pagoRepository.save(Pago.builder().cliente(r.getCliente()).referencia(codigo).fecha(r.getFechaEntrega()).monto(r.getTotal())
                    .metodo("Efectivo").concepto("Retapizado").build());
        }
    }

    private void reclamos() {
        gr("GR-012", "C-0105", "SF-002", "2026-03-10", "Pata floja", "Una pata trasera se mueve al sentarse.", "2026-09-14",
                EstadoReclamo.RESUELTO, "Taco interno despegado; se reencoló y atornilló.", "Reparación en domicilio", 18, "2026-09-16");
        gr("GR-013", "C-0101", "SF-003", "2026-02-15", "Costura abierta", "Se abrió la costura del cojín de asiento.", "2026-10-06",
                EstadoReclamo.EN_DIAGNOSTICO, "Revisando si es desgaste por uso o falla de hilo.", "Por definir", 0, null);
        gr("GR-014", "C-0104", "SF-001", "2026-06-20", "Espuma hundida", "Dos cojines del lobby se hundieron.", "2026-10-13",
                EstadoReclamo.ABIERTO, null, "Por definir", 0, null);
        gr("GR-015", "C-0106", "SF-008", "2025-11-02", "Mecanismo trabado", "El cuerpo izquierdo no vuelve a su posición.", "2026-10-02",
                EstadoReclamo.EN_REPARACION, "Resorte del mecanismo vencido; se cambia el mecanismo completo.", "Reemplazo del mecanismo", 180, null);
        gr("GR-016", "C-0110", "SF-010", "2026-01-05", "Decoloración de tela", "Los puffs de la terraza perdieron color.", "2026-09-22",
                EstadoReclamo.RECHAZADO, "Exposición directa al sol en uso comercial; fuera de cobertura.", "No procede", 0, "2026-09-24");
    }

    private void gr(String codigo, String cliente, String producto, String compra, String falla, String desc, String fecha, EstadoReclamo estado,
                    String diag, String sol, double costo, String cierre) {
        reclamoRepository.save(ReclamoGarantia.builder().codigo(codigo).cliente(cli.get(cliente)).producto(prod.get(producto))
                .fechaCompra(LocalDate.parse(compra)).falla(falla).descripcion(desc).fecha(LocalDate.parse(fecha)).estado(estado)
                .diagnostico(diag).solucion(sol).costo(BigDecimal.valueOf(costo).setScale(2, RoundingMode.HALF_UP))
                .fechaCierre(cierre == null ? null : LocalDate.parse(cierre)).build());
    }

    // ================================================================ consumo historico

    private void consumos() {
        Object[][] hist = {
                {"OP-1010", "SF-001", 1, "E-02", "2026-09-08", 0.03},
                {"OP-1011", "SF-002", 2, "E-06", "2026-09-12", -0.06},
                {"OP-1012", "SF-007", 1, "E-02", "2026-09-16", 0.10},
                {"OP-1013", "SF-005", 1, "E-07", "2026-09-19", 0.06},
                {"OP-1014", "SF-003", 2, "E-08", "2026-09-23", -0.05},
                {"OP-1015", "SF-001", 2, "E-06", "2026-09-26", -0.04},
                {"OP-1016", "SF-006", 1, "E-07", "2026-09-30", 0.12},
                {"OP-1017", "SF-009", 1, "E-08", "2026-10-03", -0.03},
                {"OP-1018", "SF-002", 1, "E-02", "2026-10-07", 0.05}};
        Random rnd = new Random(2026);
        for (Object[] h : hist) {
            Producto p = prod.get((String) h[1]);
            FichaTecnica f = fichaService.aprobadaDe(p);
            Empleado e = emp.get((String) h[3]);
            double desvioOp = (double) h[5] + (e.getCodigo().equals("E-02") ? 0.03 : 0);
            Explosion.Resultado r = fichaService.explotar(f, (Integer) h[2], Opciones.NINGUNA);
            LocalDate fecha = LocalDate.parse((String) h[4]);
            for (Explosion.PorMaterial pm : r.porMaterial()) {
                Material m = pm.material();
                double porMaterial = switch (m.getCategoria()) {
                    case "Madera" -> 0.03;
                    case "Espuma" -> 0.02;
                    case "Tableros" -> 0.01;
                    default -> m.isTela() ? 0.015 : 0;
                };
                double ruido = (rnd.nextDouble() - 0.5) * 0.04;
                double real = m.isEntera() ? pm.cantidad() : r2(pm.cantidad() * (1 + desvioOp + porMaterial + ruido));
                consumoRepository.save(ConsumoReal.builder().opCodigo((String) h[0]).producto(p.getNombre()).fichaUid(f.getUid())
                        .responsable(e.getNombre()).material(m).estimado(r2(pm.cantidad())).realConsumido(real).fecha(fecha).build());
                if (!fecha.isBefore(LocalDate.of(2026, 10, 1)) && real > 0) {
                    fisico(m, LocalDateTime.of(fecha, LocalTime.of(17, 0)), TipoMovimiento.CONSUMO_OP, real, (String) h[0], "Consumo " + p.getNombre(), "rquispe");
                }
            }
        }
        // OP-1029 (lista el 13/10): consumio su material
        OrdenProduccion listo = ops.get("OP-1029");
        Explosion.Resultado r = produccionService.requerimiento(listo);
        for (Explosion.PorMaterial pm : r.porMaterial()) {
            Material m = pm.material();
            double real = m.isEntera() ? pm.cantidad() : r2(pm.cantidad() * (m.getCategoria().equals("Espuma") ? 1.05 : 1.02));
            consumoRepository.save(ConsumoReal.builder().opCodigo("OP-1029").producto(listo.getProductoNombre()).fichaUid(listo.getFichaUid())
                    .responsable(listo.getResponsable().getNombre()).material(m).estimado(r2(pm.cantidad())).realConsumido(real)
                    .fecha(LocalDate.of(2026, 10, 13)).build());
            fisico(m, LocalDateTime.of(2026, 10, 13, 16, 40), TipoMovimiento.CONSUMO_OP, real, "OP-1029", "Consumo " + listo.getProductoNombre(), "rquispe");
        }
        movimientoRepository.save(Movimiento.builder().material(mat.get("MP-019")).fecha(LocalDateTime.of(2026, 10, 13, 16, 50))
                .tipo(TipoMovimiento.RETAZO).cantidad(0.3).referencia("OP-1029").concepto("Retazo reutilizable · 0.6 m² de espuma D23")
                .usuario("rquispe").build());
    }

    /** Movimiento fisico posterior al saldo inicial (afecta el calculo del saldo inicial). */
    private void fisico(Material m, LocalDateTime fecha, TipoMovimiento tipo, double cant, String ref, String concepto, String usuario) {
        movimientos.add(Movimiento.builder().material(m).fecha(fecha).tipo(tipo).cantidad(cant).referencia(ref).concepto(concepto).usuario(usuario).build());
    }

    // ================================================================ compras

    private OrdenCompra oc(String codigo, String proveedor, String fecha, EstadoCompra estado, String origen, boolean nueva, String recepcion,
                           Object[][] items) {
        OrdenCompra o = OrdenCompra.builder().codigo(codigo).proveedor(prov.get(proveedor)).fecha(LocalDate.parse(fecha))
                .fechaEntrega(LocalDate.parse(fecha).plusDays(5)).estado(estado).origen(origen).nueva(nueva)
                .fechaRecepcion(recepcion == null ? null : LocalDate.parse(recepcion)).build();
        for (Object[] it : items) {
            Material m = mat.get((String) it[0]);
            Tela t = it.length > 2 ? tel.get((String) it[2]) : null;
            o.agregar(ItemCompra.builder().material(m).tela(t).cantidad(((Number) it[1]).doubleValue())
                    .precioUnitario(BigDecimal.valueOf(m.getCosto()).setScale(2, RoundingMode.HALF_UP)).build());
            if (estado == EstadoCompra.RECIBIDA && !m.isTela()) {
                fisico(m, LocalDateTime.of(o.getFechaRecepcion(), LocalTime.of(11, 15)), TipoMovimiento.ENTRADA, ((Number) it[1]).doubleValue(),
                        codigo, "Recepción " + o.getProveedor().getRazonSocial(), "larocutipa");
            } else if (estado == EstadoCompra.RECIBIDA) {
                fisico(m, LocalDateTime.of(o.getFechaRecepcion(), LocalTime.of(11, 20)), TipoMovimiento.ENTRADA, ((Number) it[1]).doubleValue(),
                        codigo, "Rollo " + t.getColor() + " " + t.getCodigo(), "larocutipa");
            }
        }
        return compraRepository.save(o);
    }

    private void compras() {
        oc("OC-0317", "PR-01", "2026-10-09", EstadoCompra.RECIBIDA, "OP-1024", false, "2026-10-14",
                new Object[][]{{"MP-002", 12}, {"MP-001", 300}, {"MP-021", 40}});
        oc("OC-0318", "PR-04", "2026-10-01", EstadoCompra.RECIBIDA, "MRP", false, "2026-10-06",
                new Object[][]{{"MP-019", 10}, {"MP-018", 8}, {"MP-017", 6}, {"MP-009", 100}});
        oc("OC-0319", "PR-03", "2026-10-04", EstadoCompra.RECIBIDA, "Manual", false, "2026-10-09",
                new Object[][]{{"MP-011", 60}, {"MP-015", 20}, {"MP-014", 40}, {"MP-003", 30, "TL-02"}});
        oc("OC-0320", "PR-06", "2026-10-13", EstadoCompra.APROBADA, "MRP", false, null,
                new Object[][]{{"MP-029", 3}, {"MP-028", 2}});
        oc("OC-0321", "PR-04", "2026-10-14", EstadoCompra.BORRADOR, "OP-1027", false, null,
                new Object[][]{{"MP-013", 15}});
        oc("OC-0322", "PR-02", "2026-10-15", EstadoCompra.BORRADOR, "Calculadora", true, null,
                new Object[][]{{"MP-006", 20, "TL-11"}, {"MP-008", 10, "TL-19"}});
        oc("OC-0323", "PR-05", "2026-10-08", EstadoCompra.CANCELADA, "Manual", false, null,
                new Object[][]{{"MP-026", 10}});
        fisico(mat.get("MP-026"), LocalDateTime.of(2026, 10, 7, 9, 10), TipoMovimiento.SALIDA, 2, "VALE-118", "Salida a mantenimiento de engrapadoras", "larocutipa");
        fisico(mat.get("MP-027"), LocalDateTime.of(2026, 10, 10, 18, 0), TipoMovimiento.AJUSTE, -0.5, "INV-10", "Ajuste por inventario físico", "larocutipa");
    }

    // ================================================================ ventas de octubre

    private void ventas() {
        // dia, hora, cliente (null = varios), comprobante, metodo, descuento, items {producto, cantidad, tela}
        Object[][] v = {
                {1, "10:20", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-01"}}},
                {1, "16:45", "C-0107", "B", "Yape", 0, new Object[][]{{"SF-010", 2, "TL-07"}}},
                {2, "11:05", null, "B", "Tarjeta", 0, new Object[][]{{"SF-007", 1, "TL-21"}}},
                {3, "12:30", "C-0110", "F", "Transferencia", 0, new Object[][]{{"SF-011", 1, "TL-16"}}},
                {3, "17:10", null, "B", "Efectivo", 70, new Object[][]{{"SF-002", 1, "TL-02"}}},
                {4, "10:40", null, "B", "Plin", 0, new Object[][]{{"SF-003", 1, "TL-03"}}},
                {4, "15:55", "C-0108", "B", "Tarjeta", 0, new Object[][]{{"SF-006", 1, "TL-13"}}},
                {5, "11:30", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-02"}, {"SF-010", 2, "TL-07"}}},
                {6, "16:20", null, "B", "Transferencia", 290, new Object[][]{{"SF-004", 1, "TL-02"}}},
                {7, "10:15", "C-0102", "B", "Yape", 0, new Object[][]{{"SF-009", 1, "TL-18"}}},
                {7, "18:05", null, "B", "Tarjeta", 0, new Object[][]{{"SF-007", 1, "TL-21"}}},
                {8, "12:00", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-03"}}},
                {8, "17:35", null, "B", "Mixto", 0, new Object[][]{{"SF-005", 1, "TL-09"}}},
                {9, "11:50", null, "B", "Tarjeta", 0, new Object[][]{{"SF-002", 1, "TL-20"}}},
                {9, "16:10", "C-0112", "F", "Transferencia", 400, new Object[][]{{"SF-011", 0, null}, {"SF-007", 1, "TL-21"}, {"SF-003", 1, "TL-20"}}},
                {10, "10:30", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-01"}}},
                {10, "13:20", null, "B", "Yape", 0, new Object[][]{{"SF-010", 2, "TL-05"}}},
                {11, "11:40", "C-0109", "B", "Tarjeta", 0, new Object[][]{{"SF-008", 1, "TL-14"}}},
                {11, "17:00", null, "B", "Efectivo", 200, new Object[][]{{"SF-005", 1, "TL-10"}}},
                {12, "10:05", null, "B", "Plin", 0, new Object[][]{{"SF-002", 1, "TL-08"}}},
                {12, "16:30", null, "B", "Tarjeta", 0, new Object[][]{{"SF-001", 1, "TL-02"}}},
                {13, "11:10", "C-0105", "B", "Transferencia", 0, new Object[][]{{"SF-006", 1, "TL-12"}}},
                {13, "15:40", null, "B", "Efectivo", 0, new Object[][]{{"SF-009", 1, "TL-19"}}},
                {14, "10:50", null, "B", "Tarjeta", 600, new Object[][]{{"SF-004", 1, "TL-01"}}},
                {14, "12:45", null, "B", "Yape", 0, new Object[][]{{"SF-007", 1, "TL-20"}}},
                {14, "17:20", "C-0103", "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-04"}, {"SF-003", 1, "TL-04"}}},
                {15, "09:40", null, "B", "Tarjeta", 0, new Object[][]{{"SF-001", 1, "TL-01"}, {"SF-002", 1, "TL-02"}}}};
        int boleta = 1240, factura = 318;
        for (Object[] x : v) {
            boolean fac = "F".equals(x[3]);
            Venta venta = Venta.builder().comprobante(fac ? Venta.Comprobante.FACTURA : Venta.Comprobante.BOLETA)
                    .serie(fac ? "F001" : "B001").numero(fac ? ++factura : ++boleta)
                    .fecha(LocalDateTime.of(LocalDate.of(2026, 10, (Integer) x[0]), LocalTime.parse((String) x[1])))
                    .cliente(x[2] == null ? null : cli.get((String) x[2])).vendedor(((Integer) x[0]) % 2 == 0 ? "Roxana Vilca Mendoza" : "Katherine Flores Chambi")
                    .build();
            BigDecimal bruto = BigDecimal.ZERO;
            for (Object[] it : (Object[][]) x[6]) {
                int cant = (Integer) it[1];
                if (cant == 0) continue;
                Producto p = prod.get((String) it[0]);
                venta.agregar(ItemVenta.builder().producto(p).cantidad(cant).precioUnitario(p.getPrecio()).telaCodigo((String) it[2]).build());
                bruto = bruto.add(p.getPrecio().multiply(BigDecimal.valueOf(cant)));
            }
            BigDecimal desc = BigDecimal.valueOf((Integer) x[5]).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = bruto.subtract(desc);
            venta.setDescuento(desc);
            venta.setTotal(total);
            String metodo = (String) x[4];
            if (metodo.equals("Mixto")) {
                BigDecimal a = new BigDecimal("2000.00");
                metodo = "Mixto (Efectivo S/ 2,000.00 + Yape S/ " + String.format(Locale.US, "%,.2f", total.subtract(a)) + ")";
            }
            if (metodo.equals("Efectivo")) {
                BigDecimal recibido = total.setScale(-2, RoundingMode.UP);
                venta.setRecibido(recibido);
                venta.setVuelto(recibido.subtract(total));
            }
            venta.setMetodoPago(metodo);
            ventaRepository.save(venta);
        }
    }

    // ================================================================ finanzas

    private void finanzas() {
        int[][] hist = {{2025, 11, 62400, 48900, 25}, {2025, 12, 98700, 61200, 39}, {2026, 1, 54300, 45800, 21}, {2026, 2, 51800, 43900, 20},
                {2026, 3, 58600, 47200, 23}, {2026, 4, 61900, 49700, 24}, {2026, 5, 70400, 52300, 28}, {2026, 6, 66200, 51100, 26},
                {2026, 7, 79500, 57800, 32}, {2026, 8, 72300, 55400, 29}, {2026, 9, 75800, 56900, 30}};
        for (int[] h : hist) {
            resumenRepository.save(ResumenMensual.builder().anio(h[0]).mes(h[1]).ventas(BigDecimal.valueOf(h[2])).egresos(BigDecimal.valueOf(h[3])).sofas(h[4]).build());
        }
        g("2026-10-01", "Alquiler", "Alquiler de taller y tienda · octubre", 3500);
        g("2026-10-05", "Servicios", "Luz, agua e internet", 950);
        g("2026-10-08", "Transporte", "Fletes de entregas en Juliaca y Puno", 1200);
        g("2026-10-10", "Publicidad", "Campaña en redes sociales", 800);
        g("2026-10-15", "Planilla", "Planilla 1.ª quincena de octubre", 16800);
    }

    private void g(String fecha, String cat, String concepto, double monto) {
        gastoRepository.save(Gasto.builder().fecha(LocalDate.parse(fecha)).categoria(cat).concepto(concepto)
                .monto(BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP)).build());
    }

    // ================================================================ stocks y kardex

    /**
     * Fija el stock final de cada material (reservado + disponible deseado; telas = suma de colores), calcula el
     * saldo inicial del 01/10 restando los movimientos posteriores y recalcula los saldos del kardex.
     */
    private void stocksFinales() {
        for (Material m : mat.values()) {
            Material fresco = materialRepository.findById(m.getId()).orElseThrow();
            double stock;
            TipoTela tipo = TipoTela.porMaterial(fresco.getCodigo());
            if (tipo != null) {
                stock = telaRepository.findByTipo(tipo).stream().filter(t -> !t.getDescontinuada()).mapToDouble(Tela::getStock).sum();
            } else if (Boolean.TRUE.equals(fresco.getBajoPedido())) {
                stock = 0;
            } else {
                stock = r2(fresco.getReservado() + disponibleFinal.getOrDefault(fresco.getCodigo(), fresco.getMinimo() * 2));
            }
            double posteriores = movimientos.stream().filter(x -> x.getMaterial().getCodigo().equals(fresco.getCodigo()))
                    .mapToDouble(Movimiento::getEfecto).sum();
            double inicial = r2(stock - posteriores);
            if (inicial < 0) {
                stock = r2(stock - inicial);
                inicial = 0;
            }
            fresco.setStock(stock);
            materialRepository.save(fresco);
            if (inicial > 0 || stock > 0) {
                movimientoRepository.save(Movimiento.builder().material(fresco).fecha(LocalDateTime.of(2026, 10, 1, 8, 0))
                        .tipo(TipoMovimiento.ENTRADA).cantidad(inicial).referencia("INV-OCT").concepto("Saldo inicial de octubre")
                        .usuario("larocutipa").build());
            }
        }
        for (Movimiento x : movimientos) {
            x.setMaterial(materialRepository.findById(x.getMaterial().getId()).orElseThrow());
            movimientoRepository.save(x);
        }
        // Saldos corridos
        Map<Long, Double> saldo = new HashMap<>();
        List<Movimiento> todos = new ArrayList<>(movimientoRepository.findAll());
        todos.sort(Comparator.comparing(Movimiento::getFecha).thenComparing(Movimiento::getId));
        for (Movimiento x : todos) {
            double s = saldo.getOrDefault(x.getMaterial().getId(), 0d) + x.getEfecto();
            saldo.put(x.getMaterial().getId(), s);
            x.setSaldo(r2(s));
        }
        movimientoRepository.saveAll(todos);
    }

    private static double r2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
