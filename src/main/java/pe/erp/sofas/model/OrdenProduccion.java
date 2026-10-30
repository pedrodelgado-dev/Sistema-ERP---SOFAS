/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: OrdenProduccion
 * Descripción: Nulo en los retapizados.
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

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import pe.erp.sofas.core.AppClock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "orden_produccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class OrdenProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String codigo;

    /** Nulo en los retapizados. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /** "Retapizado · Sofá 2 cuerpos" o un nombre a medida. */
    private String nombrePersonalizado;

    @Builder.Default
    @Column(nullable = false)
    private Integer cantidad = 1;

    /** Nulo cuando la orden es para stock de tienda. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsable_id")
    private Empleado responsable;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 10)
    private Prioridad prioridad = Prioridad.MEDIA;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private Etapa etapa = Etapa.PENDIENTE;

    private LocalDate fechaInicio;
    private LocalDate fechaEntrega;
    /** Fecha en que llego a Listo. */
    private LocalDate fechaFin;

    @Column(precision = 12, scale = 2)
    private BigDecimal costo;

    /** Version de la ficha usada ("FT-001v3"); nulo si no hay ficha aprobada. */
    @Column(length = 16)
    private String fichaUid;

    /** Medidas a pedido (nulo = las de la ficha). */
    private Integer largo;
    private Integer fondo;
    private Integer alto;
    private Integer cuerpos;

    /** Sustituciones de material: codigo original → codigo elegido (tela, espuma, patas). */
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "op_sustitucion", joinColumns = @JoinColumn(name = "op_id"))
    @MapKeyColumn(name = "original", length = 20)
    @Column(name = "elegido", length = 20)
    @Fetch(FetchMode.SUBSELECT)
    private Map<String, String> sustituciones = new HashMap<>();

    /** Materiales adicionales por unidad (configurador). */
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "op_extra", joinColumns = @JoinColumn(name = "op_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<LineaExtra> extras = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tela_id")
    private Tela tela;

    /** Texto de la autorizacion cuando avanzo sin ficha aprobada. */
    @Column(length = 300)
    private String autorizacion;

    @Column(length = 12)
    private String pedidoCodigo;

    @Column(length = 12)
    private String retapizadoCodigo;

    /** Resumen de la configuracion (configurador 3D). */
    @Column(length = 600)
    private String especificacion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean anulada = false;

    public String getProductoNombre() {
        if (nombrePersonalizado != null && !nombrePersonalizado.isBlank()) return nombrePersonalizado;
        return producto == null ? "—" : producto.getNombre();
    }

    public boolean isRetrasada() {
        return etapa != Etapa.LISTO && !Boolean.TRUE.equals(anulada)
                && fechaEntrega != null && fechaEntrega.isBefore(AppClock.hoy());
    }

    public boolean isActiva() {
        return !Boolean.TRUE.equals(anulada) && etapa != Etapa.LISTO;
    }

    public String getClienteNombre() {
        return cliente == null ? "Stock de tienda" : cliente.getNombre();
    }

    public boolean isSinFicha() {
        return fichaUid == null;
    }

    public String getEstado() {
        if (Boolean.TRUE.equals(anulada)) return "Cancelado";
        if (etapa == Etapa.LISTO) return "Listo";
        if (isRetrasada()) return "Retrasado";
        if (etapa == Etapa.PENDIENTE) return "Pendiente";
        return "En producción";
    }

    public String getTelaNombre() {
        return tela == null ? "Según ficha" : tela.getNombre();
    }

    @Override
    public String toString() {
        return codigo + " · " + getProductoNombre();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
