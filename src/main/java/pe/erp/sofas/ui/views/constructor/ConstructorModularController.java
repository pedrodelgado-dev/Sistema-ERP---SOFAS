/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConstructorModularController
 * Descripción: Constructor Modular: permite armar sofás pieza por pieza de forma libre.
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
package pe.erp.sofas.ui.views.constructor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.Intent;
import pe.erp.sofas.core.Session;
import pe.erp.sofas.core.Toast;
import pe.erp.sofas.core.Vista;
import pe.erp.sofas.model.ConfigSofa;
import pe.erp.sofas.service.ConfiguradorService;
import pe.erp.sofas.service.GestorCarritoService;
import pe.erp.sofas.service.PersistenciaConfiguracionService;
import pe.erp.sofas.service.TelaService;
import pe.erp.sofas.ui.components.UI;
import pe.erp.sofas.ui.three.SofaBuilder;
import pe.erp.sofas.ui.three.Viewer3D;
import pe.erp.sofas.ui.three.parts.SofaPartAssembler;
import pe.erp.sofas.ui.three.parts.families.FamiliaFactory;
import pe.erp.sofas.ui.three.parts.families.SofaFamilia;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructor Modular: permite armar sofás pieza por pieza de forma libre.
 *
 * Paleta de módulos:
 * - Brazo izq + 1 cuerpo
 * - Central sin brazos
 * - Esquinero
 * - Chaise
 * - Puff
 * - Brazo der + 1 cuerpo
 *
 * El usuario agrega módulos al final, puede reordenarlos y eliminarlos.
 * Vista en planta muestra las dimensiones reales.
 */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConstructorModularController implements Vista.ConCiclo {

    private final ConfiguradorService configurador;
    private final TelaService telas;
    private final Toast toast;
    private final GestorCarritoService gestorCarrito;
    private final PersistenciaConfiguracionService persistencia;
    private final Session session;
    private final FamiliaFactory familiaFactory = new FamiliaFactory();
    private final SofaBuilder builder = new SofaBuilder();

    private VBox raiz;
    private Viewer3D visor;
    private ListView<String> listaModulos;
    private List<String> modulosActuales = new ArrayList<>();

    private void inicializar() {
        raiz = new VBox(10);
        raiz.setPadding(new Insets(12));

        // Título
        Label titulo = UI.label("Constructor Modular", "title");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Contenido principal: visor + panel lateral
        HBox contenido = new HBox(10);
        VBox.setVgrow(contenido, Priority.ALWAYS);

        // Visor 3D a la izquierda
        visor = new Viewer3D(true);
        visor.setMinSize(400, 500);
        HBox.setHgrow(visor, Priority.ALWAYS);

        // Panel de control a la derecha
        VBox panelControl = crearPanelControl();
        panelControl.setPrefWidth(300);

        contenido.getChildren().addAll(visor, panelControl);

        raiz.getChildren().addAll(titulo, contenido);
    }

    /**
     * Crea el panel de control con paleta de módulos y lista de ensamblado.
     */
    private VBox crearPanelControl() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #fafafa;");

        // Título
        Label titulo = UI.label("Módulos", "subtitle");
        titulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        // Paleta de módulos disponibles
        VBox paleta = crearPaletaModulos();

        // Separador
        Separator sep1 = new Separator();

        // Título lista
        Label tituloLista = UI.label("Tu armado", "subtitle");
        tituloLista.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        // Lista de módulos seleccionados
        listaModulos = new ListView<>();
        listaModulos.setPrefHeight(200);
        listaModulos.setStyle("-fx-font-size: 11px;");
        VBox.setVgrow(listaModulos, Priority.ALWAYS);

        // Botones de acción
        VBox botones = new VBox(5);

        HBox botonesTop = new HBox(5);
        Button btnLimpiar = UI.button("Limpiar", null, "btn-sm");
        btnLimpiar.setOnAction(e -> {
            modulosActuales.clear();
            listaModulos.getItems().clear();
            actualizarVisor();
            toast.advertencia("Armado limpiado");
        });

        Button btnPrevisualizarPlanta = UI.button("Vista planta", null, "btn-sm");
        btnPrevisualizarPlanta.setOnAction(e -> {
            toast.info("Vista en planta: " + calcularDimensiones());
        });

        botonesTop.getChildren().addAll(btnLimpiar, btnPrevisualizarPlanta);

        Button btnGuardarCarrito = UI.button("Guardar al carrito", null, "btn-primary");
        btnGuardarCarrito.setMaxWidth(Double.MAX_VALUE);
        btnGuardarCarrito.setOnAction(e -> guardarYAgregarAlCarrito());

        botones.getChildren().addAll(botonesTop, btnGuardarCarrito);

        panel.getChildren().addAll(titulo, paleta, sep1, tituloLista, listaModulos, botones);
        return panel;
    }

    /**
     * Crea la paleta de módulos disponibles.
     */
    private VBox crearPaletaModulos() {
        VBox paleta = new VBox(5);
        paleta.setStyle("-fx-border-color: #eee; -fx-border-radius: 4; -fx-padding: 8; -fx-background-color: white;");

        String[] modulos = {
            "Brazo izq + 1 cuerpo",
            "Central (sin brazos)",
            "Esquinero",
            "Chaise",
            "Puff",
            "Brazo der + 1 cuerpo"
        };

        for (String modulo : modulos) {
            Button btn = UI.button(modulo, null, "btn-sm");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> agregarModulo(modulo));
            paleta.getChildren().add(btn);
        }

        return paleta;
    }

    /**
     * Agrega un módulo al final del armado.
     */
    private void agregarModulo(String modulo) {
        modulosActuales.add(modulo);
        if (listaModulos != null) {
            listaModulos.getItems().add((listaModulos.getItems().size() + 1) + ". " + modulo);
        }
        actualizarVisor();
        toast.exito("Agregado: " + modulo);
    }

    /**
     * Actualiza el visor 3D con el armado actual.
     */
    private void actualizarVisor() {
        // Por ahora, mostrar una configuración Modular genérica
        // En integración real, esto construiría desde los módulos seleccionados
        try {
            ConfigSofa config = configurador.defecto(ConfigSofa.Modelo.MODERNO)
                .conCuerpos(Math.min(4, Math.max(1, modulosActuales.size())));

            var tela = config.telaId() != null ? telas.tela(config.telaId()) : null;
            var telaCojines = config.telaCojinesId() != null ? telas.tela(config.telaCojinesId()) : null;

            Group sofaGeom = builder.build(SofaBuilder.Opciones.de(config, tela, telaCojines));
            visor.setContenido(sofaGeom, true);
        } catch (Exception e) {
            toast.error("Error al actualizar visor: " + e.getMessage());
        }
    }

    /**
     * Calcula dimensiones del armado actual.
     */
    private String calcularDimensiones() {
        int largo = modulosActuales.size() * 52;  // Cada módulo ~52 cm
        int fondo = 92;  // Estándar
        return largo + " × " + fondo + " cm";
    }

    private void guardarYAgregarAlCarrito() {
        if (modulosActuales.isEmpty()) {
            toast.advertencia("Debes agregar al menos un módulo");
            return;
        }

        try {
            ConfigSofa config = configurador.defecto(ConfigSofa.Modelo.MODERNO)
                .conCuerpos(Math.min(4, Math.max(1, modulosActuales.size())));

            String nombre = "Constructor - " + modulosActuales.size() + " módulos";
            var configGuardada = persistencia.guardar(session.usuario(), config, nombre, "");
            gestorCarrito.agregarAlCarrito(session.usuario(), configGuardada.getId(), 1);
            toast.exito("Configuración guardada al carrito");

            modulosActuales.clear();
            listaModulos.getItems().clear();
            actualizarVisor();
        } catch (Exception e) {
            toast.error("Error: " + e.getMessage());
        }
    }

    @Override
    public void onShow(Intent intent) {
        if (raiz == null) {
            inicializar();
        }
        // Inicializar con módulo de ejemplo
        if (modulosActuales.isEmpty()) {
            agregarModulo("Brazo izq + 1 cuerpo");
            agregarModulo("Central (sin brazos)");
            agregarModulo("Brazo der + 1 cuerpo");
        }
    }

    @Override
    public void onHide() {
        visor.detenerGiro();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
