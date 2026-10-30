/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ProveedoresController
 * Descripción: Proveedores con RUC, contacto, materiales que abastecen, condicion de pago, estado y total comprado.
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
package pe.erp.sofas.ui.views.proveedores;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.Proveedor;
import pe.erp.sofas.service.BusquedaService;
import pe.erp.sofas.service.ProveedorService;
import pe.erp.sofas.ui.components.*;

import java.util.Comparator;
import java.util.List;

/** Proveedores con RUC, contacto, materiales que abastecen, condicion de pago, estado y total comprado. */
@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ProveedoresController implements Vista {

    private final ProveedorService proveedores;
    private final Overlays overlays;
    private final Toast toast;
    private final ConfirmDialog confirm;
    private final Navigator navigator;

    @FXML private VBox raiz;

    private SearchField buscador;
    private final TableView<Proveedor> tabla = Cells.tabla("Ningún proveedor coincide.");
    private Overlays.Handle panel;

    @FXML
    public void initialize() {
        buscador = new SearchField("Buscar proveedor, RUC o material");
        buscador.setPrefWidth(320);
        buscador.textProperty().addListener((o, a, b) -> render());
        Button nuevo = UI.primary("Nuevo proveedor", "ADD");
        nuevo.setOnAction(e -> formulario(null));
        TableColumn<Proveedor, String> total = Cells.derecha("Total comprado", p -> Money.fmt0(p.getTotalComprado()), 120, "cell-strong");
        total.setComparator(Comparator.comparing(Money::parse));
        tabla.getColumns().addAll(List.of(
                Cells.texto("Código", Proveedor::getCodigo, 66, "cell-mono"),
                Cells.texto("Proveedor", Proveedor::getRazonSocial, 220, "cell-strong"),
                Cells.texto("RUC", Proveedor::getRuc, 110, "cell-mono"),
                Cells.nodo("Contacto", p -> new VBox(0, UI.label(p.getContacto(), "cell-title"), UI.label(p.getTelefono(), "cell-sub")), 150),
                Cells.texto("Materiales", Proveedor::getRubro, 260, "cell-muted"),
                Cells.texto("Condición", Proveedor::getCondicionPago, 120),
                Cells.badge("Estado", p -> p.getActivo() ? "Activo" : "Inactivo", 80), total));
        tabla.setRowFactory(tv -> {
            TableRow<Proveedor> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (!r.isEmpty()) ficha(r.getItem());
            });
            return r;
        });
        VBox card = new VBox(tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        card.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        raiz.getChildren().setAll(UI.hbox(10, buscador, UI.spacer(), nuevo), card);
    }

    @Override
    public void onShow(Intent intent) {
        render();
        if (intent.es(Intent.ACCION_NUEVO)) formulario(null);
    }

    private void render() {
        String q = BusquedaService.normalizar(buscador.getText());
        tabla.getItems().setAll(proveedores.listar().stream()
                .filter(p -> q.isEmpty() || BusquedaService.normalizar(p.getRazonSocial() + " " + p.getRuc() + " " + p.getRubro() + " " + p.getCodigo()).contains(q)).toList());
    }

    private void ficha(Proveedor p) {
        GridPane g = UI.grid(2);
        g.add(UI.dato("RUC", p.getRuc()), 0, 0);
        g.add(UI.dato("Condición de pago", p.getCondicionPago()), 1, 0);
        g.add(UI.dato("Contacto", p.getContacto() + " · " + p.getTelefono()), 0, 1);
        g.add(UI.dato("Correo", p.getEmail()), 1, 1);
        g.add(UI.dato("Dirección", p.getDireccion()), 0, 2, 2, 1);
        VBox mats = new VBox(4, UI.label("Materiales que abastece", "card-title"));
        proveedores.materiales(p).forEach(m -> mats.getChildren().add(UI.hbox(8, UI.codigo(m.getCodigo()), UI.label(m.getNombre(), "cell-title"), UI.spacer(),
                new StatusBadge(m.getEstado()))));
        VBox ocs = new VBox(4, UI.label("Órdenes de compra", "card-title"));
        proveedores.compras(p).forEach(o -> {
            Hyperlink l = new Hyperlink(o.getCodigo());
            l.setOnAction(e -> navigator.goTo("compras", Intent.abrir(o.getCodigo())));
            ocs.getChildren().add(UI.hbox(8, l, UI.label(AppClock.fmt(o.getFecha()), "cell-sub"), UI.spacer(), UI.label(Money.fmt(o.getTotal()), "cell-title"),
                    new StatusBadge(o.getEstado().nombre())));
        });
        if (ocs.getChildren().size() == 1) ocs.getChildren().add(UI.label("Sin órdenes.", "muted"));
        VBox cuerpo = new VBox(16, UI.hbox(8, new StatusBadge(p.getActivo() ? "Activo" : "Inactivo"), UI.label(p.getRubro(), "cell-sub")), g,
                UI.hbox(8, UI.label("Total comprado", "muted"), UI.spacer(), UI.label(Money.fmt(p.getTotalComprado()), "price-value")), mats, ocs);
        Button editar = UI.secondary("Editar", "EDIT");
        editar.setOnAction(e -> {
            panel.cerrar();
            formulario(p);
        });
        Button estado = UI.button(p.getActivo() ? "Desactivar" : "Activar", p.getActivo() ? "BLOCK" : "DONE", p.getActivo() ? "btn-danger-soft" : "btn-success");
        estado.setOnAction(e -> confirm.mostrar(p.getActivo() ? ConfirmDialog.Tipo.PELIGRO : ConfirmDialog.Tipo.INFO,
                (p.getActivo() ? "¿Desactivar " : "¿Activar ") + p.getRazonSocial() + "?",
                p.getActivo() ? "No se le podrán emitir nuevas órdenes de compra." : "Volverá a estar disponible para compras.", p.getActivo() ? "Desactivar" : "Activar", () -> {
                    p.setActivo(!p.getActivo());
                    if (toast.intentar(() -> proveedores.guardar(p))) {
                        panel.cerrar();
                        toast.exito(p.getRazonSocial() + (p.getActivo() ? " activado." : " desactivado."));
                        render();
                    }
                }));
        panel = overlays.panel(p.getRazonSocial(), p.getCodigo(), cuerpo, UI.hbox(8, estado, UI.spacer(), editar), 500);
    }

    private void formulario(Proveedor existente) {
        Proveedor p = existente == null ? Proveedor.builder().activo(true).totalComprado(java.math.BigDecimal.ZERO).condicionPago("Contado").build() : existente;
        Validacion v = new Validacion();
        TextField rs = UI.text(p.getRazonSocial(), "Razón social");
        TextField ruc = Forms.digitos(p.getRuc(), 11);
        TextField contacto = UI.text(p.getContacto(), "Nombre del contacto");
        TextField tel = UI.text(p.getTelefono(), "951 000 000");
        TextField email = UI.text(p.getEmail(), "ventas@proveedor.pe");
        TextField dir = UI.text(p.getDireccion(), "Dirección");
        TextField rubro = UI.text(p.getRubro(), "Materiales que abastece");
        ComboBox<String> cond = Forms.combo(List.of("Contado", "Crédito 15 días", "Crédito 30 días", "Crédito 45 días"), p.getCondicionPago(), s -> s);
        FormGrid f = new FormGrid(2);
        f.nodo(v.campo("Razón social", rs, () -> Validacion.requerido(rs, "la razón social")), 2);
        f.nodo(v.campo("RUC", ruc, () -> ruc.getText().matches("\\d{11}") ? null : "El RUC debe tener 11 dígitos."), 1);
        f.nodo(v.campo("Condición de pago", cond, null), 1);
        f.nodo(v.campo("Contacto", contacto, null), 1);
        f.nodo(v.campo("Teléfono", tel, null), 1);
        f.nodo(v.campo("Correo", email, null), 1);
        f.nodo(v.campo("Dirección", dir, null), 1);
        f.nodo(v.campo("Materiales que abastece", rubro, () -> Validacion.requerido(rubro, "los materiales")), 2);
        Button cancelar = UI.secondary("Cancelar", null);
        Button ok = UI.primary(existente == null ? "Registrar proveedor" : "Guardar cambios", "SAVE");
        v.enlazar(ok);
        var h = overlays.modal(existente == null ? "Nuevo proveedor" : "Editar " + p.getCodigo(), null, f, 600, cancelar, ok);
        cancelar.setOnAction(e -> h.cerrar());
        ok.setOnAction(e -> {
            p.setRazonSocial(rs.getText().trim());
            p.setRuc(ruc.getText().trim());
            p.setContacto(contacto.getText());
            p.setTelefono(tel.getText());
            p.setEmail(email.getText());
            p.setDireccion(dir.getText());
            p.setRubro(rubro.getText());
            p.setCondicionPago(cond.getValue());
            Proveedor[] g = new Proveedor[1];
            if (toast.intentar(() -> g[0] = proveedores.guardar(p))) {
                h.cerrar();
                toast.exito(g[0].getCodigo() + " · " + g[0].getRazonSocial() + " guardado.");
                render();
            }
        });
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
