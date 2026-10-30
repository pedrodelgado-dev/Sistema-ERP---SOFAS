/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: LineaExtra
 * Descripción: Material adicional a la ficha (extras del configurador: chaise, cojines, mecanismos...). Por unidad.
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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/** Material adicional a la ficha (extras del configurador: chaise, cojines, mecanismos...). Por unidad. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaExtra {

    @Column(name = "material_codigo", length = 20)
    private String materialCodigo;

    @Column(length = 20)
    private String componente;

    private Double cantidad;
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
