/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Venta
 * Descripción: Venta de tienda (POS) con comprobante simulado B001 / F001.
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Venta de tienda (POS) con comprobante simulado B001 / F001. */
@Entity
@Table(name = "venta", uniqueConstraints = @UniqueConstraint(columnNames = {"serie", "numero"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Venta {

    public enum Comprobante { BOLETA, FACTURA;

        public String nombre() {
            return this == BOLETA ? "Boleta" : "Factura";
        }

        public String serie() {
            return this == BOLETA ? "B001" : "F001";
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Nulo = "Cliente varios". */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Comprobante comprobante;

    /** Efectivo, Tarjeta, Transferencia, Yape, Plin o "Mixto (Efectivo + Yape)". */
    private String metodoPago;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(precision = 12, scale = 2)
    private BigDecimal recibido;

    @Column(precision = 12, scale = 2)
    private BigDecimal vuelto;

    private String vendedor;

    @Builder.Default
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemVenta> items = new ArrayList<>();

    public void agregar(ItemVenta item) {
        item.setVenta(this);
        items.add(item);
    }

    public String getNumeroCompleto() {
        return serie + "-" + String.format("%06d", numero);
    }

    public String getClienteNombre() {
        return cliente == null ? "Cliente varios" : cliente.getNombre();
    }

    public int getUnidades() {
        return items.stream().mapToInt(ItemVenta::getCantidad).sum();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
