/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CarritoViewController
 * Descripción: Archivo fuente Java de la clase CarritoViewController.
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
package pe.erp.sofas.ui.views.carrito;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.Intent;
import pe.erp.sofas.core.Session;
import pe.erp.sofas.core.Toast;
import pe.erp.sofas.core.Vista;
import pe.erp.sofas.model.CarritoItem;
import pe.erp.sofas.model.Cliente;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.components.UI;
import pe.erp.sofas.ui.three.Viewer3D;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class CarritoViewController implements Vista.ConCiclo {

    private final GestorCarritoService gestorCarrito;
    private final CalculoPrecioService precioService;
    private final GeneradorPDFService generadorPDF;
    private final CreadorPedidoService creadorPedido;
    private final Session session;
    private final Toast toast;

    private VBox raiz;
    private TableView<CarritoItem> tabla;
    private Label lblTotal;
    private Label lblCantidad;
    private Button btnCrearPedidos;
    private Button btnVaciar;
    private Label lblVacio;

    private void inicializar() {
        raiz = new VBox(15);
        raiz.setPadding(new Insets(20));
        raiz.setStyle("-fx-background-color: #f5f5f5;");

        // Título
        Label titulo = UI.label("Carrito de Compras", "title");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Tabla de items
        tabla = crearTabla();
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Panel de totales y acciones
        HBox panelAcciones = crearPanelAcciones();

        // Label vacío (oculto inicialmente)
        lblVacio = new Label("Carrito vacío - Diseña sofás en el Configurador o agrega desde Comparador");
        lblVacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
        lblVacio.setAlignment(Pos.CENTER);
        VBox.setVgrow(lblVacio, Priority.ALWAYS);
        lblVacio.setVisible(false);

        raiz.getChildren().addAll(titulo, tabla, lblVacio, panelAcciones);
    }

    private TableView<CarritoItem> crearTabla() {
        TableView<CarritoItem> tabla = new TableView<>();
        tabla.setStyle("-fx-font-size: 11px;");

        // Columna: Configuración
        TableColumn<CarritoItem, String> colConfig = new TableColumn<>("Configuración");
        colConfig.setCellValueFactory(param ->
            new javafx.beans.property.SimpleStringProperty(
                param.getValue().getConfiguracion().getNombre()
            )
        );
        colConfig.setPrefWidth(200);

        // Columna: Cantidad
        TableColumn<CarritoItem, Integer> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(param ->
            new javafx.beans.property.SimpleObjectProperty<>(param.getValue().getCantidad())
        );
        colCantidad.setPrefWidth(80);

        // Columna: Precio Unitario
        TableColumn<CarritoItem, String> colPrecio = new TableColumn<>("Precio Unit.");
        colPrecio.setCellValueFactory(param -> {
            BigDecimal precio = precioService.calcularPrecio(
                param.getValue().getConfiguracion().toConfigSofa()
            );
            return new javafx.beans.property.SimpleStringProperty("S/ " + precio);
        });
        colPrecio.setPrefWidth(100);

        // Columna: Total
        TableColumn<CarritoItem, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(param -> {
            BigDecimal precioUnit = precioService.calcularPrecio(
                param.getValue().getConfiguracion().toConfigSofa()
            );
            BigDecimal total = precioUnit.multiply(BigDecimal.valueOf(param.getValue().getCantidad()));
            return new javafx.beans.property.SimpleStringProperty("S/ " + total);
        });
        colTotal.setPrefWidth(100);

        // Columna: Acciones
        TableColumn<CarritoItem, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory(param -> new TableCell<CarritoItem, Void>() {
            private final Button btnQuitar = UI.button("Quitar", null, "btn-sm");
            private final Button btnEditar = UI.button("Editar", null, "btn-sm");

            {
                btnQuitar.setOnAction(event -> {
                    CarritoItem item = getTableView().getItems().get(getIndex());
                    gestorCarrito.removerDelCarrito(session.usuario(), item.getId());
                    actualizarCarrito();
                    toast.advertencia("Item removido del carrito");
                });

                btnEditar.setOnAction(event -> {
                    CarritoItem item = getTableView().getItems().get(getIndex());
                    mostrarDialogoEditar(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(btnEditar, btnQuitar);
                    setGraphic(hbox);
                }
            }
        });
        colAcciones.setPrefWidth(120);

        tabla.getColumns().addAll(colConfig, colCantidad, colPrecio, colTotal, colAcciones);
        return tabla;
    }

    private HBox crearPanelAcciones() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: white;");
        panel.setAlignment(Pos.CENTER_RIGHT);

        // Panel de totales (izquierda)
        VBox totales = crearPanelTotales();
        HBox.setHgrow(totales, Priority.ALWAYS);

        // Botones (derecha)
        btnVaciar = UI.button("Vaciar Carrito", null, "btn-danger");
        btnVaciar.setOnAction(e -> confirmarVaciar());

        btnCrearPedidos = UI.button("Crear Pedidos", null, "btn-primary");
        btnCrearPedidos.setOnAction(e -> crearPedidos());

        Button btnPDF = UI.button("Descargar PDF", null, "btn-secondary");
        btnPDF.setOnAction(e -> descargarPDF());

        HBox botones = new HBox(10);
        botones.getChildren().addAll(btnVaciar, btnCrearPedidos, btnPDF);
        botones.setAlignment(Pos.CENTER_RIGHT);

        panel.getChildren().addAll(totales, botones);
        return panel;
    }

    private VBox crearPanelTotales() {
        VBox panel = new VBox(8);

        HBox fila1 = new HBox(10);
        fila1.setAlignment(Pos.CENTER_LEFT);
        Label lbl1 = new Label("Items:");
        lbl1.setStyle("-fx-font-weight: bold;");
        lblCantidad = new Label("0");
        fila1.getChildren().addAll(lbl1, lblCantidad);

        HBox fila2 = new HBox(10);
        fila2.setAlignment(Pos.CENTER_LEFT);
        Label lbl2 = new Label("Total:");
        lbl2.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        lblTotal = new Label("S/ 0.00");
        lblTotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        fila2.getChildren().addAll(lbl2, lblTotal);

        panel.getChildren().addAll(fila1, fila2);
        return panel;
    }

    private void actualizarCarrito() {
        List<CarritoItem> items = gestorCarrito.listarItems(session.usuario());
        tabla.getItems().setAll(items);

        if (items.isEmpty()) {
            tabla.setVisible(false);
            lblVacio.setVisible(true);
            btnCrearPedidos.setDisable(true);
            btnVaciar.setDisable(true);
            lblCantidad.setText("0");
            lblTotal.setText("S/ 0.00");
        } else {
            tabla.setVisible(true);
            lblVacio.setVisible(false);
            btnCrearPedidos.setDisable(false);
            btnVaciar.setDisable(false);
            actualizarTotales();
        }
    }

    private void actualizarTotales() {
        GestorCarritoService.DetalleCarrito detalle = gestorCarrito.obtenerDetalleCarrito(session.usuario());
        lblCantidad.setText(detalle.cantidadItems() + " items (" + detalle.cantidadTotal() + " uds)");
        lblTotal.setText("S/ " + detalle.precioTotal());
    }

    private void mostrarDialogoEditar(CarritoItem item) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Editar Cantidad");
        dialog.setHeaderText("Cantidad para: " + item.getConfiguracion().getNombre());

        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, item.getCantidad());
        Spinner<Integer> spinner = new Spinner<>(factory);
        spinner.setPrefWidth(100);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(new Label("Cantidad:"), spinner);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return spinner.getValue();
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(cantidad -> {
            gestorCarrito.actualizarCantidad(session.usuario(), item.getId(), cantidad);
            actualizarCarrito();
            toast.exito("Cantidad actualizada");
        });
    }

    private void confirmarVaciar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Vaciar Carrito");
        alert.setHeaderText("¿Vaciar carrito?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            gestorCarrito.vaciarCarrito(session.usuario());
            actualizarCarrito();
            toast.exito("Carrito vaciado");
        }
    }

    private void crearPedidos() {
        List<CarritoItem> items = gestorCarrito.listarItems(session.usuario());
        if (items.isEmpty()) {
            toast.advertencia("Carrito vacío");
            return;
        }

        // Obtener cliente actual (simplificado: usar cliente por defecto o solicitar)
        // En un sistema real, esto vendría de la sesión del cliente
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Cliente desde Carrito");

        try {
            var pedidos = creadorPedido.crearPedidosDesdeCarrito(session.usuario(), cliente, true);
            toast.exito("Se crearon " + pedidos.size() + " pedido(s)");
            actualizarCarrito();
            // Redireccionar a PEDIDOS (si hay navegación disponible)
        } catch (Exception e) {
            toast.error("Error: " + e.getMessage());
        }
    }

    private void descargarPDF() {
        List<CarritoItem> items = gestorCarrito.listarItems(session.usuario());
        if (items.isEmpty()) {
            toast.advertencia("Carrito vacío");
            return;
        }

        try {
            // Generar PDF con todos los items
            StringBuilder contenido = new StringBuilder("CARRITO DE COMPRAS\n\n");
            for (CarritoItem item : items) {
                contenido.append("- ").append(item.getConfiguracion().getNombre())
                        .append(" x").append(item.getCantidad()).append("\n");
            }

            toast.info("PDF generado (feature en desarrollo)");
        } catch (Exception e) {
            toast.error("Error generando PDF: " + e.getMessage());
        }
    }

    @Override
    public void onShow(Intent intent) {
        if (raiz == null) {
            inicializar();
        }
        actualizarCarrito();
    }

    @Override
    public void onHide() {
        // Limpiar si es necesario
    }

    public VBox getRaiz() {
        return raiz;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
