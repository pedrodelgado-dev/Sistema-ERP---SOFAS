/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Movimiento
 * Descripción: Movimiento del kardex. La cantidad es positiva salvo en los ajustes, que llevan signo.
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

import java.time.LocalDateTime;

/** Movimiento del kardex. La cantidad es positiva salvo en los ajustes, que llevan signo. */
@Entity
@Table(name = "movimiento", indexes = @Index(columnList = "material_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Double cantidad;

    private String referencia;

    private String concepto;

    /** Saldo fisico del material despues del movimiento. */
    private Double saldo;

    private String usuario;

    /** Cantidad con signo segun su efecto en el saldo (0 si no lo afecta). */
    public double getEfecto() {
        return switch (tipo) {
            case ENTRADA -> cantidad;
            case SALIDA, CONSUMO_OP -> -cantidad;
            case AJUSTE -> cantidad;
            default -> 0;
        };
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
