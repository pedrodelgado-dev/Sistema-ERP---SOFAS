/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ModelosController
 * Descripción: Catalogo de modelos de sofa: tarjetas o tabla, filtros, detalle lateral y formulario.
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
package pe.erp.sofas.ui.views.modelos;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.Forma;
import pe.erp.sofas.model.Producto;
import pe.erp.sofas.service.BusquedaService;
import pe.erp.sofas.service.ImagenService;
import pe.erp.sofas.service.ProductoService;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.Render3D;

import java.math.BigDecimal;
import java.util.*;

/** Catalogo de modelos de sofa: tarjetas o tabla, filtros, detalle lateral y formulario. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ModelosController implements Vista {

    private static final List<String> ESTADOS = List.of("Todos los estados", "Disponible", "Stock bajo", "A pedido");
    private static final List<String> ORDENES = List.of("Código", "Precio: menor a mayor", "Precio: mayor a menor", "Nombre", "Stock");

    private final ProductoService productoService;
    private final ImagenService imagenService;
    private final Overlays overlays;
    private final Toast toast;
    private final ConfirmDialog confirm;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private SearchField buscador;
    private final ChipFilterBar chips = new ChipFilterBar();
    private ComboBox<String> cmbEstado;
    private ComboBox<String> cmbOrden;
    private SegmentedControl vista;
    private final VBox resultados = new VBox(12);
    private final Label conteo = UI.label("", "muted");
    private Overlays.Handle panel;

    @FXML
    public void initialize() {
        buscador = new SearchField("Buscar por nombre, código o color");
        buscador.setPrefWidth(300);
        buscador.textProperty().addListener((o, a, b) -> render());
        cmbEstado = Forms.combo(ESTADOS, ESTADOS.get(0), s -> s);
        cmbEstado.setPrefWidth(170);
        cmbEstado.valueProperty().addListener((o, a, b) -> render());
        cmbOrden = Forms.combo(ORDENES, ORDENES.get(0), s -> "Ordenar: " + s);
        cmbOrden.setPrefWidth(220);
        cmbOrden.valueProperty().addListener((o, a, b) -> render());
        vista = SegmentedControl.conIconos(new String[]{"Tarjetas", "Tabla"}, new String[]{"VIEW_MODULE", "TABLE_VIEW"});
        vista.setValor(Prefs.str(Prefs.VISTA_MODELOS, "Tarjetas"));
        vista.valorProperty().addListener((o, a, b) -> {
            Prefs.setStr(Prefs.VISTA_MODELOS, b);
            render();
        });
        Button nuevo = UI.primary("Registrar modelo", "ADD");
        nuevo.setOnAction(e -> formulario(null));
        HBox barra = UI.hbox(10, buscador, cmbEstado, cmbOrden, UI.spacer(), vista, nuevo);
        chips.seleccionProperty().addListener((o, a, b) -> render());
        ScrollPane sp = UI.scroll(resultados);
        VBox.setVgrow(sp, Priority.ALWAYS);
        raiz.getChildren().setAll(barra, UI.hbox(10, chips, UI.spacer(), conteo), sp);
    }

    @Override
    public void onShow(Intent intent) {
        List<Producto> todos = productoService.listar();
        List<ChipFilterBar.Chip> cs = new ArrayList<>();
        cs.add(new ChipFilterBar.Chip("*", "Todos", todos.size()));
        for (String c : Producto.CATEGORIAS) {
            long n = todos.stream().filter(p -> p.getCategoria().equals(c)).count();
            if (n > 0) cs.add(new ChipFilterBar.Chip(c, c, n));
        }
        chips.setChips(cs);
        render();
        if (intent.es(Intent.ACCION_NUEVO)) formulario(null);
        else if (intent.es(Intent.ACCION_ABRIR) && intent.dato() != null) {
            productoService.porCodigo(intent.dato().toString()).ifPresent(this::detalle);
        }
    }

    private List<Producto> filtrados() {
        String q = BusquedaService.normalizar(buscador.getText());
        String cat = chips.getSeleccion();
        String estado = cmbEstado.getValue();
        Comparator<Producto> orden = switch (cmbOrden.getValue()) {
            case "Precio: menor a mayor" -> Comparator.comparing(Producto::getPrecio);
            case "Precio: mayor a menor" -> Comparator.comparing(Producto::getPrecio).reversed();
            case "Nombre" -> Comparator.comparing(Producto::getNombre);
            case "Stock" -> Comparator.comparing(Producto::getStock).reversed();
            default -> Comparator.comparing(Producto::getCodigo);
        };
        return productoService.listar().stream()
                .filter(p -> cat == null || cat.equals("*") || p.getCategoria().equals(cat))
                .filter(p -> estado.equals(ESTADOS.get(0)) || p.getEstado().equals(estado))
                .filter(p -> q.isEmpty() || BusquedaService.normalizar(p.getNombre() + " " + p.getCodigo() + " " + p.getColor() + " " + p.getMaterial()).contains(q))
                .sorted(orden).toList();
    }

    private void render() {
        if (vista == null) return;
        List<Producto> l = filtrados();
        conteo.setText(l.size() + (l.size() == 1 ? " modelo" : " modelos"));
        if (l.isEmpty()) {
            Button limpiar = UI.secondary("Limpiar filtros", "FILTER_LIST");
            limpiar.setOnAction(e -> {
                buscador.setText("");
                cmbEstado.setValue(ESTADOS.get(0));
                chips.seleccionar("*");
            });
            resultados.getChildren().setAll(UI.empty("WEEKEND", "Ningún modelo coincide", "Prueba con otra categoría o borra la búsqueda.", limpiar));
            return;
        }
        if ("Tabla".equals(vista.getValor())) {
            resultados.getChildren().setAll(tabla(l));
        } else {
            TilePane grid = new TilePane(16, 16);
            grid.setPrefTileWidth(258);
            grid.setTileAlignment(Pos.TOP_LEFT);
            for (Producto p : l) grid.getChildren().add(tarjeta(p));
            resultados.getChildren().setAll(grid);
        }
    }

    private ImageSlot foto(Producto p, int indice, double w, double h, boolean editable) {
        ImageSlot s = new ImageSlot("Foto: " + p.getNombre(), w, h, 12);
        if (indice == 0 && p.getImagenPrincipal() == null) s.respaldo(() -> Render3D.modelo(p));
        if (!editable) s.soloLectura();
        else {
            s.alElegir(f -> toast.intentar(() -> {
                String ruta = imagenService.guardar(f, p.getCodigo() + "-" + indice, 1200, 800);
                Imagenes.olvidar(ruta);
                productoService.fijarImagen(p.getId(), indice, ruta);
                toast.exito("Foto " + (indice == 0 ? "principal" : indice + 1) + " de " + p.getNombre() + " guardada.");
                refrescarDetalle(p.getId());
            }));
            s.alQuitar(() -> toast.intentar(() -> {
                productoService.fijarImagen(p.getId(), indice, null);
                toast.info("Foto quitada.");
                refrescarDetalle(p.getId());
            }));
        }
        s.mostrar(p.getListaImagenes().get(indice));
        return s;
    }

    private void refrescarDetalle(Long id) {
        render();
        if (panel != null) {
            panel.cerrar();
            detalle(productoService.producto(id));
        }
    }

    private Node tarjeta(Producto p) {
        ImageSlot img = foto(p, 0, 258, 168, false);
        img.setMouseTransparent(true);
        VBox cuerpo = new VBox(5,
                UI.hbox(6, UI.codigo(p.getCodigo()), UI.spacer(), new StatusBadge(p.getEstado())),
                UI.label(p.getNombre(), "product-name"),
                UI.label(p.getCategoria() + " · " + p.getMedidas(), "cell-sub"),
                UI.hbox(6, UI.label(Money.fmt(p.getPrecio()), "product-price"), UI.spacer(),
                        UI.label("Stock " + p.getStock(), "cell-sub")));
        cuerpo.getStyleClass().add("product-card-body");
        VBox card = new VBox(img, cuerpo);
        card.getStyleClass().addAll("card", "product-card", "clickable");
        card.setFocusTraversable(true);
        card.setOnMouseClicked(e -> detalle(p));
        card.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) detalle(p);
        });
        return card;
    }

    private Node tabla(List<Producto> l) {
        TableView<Producto> t = Cells.tabla("Sin modelos.");
        TableColumn<Producto, String> precio = Cells.derecha("Precio", p -> Money.fmt(p.getPrecio()), 100, "cell-strong");
        precio.setComparator(Comparator.comparing(Money::parse));
        TableColumn<Producto, String> stock = Cells.derecha("Stock", p -> String.valueOf(p.getStock()), 60);
        stock.setComparator(Comparator.comparingInt(Integer::parseInt));
        t.getColumns().addAll(List.of(
                Cells.nodo("", p -> {
                    ImageSlot s = foto(p, 0, 54, 38, false);
                    s.setMouseTransparent(true);
                    return s;
                }, 70),
                Cells.texto("Código", Producto::getCodigo, 70, "cell-mono"),
                Cells.texto("Modelo", Producto::getNombre, 200, "cell-strong"),
                Cells.texto("Categoría", Producto::getCategoria, 110, "cell-muted"),
                Cells.texto("Medidas", Producto::getMedidas, 140),
                Cells.texto("Tela", p -> p.getMaterial() + " " + p.getColor(), 150, "cell-muted"),
                Cells.derecha("Costo", p -> Money.fmt(p.getCosto()), 90, "cell-muted"), precio,
                Cells.derecha("Margen", p -> p.getMargen() + " %", 70), stock,
                Cells.badge("Estado", Producto::getEstado, 100)));
        t.getItems().setAll(l);
        t.setFixedCellSize(54);
        t.setPrefHeight(54 * l.size() + 44);
        t.setRowFactory(tv -> {
            TableRow<Producto> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty() && e.getClickCount() >= 1) detalle(r.getItem());
            });
            return r;
        });
        VBox card = new VBox(t);
        card.getStyleClass().addAll("card", "table-card");
        return card;
    }

    // ================================================================ detalle

    private void detalle(Producto p) {
        VBox cuerpo = new VBox(16);
        ImageSlot principal = foto(p, 0, 452, 270, true);
        HBox miniaturas = new HBox(10);
        for (int i = 1; i <= 4; i++) miniaturas.getChildren().add(foto(p, i, 105, 72, true));
        cuerpo.getChildren().addAll(principal, miniaturas,
                UI.wrap("Arrastra fotos a los recuadros o haz clic para elegirlas. Clic derecho: reemplazar o quitar.", "cell-sub"));

        GridPane datos = UI.grid(3);
        datos.add(UI.dato("Categoría", p.getCategoria()), 0, 0);
        datos.add(UI.dato("Material", p.getMaterial()), 1, 0);
        datos.add(UI.dato("Color", p.getColor()), 2, 0);
        datos.add(UI.dato("Medidas", p.getMedidas()), 0, 1);
        datos.add(UI.dato("Cuerpos", String.valueOf(p.getCuerpos())), 1, 1);
        datos.add(UI.dato("Stock", p.getStock() + " · " + p.getEstado()), 2, 1);
        datos.add(UI.dato("Costo", Money.fmt(p.getCosto())), 0, 2);
        datos.add(UI.dato("Precio", Money.fmt(p.getPrecio())), 1, 2);
        datos.add(UI.dato("Margen", p.getMargen() + " %"), 2, 2);
        cuerpo.getChildren().addAll(UI.label("Ficha del modelo", "card-title"), datos);

        // Costo estandar de la ficha aprobada
        var ce = productoService.costoEstandar(p);
        VBox estandar = new VBox(8);
        estandar.getStyleClass().add("card-flat");
        if (ce == null) {
            Button crear = UI.button("Ir a Fichas técnicas", "DESCRIPTION", "btn-sm", "btn-secondary");
            crear.setOnAction(e -> navigator.goTo("fichas", Intent.preseleccionar(p.getCodigo())));
            estandar.getChildren().addAll(UI.hbox(8, UI.label("Costo estándar", "card-title"), UI.spacer(), new StatusBadge("Sin ficha aprobada")),
                    UI.wrap("El modelo no tiene una ficha técnica aprobada; el costo es estimado.", "muted"), crear);
        } else {
            double margen = ce.margenPct();
            Hyperlink link = new Hyperlink(ce.ficha().getEtiqueta());
            link.setOnAction(e -> navigator.goTo("fichas", Intent.abrir(ce.ficha().getUid())));
            estandar.getChildren().addAll(
                    UI.hbox(8, UI.label("Costo estándar", "card-title"), UI.spacer(), link, new StatusBadge("Aprobada")),
                    UI.hbox(14, UI.miniStat("Costo estándar", Money.fmt(ce.costo())), UI.miniStat("Precio", Money.fmt(p.getPrecio())),
                            UI.miniStat("Margen real", String.format("%.1f %%", margen))));
            if (margen < 35) {
                estandar.getChildren().add(UI.banner("danger-banner", "WARNING", "Margen por debajo del 35 %. Precio sugerido: "
                        + Money.fmt0(ce.costo() / 0.65) + "."));
            }
        }
        cuerpo.getChildren().add(estandar);
        if (p.getDescripcion() != null) cuerpo.getChildren().addAll(UI.label("Descripción", "card-title"), UI.wrap(p.getDescripcion(), "muted"));

        Button personalizar = UI.primary("Personalizar en 3D", "MDOAL_3D_ROTATION");
        personalizar.setOnAction(e -> navigator.goTo("configurador", Intent.preseleccionar(p.getCodigo())));
        Button vender = UI.button("Vender", "POINT_OF_SALE", "btn-wood");
        vender.setDisable(p.getStock() <= 0);
        UI.tooltip(vender, p.getStock() <= 0 ? "Sin stock: créalo como pedido a medida" : "Agregar al carrito del POS");
        vender.setOnAction(e -> navigator.goTo("ventas", new Intent("AGREGAR", p.getCodigo())));
        Button editar = UI.secondary("Editar", "EDIT");
        editar.setOnAction(e -> {
            panel.cerrar();
            formulario(p);
        });
        Button eliminar = UI.iconButton("DELETE_OUTLINE", "Eliminar modelo", "btn-danger-soft");
        eliminar.setOnAction(e -> confirm.mostrar("¿Eliminar " + p.getNombre() + "?",
                "El modelo dejará de aparecer en el catálogo y en el POS. Las ventas y fichas anteriores se conservan.", "Eliminar",
                () -> {
                    if (toast.intentar(() -> productoService.eliminar(p.getId()))) {
                        panel.cerrar();
                        toast.exito(p.getCodigo() + " eliminado del catálogo.");
                        onShow(Intent.NONE);
                    }
                }));
        HBox pie = UI.hbox(8, eliminar, UI.spacer(), editar, vender, personalizar);
        panel = overlays.panel(p.getNombre(), p.getCodigo() + " · " + p.getCategoria(), cuerpo, pie, 500);
    }

    // ================================================================ formulario

    private void formulario(Producto existente) {
        Producto p = existente == null ? Producto.builder().cuerpos(3).stock(0).forma(Forma.MODERNO).categoria("Sofá").build() : existente;
        Validacion v = new Validacion();
        TextField nombre = UI.text(p.getNombre(), "Sofá Moderno 3 cuerpos");
        ComboBox<String> categoria = Forms.combo(Producto.CATEGORIAS, p.getCategoria(), s -> s);
        ComboBox<Forma> forma = Forms.combo(List.of(Forma.values()), p.getForma(), Forma::nombre);
        TextField material = UI.text(p.getMaterial(), "Chenille");
        TextField color = UI.text(p.getColor(), "Gris perla");
        ColorPicker hex = new ColorPicker(ColorSwatch.parse(p.getColorHex()));
        hex.setMaxWidth(Double.MAX_VALUE);
        TextField largo = Forms.entero(p.getLargo());
        TextField fondo = Forms.entero(p.getFondo());
        TextField alto = Forms.entero(p.getAlto());
        TextField cuerpos = Forms.entero(p.getCuerpos());
        TextField costo = Forms.decimal(p.getCosto());
        TextField precio = Forms.decimal(p.getPrecio());
        TextField stock = Forms.entero(p.getStock());
        TextArea desc = Forms.area(p.getDescripcion(), "Descripción para la ficha y la cotización", 3);

        FormGrid f = new FormGrid(3);
        f.seccion("1", "Modelo");
        f.nodo(v.campo("Nombre", nombre, () -> Validacion.requerido(nombre, "el nombre")), 2);
        f.nodo(v.campo("Categoría", categoria, () -> Validacion.requerido(categoria, "la categoría")), 1);
        f.nodo(v.campo("Forma (3D y corte)", forma, () -> Validacion.requerido(forma, "la forma")), 1);
        f.nodo(v.campo("Material", material, null), 1);
        f.nodo(v.campo("Color", color, null), 1);
        f.nodo(v.campo("Color (render)", hex, null), 1);
        f.seccion("2", "Medidas");
        f.nodo(v.campo("Largo (cm)", largo, () -> Validacion.numero(largo, 30, "El largo")), 1);
        f.nodo(v.campo("Fondo (cm)", fondo, () -> Validacion.numero(fondo, 30, "El fondo")), 1);
        f.nodo(v.campo("Alto (cm)", alto, () -> Validacion.numero(alto, 30, "El alto")), 1);
        f.nodo(v.campo("Cuerpos", cuerpos, () -> Validacion.numero(cuerpos, 1, "Los cuerpos")), 1);
        f.seccion("3", "Precio y stock");
        f.nodo(v.campo("Costo (S/)", costo, () -> Validacion.numero(costo, 0, "El costo")), 1);
        f.nodo(v.campo("Precio de venta (S/)", precio, () -> {
            String e = Validacion.numero(precio, 1, "El precio");
            if (e != null) return e;
            return Validacion.valor(precio) <= Validacion.valor(costo) ? "El precio debe ser mayor que el costo." : null;
        }), 1);
        f.nodo(v.campo("Stock", stock, () -> Validacion.numero(stock, 0, "El stock")), 1);
        f.nodo(v.campo("Descripción", desc, null), 3);
        v.escuchar(costo.textProperty());

        Button cancelar = UI.secondary("Cancelar", null);
        Button guardar = UI.primary(existente == null ? "Registrar modelo" : "Guardar cambios", "SAVE");
        v.enlazar(guardar);
        var h = overlays.modal(existente == null ? "Registrar modelo" : "Editar " + p.getCodigo(),
                "Los campos marcados se validan mientras escribes.", f, 640, cancelar, guardar);
        cancelar.setOnAction(e -> h.cerrar());
        guardar.setOnAction(e -> {
            p.setNombre(nombre.getText().trim());
            p.setCategoria(categoria.getValue());
            p.setForma(forma.getValue());
            p.setMaterial(material.getText());
            p.setColor(color.getText());
            p.setColorHex(String.format("#%02X%02X%02X", (int) (hex.getValue().getRed() * 255), (int) (hex.getValue().getGreen() * 255), (int) (hex.getValue().getBlue() * 255)));
            p.setLargo(Forms.entero(largo));
            p.setFondo(Forms.entero(fondo));
            p.setAlto(Forms.entero(alto));
            p.setCuerpos(Forms.entero(cuerpos));
            p.setCosto(Forms.dinero(costo));
            p.setPrecio(Forms.dinero(precio));
            p.setStock(Forms.entero(stock));
            p.setDescripcion(desc.getText());
            if (toast.intentar(() -> {
                Producto g = productoService.guardar(p);
                toast.exito(g.getCodigo() + " · " + g.getNombre() + (existente == null ? " registrado." : " actualizado."));
            })) {
                h.cerrar();
                onShow(Intent.NONE);
            }
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
