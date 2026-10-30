/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConfiguracionController
 * Descripción: Configuracion: empresa, usuarios y roles (matriz de permisos), parametros de produccion, preferencias y perfil.
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
package pe.erp.sofas.ui.views.configuracion;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.components.*;

import java.util.*;

/** Configuracion: empresa, usuarios y roles (matriz de permisos), parametros de produccion, preferencias y perfil. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConfiguracionController implements Vista {

    private static final String[][] SECCIONES = {{"empresa", "Empresa", "BUSINESS"}, {"usuarios", "Usuarios y roles", "ADMIN_PANEL_SETTINGS"},
            {"produccion", "Producción", "BUILD"}, {"preferencias", "Preferencias", "TUNE"}, {"perfil", "Mi perfil", "ACCOUNT_CIRCLE"}};

    private final ConfiguracionService config;
    private final ParametroService parametros;
    private final ImagenService imagenes;
    private final ThemeManager tema;
    private final Session session;
    private final Overlays overlays;
    private final Toast toast;

    @FXML private VBox raiz;

    private final VBox menu = new VBox();
    private final VBox contenido = new VBox(16);
    private String seccion = "empresa";

    @FXML
    public void initialize() {
        menu.getStyleClass().addAll("card", "settings-menu");
        menu.setPrefWidth(240);
        menu.setMinWidth(220);
        menu.setMaxHeight(Region.USE_PREF_SIZE);
        ScrollPane sp = UI.scroll(contenido);
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox cuerpo = new HBox(16, menu, sp);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        raiz.getChildren().setAll(cuerpo);
    }

    @Override
    public void onShow(Intent intent) {
        if (intent.es(Intent.ACCION_ABRIR) && intent.dato() != null) seccion = intent.dato().toString();
        render();
    }

    private void render() {
        menu.getChildren().clear();
        for (String[] s : SECCIONES) {
            HBox item = new HBox(10, Ico.of(s[2], 18), UI.label(s[1]));
            item.getStyleClass().add("settings-item");
            item.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), s[0].equals(seccion));
            item.setOnMouseClicked(e -> {
                seccion = s[0];
                render();
            });
            menu.getChildren().add(item);
        }
        contenido.getChildren().setAll(switch (seccion) {
            case "usuarios" -> usuarios();
            case "produccion" -> produccion();
            case "preferencias" -> preferencias();
            case "perfil" -> perfil();
            default -> empresa();
        });
    }

    // ================================================================ empresa

    private Node empresa() {
        Empresa e = config.empresa();
        Validacion v = new Validacion();
        TextField rs = UI.text(e.getRazonSocial(), "Razón social");
        TextField nc = UI.text(e.getNombreComercial(), "Nombre comercial");
        TextField ruc = Forms.digitos(e.getRuc(), 11);
        TextField dir = UI.text(e.getDireccion(), "Dirección");
        TextField ciudad = UI.text(e.getCiudad(), "Ciudad");
        TextField tel = UI.text(e.getTelefono(), "Teléfono");
        TextField email = UI.text(e.getEmail(), "Correo");
        TextField web = UI.text(e.getWeb(), "Web");
        ImageSlot logo = new ImageSlot("Logo de la empresa", 180, 120, 12);
        logo.mostrar(e.getLogo());
        String[] rutaLogo = {e.getLogo()};
        logo.alElegir(f -> toast.intentar(() -> {
            rutaLogo[0] = imagenes.guardar(f, "logo", 600, 400);
            Imagenes.olvidar(rutaLogo[0]);
            logo.mostrar(rutaLogo[0]);
        }));
        logo.alQuitar(() -> {
            rutaLogo[0] = null;
            logo.mostrar((String) null);
        });
        FormGrid f = new FormGrid(2);
        f.nodo(v.campo("Razón social", rs, () -> Validacion.requerido(rs, "la razón social")), 1);
        f.nodo(v.campo("Nombre comercial", nc, null), 1);
        f.nodo(v.campo("RUC", ruc, () -> ruc.getText().matches("\\d{11}") ? null : "El RUC debe tener 11 dígitos."), 1);
        f.nodo(v.campo("Teléfono", tel, null), 1);
        f.nodo(v.campo("Dirección", dir, null), 1);
        f.nodo(v.campo("Ciudad", ciudad, null), 1);
        f.nodo(v.campo("Correo", email, null), 1);
        f.nodo(v.campo("Web", web, null), 1);
        Button guardar = UI.primary("Guardar", "SAVE");
        v.enlazar(guardar);
        guardar.setOnAction(x -> {
            e.setRazonSocial(rs.getText().trim());
            e.setNombreComercial(nc.getText());
            e.setRuc(ruc.getText());
            e.setDireccion(dir.getText());
            e.setCiudad(ciudad.getText());
            e.setTelefono(tel.getText());
            e.setEmail(email.getText());
            e.setWeb(web.getText());
            e.setLogo(rutaLogo[0]);
            if (toast.intentar(() -> config.guardarEmpresa(e))) toast.exito("Datos de la empresa guardados. Se usan en cotizaciones, tickets y reportes.");
        });
        HBox h = new HBox(20, logo, f);
        HBox.setHgrow(f, Priority.ALWAYS);
        return UI.card(UI.cardHeader("Empresa", "Aparece en comprobantes de prueba, cotizaciones y reportes", guardar), h);
    }

    // ================================================================ usuarios

    private Node usuarios() {
        TableView<Usuario> t = Cells.tabla("Sin usuarios.");
        t.getColumns().addAll(List.of(
                Cells.nodo("Usuario", u -> UI.hbox(10, new AvatarInitials(u.getNombre(), 30, "gradient"), new VBox(0, UI.label(u.getNombre(), "cell-title"),
                        UI.label(u.getUsername() + " · " + u.getEmail(), "cell-sub"))), 280),
                Cells.texto("Cargo", Usuario::getCargo, 160),
                Cells.badge("Rol", u -> u.getRol().nombre(), 150),
                Cells.texto("Último acceso", u -> AppClock.fmt(u.getUltimoAcceso()), 110),
                Cells.badge("Estado", u -> u.getActivo() ? "Activo" : "Inactivo", 80)));
        t.getItems().setAll(config.usuarios());
        t.setFixedCellSize(52);
        t.setPrefHeight(52 * t.getItems().size() + 44);
        t.setRowFactory(tv -> {
            TableRow<Usuario> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty() && e.getClickCount() == 2) usuario(r.getItem());
            });
            return r;
        });
        Button nuevo = UI.primary("Nuevo usuario", "PERSON_ADD");
        nuevo.setOnAction(e -> usuario(null));
        VBox tabla = new VBox(t);
        tabla.getStyleClass().addAll("card", "table-card");

        // Matriz de permisos
        GridPane m = new GridPane();
        m.setHgap(6);
        m.setVgap(4);
        List<Rol> roles = List.of(Rol.values());
        m.add(UI.label("MÓDULO", "dato-label"), 0, 0);
        for (int c = 0; c < roles.size(); c++) {
            Label l = UI.wrap(roles.get(c).nombre(), "dato-label");
            l.setMaxWidth(80);
            l.setAlignment(Pos.CENTER);
            m.add(l, c + 1, 0);
        }
        int fila = 1;
        for (Modulo mod : Modulo.values()) {
            if (!mod.enMenu()) continue;
            m.add(UI.label(mod.titulo(), "cell-title"), 0, fila);
            for (int c = 0; c < roles.size(); c++) {
                Rol rol = roles.get(c);
                Permiso p = config.permisos(rol).stream().filter(x -> x.getModulo().equals(mod.clave())).findFirst().orElse(null);
                String inicial = rol == Rol.GERENTE ? "Editar" : p == null || !p.getVer() ? "—" : p.getEditar() ? "Editar" : "Ver";
                Button nivel = new Button(inicial);
                nivel.getStyleClass().addAll("badge", "badge-" + variantePermiso(inicial), "clickable");
                nivel.setMinWidth(70);
                nivel.setDisable(rol == Rol.GERENTE);
                UI.tooltip(nivel, "Clic para cambiar: sin acceso → ver → editar");
                nivel.setOnAction(e -> {
                    String b = switch (nivel.getText()) {
                        case "—" -> "Ver";
                        case "Ver" -> "Editar";
                        default -> "—";
                    };
                    config.fijarPermiso(rol, mod.clave(), !"—".equals(b), "Editar".equals(b));
                    nivel.setText(b);
                    nivel.getStyleClass().removeAll("badge-gray", "badge-blue", "badge-green");
                    nivel.getStyleClass().add("badge-" + variantePermiso(b));
                    toast.exito(rol.nombre() + " · " + mod.titulo() + ": " + ("—".equals(b) ? "sin acceso" : b.toLowerCase()) + ".");
                });
                m.add(nivel, c + 1, fila);
                GridPane.setHalignment(nivel, javafx.geometry.HPos.CENTER);
            }
            fila++;
        }
        ScrollPane spm = new ScrollPane(m);
        spm.setFitToHeight(true);
        spm.getStyleClass().add("page-scroll");
        return new VBox(16, UI.card(UI.cardHeader("Usuarios", "Doble clic para editar · contraseñas con BCrypt", nuevo), tabla),
                UI.card(UI.cardHeader("Matriz de permisos por módulo", "El Gerente tiene acceso total"), spm));
    }

    private static String variantePermiso(String n) {
        return switch (n) {
            case "Editar" -> "green";
            case "Ver" -> "blue";
            default -> "gray";
        };
    }

    private void usuario(Usuario existente) {
        Usuario u = existente == null ? Usuario.builder().activo(true).rol(Rol.VENDEDOR).build() : existente;
        Validacion v = new Validacion();
        TextField user = UI.text(u.getUsername(), "usuario");
        user.setDisable(existente != null);
        TextField nombre = UI.text(u.getNombre(), "Nombre completo");
        TextField cargo = UI.text(u.getCargo(), "Cargo");
        TextField email = UI.text(u.getEmail(), "correo@innovasur.pe");
        ComboBox<Rol> rol = Forms.combo(List.of(Rol.values()), u.getRol(), Rol::nombre);
        PasswordField pass = new PasswordField();
        pass.setPromptText(existente == null ? "Mínimo 6 caracteres" : "Dejar vacío para no cambiar");
        CheckBox activo = new CheckBox("Usuario activo");
        activo.setSelected(u.getActivo());
        FormGrid f = new FormGrid(2);
        f.nodo(v.campo("Usuario", user, () -> user.getText() != null && user.getText().matches("[a-zA-Z0-9._]{3,30}") ? null : "De 3 a 30 caracteres, sin espacios."), 1);
        f.nodo(v.campo("Rol", rol, null), 1);
        f.nodo(v.campo("Nombre", nombre, () -> Validacion.requerido(nombre, "el nombre")), 2);
        f.nodo(v.campo("Cargo", cargo, null), 1);
        f.nodo(v.campo("Correo", email, null), 1);
        f.nodo(v.campo("Contraseña", pass, () -> existente == null && pass.getText().length() < 6 ? "Mínimo 6 caracteres."
                : existente != null && !pass.getText().isEmpty() && pass.getText().length() < 6 ? "Mínimo 6 caracteres." : null), 1);
        f.nodo(activo, 1);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary("Guardar", "SAVE");
        v.enlazar(ok);
        var h = overlays.modal(existente == null ? "Nuevo usuario" : u.getNombre(), null, f, 560, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            u.setUsername(user.getText().trim());
            u.setNombre(nombre.getText().trim());
            u.setCargo(cargo.getText());
            u.setEmail(email.getText());
            u.setRol(rol.getValue());
            u.setActivo(activo.isSelected());
            if (toast.intentar(() -> config.guardarUsuario(u, pass.getText()))) {
                h.cerrar();
                toast.exito("Usuario " + u.getUsername() + " guardado.");
                render();
            }
        });
    }

    // ================================================================ produccion

    private Node produccion() {
        GridPane mermas = UI.grid(5);
        int c = 0;
        Map<String, TextField> mermaCampos = new LinkedHashMap<>();
        for (String tipo : ParametroService.MERMAS.keySet()) {
            TextField t = Forms.decimal(parametros.merma(tipo));
            mermaCampos.put(tipo, t);
            mermas.add(UI.field(tipo + " (%)", t), c++, 0);
        }
        GridPane horas = UI.grid(6);
        c = 0;
        Map<EtapaMO, TextField> horaCampos = new LinkedHashMap<>();
        for (EtapaMO e : EtapaMO.values()) {
            TextField t = Forms.decimal(parametros.costoHora(e));
            horaCampos.put(e, t);
            horas.add(UI.field(e.nombre() + " (S/ h)", t), c++, 0);
        }
        TextField ind = Forms.decimal(parametros.indirectos());
        TextField margen = Forms.decimal(parametros.margenObjetivo());
        GridPane otros = UI.grid(4);
        otros.add(UI.field("Indirectos (%)", ind), 0, 0);
        otros.add(UI.field("Margen objetivo (%)", margen), 1, 0);
        Button guardar = UI.primary("Guardar parámetros", "SAVE");
        guardar.setOnAction(e -> {
            mermaCampos.forEach((k, t) -> parametros.fijar("merma." + k, Validacion.valor(t)));
            horaCampos.forEach((k, t) -> parametros.fijar("costoHora." + k.name(), Validacion.valor(t)));
            parametros.fijar(ParametroService.INDIRECTOS, Validacion.valor(ind));
            parametros.fijar(ParametroService.MARGEN_OBJETIVO, Validacion.valor(margen));
            toast.exito("Parámetros de producción guardados. Se aplican a nuevas líneas de ficha y alertas de margen.");
        });
        return UI.card(UI.cardHeader("Producción", "Valores por defecto para fichas técnicas y costeo", guardar),
                UI.label("Mermas por defecto", "form-section-title"), mermas,
                UI.label("Costo por hora de mano de obra", "form-section-title"), horas,
                UI.label("Costeo", "form-section-title"), otros,
                UI.wrap("Los costos por hora de cada ficha se guardan en sus operaciones; los nuevos valores se usan al crear fichas nuevas.", "cell-sub"));
    }

    // ================================================================ preferencias

    private Node preferencias() {
        HBox temas = new HBox(14, temaTile("Claro", false), temaTile("Oscuro", true));
        SegmentedControl densidad = new SegmentedControl(ThemeManager.DENSIDADES.toArray(String[]::new));
        densidad.setValor(tema.getDensidad());
        densidad.valorProperty().addListener((o, a, b) -> {
            tema.setDensidad(b);
            toast.info("Densidad de tablas: " + b.toLowerCase() + ".");
        });
        return UI.card(UI.cardHeader("Preferencias", "Se guardan en este equipo"), UI.label("Tema", "form-section-title"), temas,
                UI.label("Densidad de tablas", "form-section-title"), densidad,
                UI.label("Atajos: Ctrl+K buscar · Esc cerrar modales y paneles · Enter confirmar", "cell-sub"));
    }

    private Node temaTile(String nombre, boolean oscuro) {
        Region muestra = new Region();
        muestra.setPrefSize(160, 90);
        muestra.setStyle("-fx-background-radius: 10; -fx-background-color: " + (oscuro ? "linear-gradient(to right, #0A1226 30%, #0B1220 30%)" : "linear-gradient(to right, #14213D 30%, #F8FAFC 30%)") + ";");
        VBox v = new VBox(8, muestra, UI.label(nombre, "cell-title"));
        v.getStyleClass().add("theme-tile");
        v.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), tema.isOscuro() == oscuro);
        v.setOnMouseClicked(e -> {
            tema.setOscuro(oscuro);
            render();
        });
        return v;
    }

    // ================================================================ perfil

    private Node perfil() {
        Usuario u = session.usuario();
        PasswordField actual = new PasswordField(), nueva = new PasswordField(), repetir = new PasswordField();
        Validacion v = new Validacion();
        VBox campos = new VBox(10, v.campo("Contraseña actual", actual, () -> Validacion.requerido(actual, "tu contraseña actual")),
                v.campo("Nueva contraseña", nueva, () -> nueva.getText().length() < 6 ? "Mínimo 6 caracteres." : null),
                v.campo("Repetir", repetir, () -> !repetir.getText().equals(nueva.getText()) ? "No coincide." : null));
        v.escuchar(nueva.textProperty());
        Button cambiar = UI.primary("Cambiar contraseña", "LOCK");
        v.enlazar(cambiar);
        cambiar.setOnAction(e -> {
            if (u == null || !AuthService.verificar(actual.getText(), u.getPasswordHash())) {
                toast.error("La contraseña actual no es correcta.");
                return;
            }
            if (toast.intentar(() -> config.guardarUsuario(u, nueva.getText()))) {
                toast.exito("Contraseña actualizada.");
                actual.clear();
                nueva.clear();
                repetir.clear();
            }
        });
        return UI.card(UI.hbox(14, new AvatarInitials(session.nombre(), 60, "gradient"), new VBox(2, UI.label(session.nombre(), "card-title"),
                        UI.label(session.cargo() + " · " + session.rol().nombre() + " · usuario " + session.username(), "cell-sub"))),
                UI.label("Cambiar contraseña", "form-section-title"), campos, UI.hbox(8, UI.spacer(), cambiar));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
