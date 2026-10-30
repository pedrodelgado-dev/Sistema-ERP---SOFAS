/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ReclamoGarantia
 * Descripción: Fecha del reclamo.
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
import java.time.LocalDate;

@Entity
@Table(name = "reclamo_garantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class ReclamoGarantia {

    public static final java.util.List<String> FALLAS = java.util.List.of(
            "Pata floja", "Estructura quebrada", "Mecanismo trabado", "Espuma hundida", "Costura abierta",
            "Tela con pilling", "Decoloración de tela", "Cierre roto");
    public static final java.util.List<String> SOLUCIONES = java.util.List.of(
            "Por definir", "Reparación en domicilio", "Reparación en taller", "Cambio de espuma",
            "Cambio de funda", "Reemplazo del mecanismo", "Cambio de producto", "No procede");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private LocalDate fechaCompra;

    @Column(nullable = false)
    private String falla;

    @Column(length = 600)
    private String descripcion;

    /** Fecha del reclamo. */
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReclamo estado;

    @Column(length = 600)
    private String diagnostico;

    private String solucion;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO;

    private LocalDate fechaCierre;

    public String getProductoNombre() {
        return producto == null ? "—" : producto.getNombre();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
