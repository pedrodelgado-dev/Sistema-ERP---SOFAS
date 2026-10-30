/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: LineaBOM
 * Descripción: Linea de la lista de materiales: cantidad base para las medidas de la ficha, merma y escalado.
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

/** Linea de la lista de materiales: cantidad base para las medidas de la ficha, merma y escalado. */
@Entity
@Table(name = "linea_bom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class LineaBOM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id")
    @ToString.Exclude
    private FichaTecnica ficha;

    private Integer orden;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "material_id")
    private Material material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Componente componente;

    @Column(nullable = false)
    private Double cantidad;

    @Builder.Default
    @Column(nullable = false)
    private Double mermaPct = 0d;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    @Builder.Default
    private Dependencia dependencia = Dependencia.F;

    @Builder.Default
    @Column(nullable = false)
    private Boolean opcional = false;

    /** Copia sin id ni ficha (para duplicar versiones). */
    public LineaBOM copia() {
        return toBuilder().id(null).ficha(null).build();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
