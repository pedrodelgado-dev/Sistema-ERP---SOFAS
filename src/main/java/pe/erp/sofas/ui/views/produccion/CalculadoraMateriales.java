/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CalculadoraMateriales
 * Descripción: Calculadora de materiales (5.11): producto o juego, cantidad, medidas a pedido, tela y espuma de asiento.
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
package pe.erp.sofas.ui.views.produccion;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.service.calculo.Explosion;
import pe.erp.sofas.service.calculo.Medidas;
import pe.erp.sofas.service.calculo.Opciones;
import pe.erp.sofas.ui.components.*;

import java.util.*;

/**
 * Calculadora de materiales (5.11): producto o juego, cantidad, medidas a pedido, tela y espuma de asiento.
 * Muestra materiales, mano de obra, costo, dias de fabricacion (horas / 16 + 1, +5 si falta algo) y permite
 * copiar a cotizacion, generar OC de faltantes, reservar (crea la OP y reserva) o crear la OP en Pendiente.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class CalculadoraMateriales {

    private record Linea(Material material, String componentes, double necesario, double disponible, double faltante, double costo, String estado) {
    }

    private final ProductoService productos;
    private final FichaService fichas;
    private final TelaService telas;
    private final ProduccionService produccion;
    private final MrpService mrp;
    private final Overlays overlays;
    private final Toast toast;
    private final Navigator navigator;

    public void abrir() {
        List<Producto> lista = new ArrayList<>(productos.listar().stream().filter(p -> fichas.aprobadaDe(p) != null).toList());
        lista.sort(Comparator.comparing((Producto p) -> !Boolean.TRUE.equals(fichas.aprobadaDe(p).getCompuesta())).thenComparing(Producto::getCodigo));
        ComboBox<Producto> producto = Forms.combo(lista, lista.stream().filter(p -> p.getCodigo().equals("SF-001")).findFirst().orElse(lista.get(0)),
                p -> (Boolean.TRUE.equals(fichas.aprobadaDe(p).getCompuesta()) ? "Juego · " : "") + p.getCodigo() + " · " + p.getNombre());
        Spinner<Integer> cantidad = new Spinner<>(1, 50, 1);
        cantidad.setEditable(true);
        cantidad.setMaxWidth(Double.MAX_VALUE);
        CheckBox aMedida = new CheckBox("Medidas a pedido");
        TextField largo = Forms.entero(210), fondo = Forms.entero(92), alto = Forms.entero(85);
        List<Tela> ts = new ArrayList<>();
        ts.add(null);
        ts.addAll(telas.activas());
        ComboBox<Tela> tela = Forms.combo(ts, null, t -> t == null ? "Según ficha" : t.getNombre() + " · " + Money.numero(t.getStock()) + " m");
        ComboBox<String> espuma = Forms.combo(List.of("D23 estándar", "D30 alta densidad", "Viscoelástica"), "D23 estándar", s -> s);

        VBox resultado = new VBox(12);
        Object[] estado = new Object[2]; // [0] = Explosion.Resultado, [1] = List<Linea>
        Runnable calcular = () -> {
            Producto p = producto.getValue();
            if (p == null) return;
            FichaTecnica f = fichas.aprobadaDe(p);
            boolean juego = Boolean.TRUE.equals(f.getCompuesta());
            aMedida.setDisable(juego);
            if (juego) aMedida.setSelected(false);
            largo.setDisable(!aMedida.isSelected());
            fondo.setDisable(!aMedida.isSelected());
            alto.setDisable(!aMedida.isSelected());
            Map<String, String> sust = new HashMap<>();
            if (tela.getValue() != null) for (TipoTela t : TipoTela.values()) sust.put(t.materialCodigo(), tela.getValue().getTipo().materialCodigo());
            if (espuma.getValue().startsWith("D30")) sust.put("MP-019", "MP-020");
            if (espuma.getValue().startsWith("Visco")) sust.put("MP-019", "MP-034");
            Medidas m = aMedida.isSelected() ? new Medidas(Forms.entero(largo), Forms.entero(fondo), Forms.entero(alto), f.getCuerpos()) : null;
            Explosion.Resultado r = fichas.explotar(f, cantidad.getValue(), new Opciones(m, sust, List.of(), false, 0, 0));
            Map<String, Set<String>> comp = new LinkedHashMap<>();
            r.lineas().forEach(l -> comp.computeIfAbsent(l.material().getCodigo(), k -> new LinkedHashSet<>()).add(l.componente().nombre()));
            List<Linea> lineas = new ArrayList<>();
            int ok = 0, parcial = 0, sin = 0;
            for (Explosion.PorMaterial pm : r.porMaterial()) {
                Material mat = pm.material();
                double disp = mat.getDisponible();
                double falta = Math.max(0, pm.cantidad() - disp);
                String est;
                if (Boolean.TRUE.equals(mat.getBajoPedido()) && mat.getStock() <= 0) {
                    est = "Bajo pedido";
                    sin++;
                } else if (falta <= 1e-6) {
                    est = "Suficiente";
                    ok++;
                } else if (disp > 0) {
                    est = "Parcial";
                    parcial++;
                } else {
                    est = "Sin stock";
                    sin++;
                }
                lineas.add(new Linea(mat, String.join(", ", comp.get(mat.getCodigo())), pm.cantidad(), disp, falta, pm.costo(), est));
            }
            estado[0] = r;
            estado[1] = lineas;
            int dias = (int) Math.ceil(r.horasMO() / 16 + 1) + (parcial + sin > 0 ? 5 : 0);
            resultado.getChildren().setAll(
                    UI.hbox(10, UI.miniStat("Materiales", Money.fmt(r.costoMateriales())), UI.miniStat("Mano de obra (" + Money.numero(r.horasMO()) + " h)", Money.fmt(r.costoMO())),
                            UI.miniStat("Costo total (con indirectos)", Money.fmt(r.costoEstandar())), UI.miniStat("Fabricación estimada", dias + " días")),
                    UI.hbox(8, new StatusBadge(ok + " suficientes", "green"), new StatusBadge(parcial + " parciales", "amber"),
                            new StatusBadge(sin + " sin stock o a pedido", "red"), UI.spacer(), UI.label("Ficha " + f.getEtiqueta(), "code")),
                    tabla(lineas));
        };
        for (var c : List.of(producto.valueProperty(), tela.valueProperty(), espuma.valueProperty())) c.addListener((o, a, b) -> calcular.run());
        cantidad.valueProperty().addListener((o, a, b) -> calcular.run());
        aMedida.selectedProperty().addListener((o, a, b) -> calcular.run());
        for (TextField t : List.of(largo, fondo, alto)) t.textProperty().addListener((o, a, b) -> {
            if (Forms.entero(t) >= 30) calcular.run();
        });
        producto.valueProperty().addListener((o, a, b) -> {
            if (b != null && b.getLargo() != null) {
                largo.setText(String.valueOf(b.getLargo()));
                fondo.setText(String.valueOf(b.getFondo()));
                alto.setText(String.valueOf(b.getAlto()));
            }
        });
        calcular.run();

        GridPane f = UI.grid(4);
        f.add(UI.field("Producto o juego", producto), 0, 0, 3, 1);
        f.add(UI.field("Cantidad", cantidad), 3, 0);
        f.add(UI.field("", aMedida), 0, 1);
        f.add(UI.field("Largo (cm)", largo), 1, 1);
        f.add(UI.field("Fondo (cm)", fondo), 2, 1);
        f.add(UI.field("Alto (cm)", alto), 3, 1);
        f.add(UI.field("Tela", tela), 0, 2, 2, 1);
        f.add(UI.field("Espuma de asiento", espuma), 2, 2, 2, 1);
        VBox cuerpo = new VBox(16, f, resultado);

        Button copiar = UI.secondary("Copiar a cotización", "CONTENT_COPY");
        Button oc = UI.button("Generar OC de faltantes", "SHOPPING_CART", "btn-wood");
        Button reservar = UI.secondary("Reservar materiales", "LOCK");
        Button crear = UI.primary("Crear OP", "ADD");
        var h = overlays.modal("Calculadora de materiales", "Explosión de la ficha aprobada con medidas, tela y espuma", cuerpo, 900, copiar, oc, UI.spacer(), reservar, crear);
        copiar.setOnAction(e -> {
            @SuppressWarnings("unchecked") List<Linea> l = (List<Linea>) estado[1];
            Explosion.Resultado r = (Explosion.Resultado) estado[0];
            StringBuilder sb = new StringBuilder("SISTEMA ERP SOFAS · Cotización de materiales\n");
            sb.append(producto.getValue().getNombre()).append(" × ").append(cantidad.getValue());
            if (tela.getValue() != null) sb.append(" · ").append(tela.getValue().getNombre());
            sb.append("\n");
            for (Linea x : l) sb.append("- ").append(x.material().getNombre()).append(": ").append(Money.numero(x.necesario())).append(" ").append(x.material().getUnidad()).append("\n");
            sb.append("Materiales ").append(Money.fmt(r.costoMateriales())).append(" · Mano de obra ").append(Money.fmt(r.costoMO()))
                    .append(" · Costo total ").append(Money.fmt(r.costoEstandar())).append("\n");
            ClipboardContent cc = new ClipboardContent();
            cc.putString(sb.toString());
            Clipboard.getSystemClipboard().setContent(cc);
            toast.exito("Resumen copiado al portapapeles.");
        });
        oc.setOnAction(e -> {
            @SuppressWarnings("unchecked") List<Linea> l = (List<Linea>) estado[1];
            Map<Long, Double> tr = mrp.transito();
            List<MrpService.Fila> filas = new ArrayList<>();
            for (Linea x : l) {
                double falta = Math.max(0, x.faltante() - tr.getOrDefault(x.material().getId(), 0d));
                if (falta > 0) filas.add(new MrpService.Fila(x.material(), x.necesario(), tr.getOrDefault(x.material().getId(), 0d), falta, AppClock.hoy(), x.material().getProveedor(), Set.of("Calculadora")));
            }
            if (filas.isEmpty()) {
                toast.info("No hay faltantes por comprar.");
                return;
            }
            List<String>[] c = new List[1];
            if (toast.intentar(() -> c[0] = mrp.generarOrdenes(filas, "Calculadora"))) {
                toast.exito("OC en borrador: " + String.join(", ", c[0]) + ".");
                navigator.datosCambiaron();
            }
        });
        reservar.setOnAction(e -> crearOP(h, producto, cantidad, tela, espuma, aMedida, largo, fondo, alto, true));
        crear.setOnAction(e -> crearOP(h, producto, cantidad, tela, espuma, aMedida, largo, fondo, alto, false));
    }

    private void crearOP(Overlays.Handle h, ComboBox<Producto> producto, Spinner<Integer> cantidad, ComboBox<Tela> tela, ComboBox<String> espuma,
                         CheckBox aMedida, TextField largo, TextField fondo, TextField alto, boolean reservar) {
        Map<String, String> sust = new HashMap<>();
        if (espuma.getValue().startsWith("D30")) sust.put("MP-019", "MP-020");
        if (espuma.getValue().startsWith("Visco")) sust.put("MP-019", "MP-034");
        FichaTecnica f = fichas.aprobadaDe(producto.getValue());
        Medidas m = aMedida.isSelected() ? new Medidas(Forms.entero(largo), Forms.entero(fondo), Forms.entero(alto), f.getCuerpos()) : null;
        String[] msg = new String[1];
        if (toast.intentar(() -> {
            OrdenProduccion op = produccion.crear(new ProduccionService.NuevaOP(producto.getValue().getId(), null, cantidad.getValue(), null, null,
                    Prioridad.MEDIA, AppClock.hoy(), AppClock.hoy().plusDays(15), tela.getValue() == null ? null : tela.getValue().getId(), m, sust,
                    List.of(), null, null, "Creada desde la calculadora de materiales", Etapa.PENDIENTE, null, null));
            if (reservar) {
                var r = produccion.reservar(op.getId());
                msg[0] = op.getCodigo() + " creada y " + r.reservados() + " materiales reservados" + (r.faltantes().isEmpty() ? "" : " · faltan " + r.faltantes().size());
            } else {
                msg[0] = op.getCodigo() + " creada en Pendiente con la ficha " + f.getEtiqueta();
            }
        })) {
            h.cerrar();
            toast.exito(msg[0] + ".");
            navigator.datosCambiaron();
            navigator.goTo("produccion", Intent.NONE);
        }
    }

    private Node tabla(List<Linea> l) {
        TableView<Linea> t = Cells.tabla("Sin materiales.");
        t.getColumns().addAll(List.of(
                Cells.nodo("Material y componente", x -> new VBox(0, UI.label(x.material().getNombre(), "cell-title"),
                        UI.label(x.material().getCodigo() + " · " + x.componentes(), "cell-sub")), 260),
                Cells.derecha("Necesario", x -> Money.numero(x.necesario()) + " " + x.material().getUnidad(), 100),
                Cells.derecha("Disponible", x -> Money.numero(x.disponible()), 90, "cell-muted"),
                Cells.nodo("Faltante", x -> {
                    Label lb = UI.label(x.faltante() > 0 ? Money.numero(x.faltante()) : "—", "cell-title");
                    if (x.faltante() > 0) lb.setStyle("-fx-text-fill: -is-danger;");
                    return lb;
                }, 80),
                Cells.derecha("Costo", x -> Money.fmt(x.costo()), 90),
                Cells.badge("Estado", Linea::estado, 100)));
        t.getItems().setAll(l);
        t.setFixedCellSize(46);
        t.setPrefHeight(Math.min(10, l.size()) * 46 + 44);
        VBox card = new VBox(t);
        card.getStyleClass().addAll("card", "table-card");
        return card;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
