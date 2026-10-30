/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ComparadorController
 * Descripción: Comparador A/B/C: vista de 3 sofás lado a lado con sincronización de rotación.
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
package pe.erp.sofas.ui.views.comparador;

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
import pe.erp.sofas.ui.AppWindow;
import pe.erp.sofas.ui.components.UI;
import pe.erp.sofas.ui.three.SofaBuilder;
import pe.erp.sofas.ui.three.Viewer3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Comparador A/B/C: vista de 3 sofás lado a lado con sincronización de rotación.
 *
 * Permite comparar hasta 3 configuraciones diferentes en tiempo real.
 * Al rotar uno, todos los demás rotan al mismo tiempo.
 * Útil para decidir entre opciones de color, tamaño, etc.
 */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ComparadorController implements Vista.ConCiclo {

    private final ConfiguradorService configurador;
    private final TelaService telas;
    private final Toast toast;
    private final AppWindow appWindow;
    private final GestorCarritoService gestorCarrito;
    private final PersistenciaConfiguracionService persistencia;
    private final Session session;

    private final SofaBuilder builder = new SofaBuilder();
    private final List<Viewer3D> visores = new ArrayList<>();
    private final ConfigSofa[] configs = new ConfigSofa[3];

    private VBox raiz;
    private Label[] labelConfiguraciones;

    private void inicializar() {
        raiz = new VBox(10);
        raiz.setPadding(new Insets(12));
        raiz.setStyle("-fx-background-color: #f5f5f5;");

        // Título
        Label titulo = UI.label("Comparador A/B/C", "title");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Contenedor de 3 visores lado a lado
        HBox comparador = crearComparador();
        VBox.setVgrow(comparador, Priority.ALWAYS);

        // Panel de información (tabla comparativa)
        VBox infoPanel = crearPanelInfo();

        raiz.getChildren().addAll(titulo, comparador, infoPanel);
    }

    /**
     * Crea 3 visores sincronizados lado a lado.
     */
    private HBox crearComparador() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 8;");

        labelConfiguraciones = new Label[3];

        for (int i = 0; i < 3; i++) {
            VBox visorPanel = new VBox(5);
            visorPanel.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 10;");

            // Etiqueta
            labelConfiguraciones[i] = UI.label("Opción " + (i + 1), "card");
            labelConfiguraciones[i].setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            // Visor 3D
            Viewer3D visor = new Viewer3D(true);
            visor.setMinSize(300, 400);
            visores.add(visor);

            // Sincronizar rotación: cuando uno rota, todos rotan
            final int index = i;
            visor.yawProperty().addListener((obs, oldV, newV) -> {
                for (int j = 0; j < 3; j++) {
                    if (j != index) {
                        visores.get(j).yawProperty().setValue(newV);
                    }
                }
            });
            visor.pitchProperty().addListener((obs, oldV, newV) -> {
                for (int j = 0; j < 3; j++) {
                    if (j != index) {
                        visores.get(j).pitchProperty().setValue(newV);
                    }
                }
            });

            // Botón agregar al carrito
            Button btnAgregarCarrito = UI.button("Agregar al carrito", null, "btn-primary");
            btnAgregarCarrito.setMaxWidth(Double.MAX_VALUE);
            btnAgregarCarrito.setOnAction(e -> agregarAlCarrito(index));

            visorPanel.getChildren().addAll(labelConfiguraciones[i], visor, btnAgregarCarrito);
            HBox.setHgrow(visorPanel, Priority.ALWAYS);
            hbox.getChildren().add(visorPanel);

            // Configuración inicial
            if (i == 0) {
                configs[i] = configurador.defecto(ConfigSofa.Modelo.MODERNO);
            } else {
                configs[i] = null;  // Vacío hasta que se cargue
            }
        }

        return hbox;
    }

    /**
     * Panel de información con tabla comparativa.
     */
    private VBox crearPanelInfo() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #fafafa;");

        Label titulo = UI.label("Comparativa", "subtitle");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Tabla de comparación simple (será expandida en integración real)
        TableView<ComparisonRow> tabla = new TableView<>();
        tabla.setMinHeight(150);
        tabla.setMaxHeight(300);

        TableColumn<ComparisonRow, String> propiedadCol = new TableColumn<>("Propiedad");
        propiedadCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().propiedad));
        propiedadCol.setPrefWidth(100);

        TableColumn<ComparisonRow, String> opcion1Col = new TableColumn<>("Opción A");
        opcion1Col.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().opcion1));
        opcion1Col.setPrefWidth(120);

        TableColumn<ComparisonRow, String> opcion2Col = new TableColumn<>("Opción B");
        opcion2Col.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().opcion2));
        opcion2Col.setPrefWidth(120);

        TableColumn<ComparisonRow, String> opcion3Col = new TableColumn<>("Opción C");
        opcion3Col.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().opcion3));
        opcion3Col.setPrefWidth(120);

        tabla.getColumns().addAll(propiedadCol, opcion1Col, opcion2Col, opcion3Col);

        // Datos de ejemplo
        tabla.getItems().addAll(
            new ComparisonRow("Modelo", "Moderno", "-", "-"),
            new ComparisonRow("Cuerpos", "3", "-", "-"),
            new ComparisonRow("Largo", "210 cm", "-", "-"),
            new ComparisonRow("Tela", "Chenille gris", "-", "-"),
            new ComparisonRow("Precio", "S/ 3,500", "-", "-")
        );

        panel.getChildren().addAll(titulo, tabla);
        return panel;
    }

    /**
     * Carga una configuración en una posición específica (A, B, o C).
     */
    public void cargarConfiguracion(int posicion, ConfigSofa config) {
        if (posicion < 0 || posicion >= 3) return;

        configs[posicion] = config;
        var tela = config.telaId() != null ? telas.tela(config.telaId()) : null;
        var telaCojines = config.telaCojinesId() != null ? telas.tela(config.telaCojinesId()) : null;

        Group sofaGeom = builder.build(SofaBuilder.Opciones.de(config, tela, telaCojines));
        visores.get(posicion).setContenido(sofaGeom, true);

        // Actualizar etiqueta
        labelConfiguraciones[posicion].setText(config.modelo().nombre());
    }

    @Override
    public void onShow(Intent intent) {
        if (raiz == null) {
            inicializar();
            // Cargar primera configuración después de inicializar
            if (configs[0] != null && labelConfiguraciones != null) {
                cargarConfiguracion(0, configs[0]);
            }
        }
    }

    @Override
    public void onHide() {
        // Limpiar recursos
        visores.forEach(Viewer3D::detenerGiro);
    }

    private void agregarAlCarrito(int posicion) {
        if (configs[posicion] == null) {
            toast.advertencia("Opción " + (posicion + 1) + " no configurada");
            return;
        }

        try {
            String nombre = "Comparador - " + configs[posicion].modelo().nombre();
            var configGuardada = persistencia.guardar(session.usuario(), configs[posicion], nombre, "");
            gestorCarrito.agregarAlCarrito(session.usuario(), configGuardada.getId(), 1);
            toast.exito("Agregado al carrito: " + nombre);
        } catch (Exception e) {
            toast.error("Error: " + e.getMessage());
        }
    }

    /**
     * Fila de la tabla comparativa.
     */
    public static class ComparisonRow {
        public String propiedad;
        public String opcion1;
        public String opcion2;
        public String opcion3;

        public ComparisonRow(String propiedad, String opcion1, String opcion2, String opcion3) {
            this.propiedad = propiedad;
            this.opcion1 = opcion1;
            this.opcion2 = opcion2;
            this.opcion3 = opcion3;
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
