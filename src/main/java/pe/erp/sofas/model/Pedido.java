/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Pedido
 * Descripción: Precio total del pedido (IGV incluido).
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Pedido {

    public enum Tipo { ESTANDAR, PERSONALIZADO;

        public String nombre() {
            return this == ESTANDAR ? "Estándar" : "Personalizado";
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Tipo tipo;

    @Builder.Default
    private Integer cantidad = 1;

    private LocalDate fecha;
    private LocalDate fechaEntrega;

    /** Precio total del pedido (IGV incluido). */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal adelanto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoPedido estado;

    @Column(length = 12)
    private String opCodigo;

    private Integer largo;
    private Integer fondo;
    private Integer alto;

    /** "Chenille", "Cuero sintético"... */
    private String material;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tela_id")
    private Tela tela;

    @Column(length = 600)
    private String observaciones;

    /** Configuracion serializada del configurador 3D (clave=valor;...). */
    @Column(length = 800)
    private String configuracion;

    /** Resumen legible de la configuracion. */
    @Column(length = 600)
    private String especificacion;

    public BigDecimal getSaldo() {
        return precio.subtract(adelanto).max(BigDecimal.ZERO);
    }

    public double getPctPagado() {
        return precio.signum() == 0 ? 0 : adelanto.divide(precio, 4, RoundingMode.HALF_UP).doubleValue();
    }

    public boolean isRetrasado() {
        return !estado.cerrado() && estado != EstadoPedido.LISTO && fechaEntrega != null
                && fechaEntrega.isBefore(pe.erp.sofas.core.AppClock.hoy());
    }

    public String getProductoNombre() {
        return producto == null ? "Sofá a medida" : producto.getNombre();
    }

    public String getMedidas() {
        if (largo == null || largo == 0) return producto == null ? "—" : producto.getMedidas();
        return largo + " × " + fondo + " × " + alto + " cm";
    }

    public String getColor() {
        return tela == null ? "—" : tela.getColor();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
