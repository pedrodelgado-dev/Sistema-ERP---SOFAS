/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: OperacionMO
 * Descripción: Horas de mano de obra de una etapa en la ficha tecnica.
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

/** Horas de mano de obra de una etapa en la ficha tecnica. */
@Entity
@Table(name = "operacion_mo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class OperacionMO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id")
    @ToString.Exclude
    private FichaTecnica ficha;

    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtapaMO etapa;

    @Column(nullable = false)
    private Double horas;

    @Column(nullable = false)
    private Double costoHora;

    public double getCosto() {
        return horas * costoHora;
    }

    public OperacionMO copia() {
        return toBuilder().id(null).ficha(null).build();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
