/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConsumoReal
 * Descripción: Consumo real frente al estimado por la ficha al cerrar una OP (base de Consumo y mermas).
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

import java.time.LocalDate;

/** Consumo real frente al estimado por la ficha al cerrar una OP (base de Consumo y mermas). */
@Entity
@Table(name = "consumo_real")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ConsumoReal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String opCodigo;

    private String producto;

    /** Codigo de la ficha (FT-001) usada, para ajustar la ficha con el promedio real. */
    @Column(length = 16)
    private String fichaUid;

    private String responsable;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false)
    private Double estimado;

    @Column(nullable = false)
    private Double realConsumido;

    private LocalDate fecha;

    public double getDiferencia() {
        return realConsumido - estimado;
    }

    public double getDiferenciaPct() {
        return estimado == 0 ? 0 : (realConsumido - estimado) * 100 / estimado;
    }

    public double getDiferenciaValor() {
        return getDiferencia() * material.getCosto();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
