/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Tela
 * Descripción: Color del muestrario. Los metros suman al material de inventario de su tipo de tela.
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

/** Color del muestrario. Los metros suman al material de inventario de su tipo de tela. */
@Entity
@Table(name = "tela")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Tela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTela tipo;

    /** Nombre del color: "Gris perla". */
    @Column(nullable = false)
    private String color;

    @Column(nullable = false, length = 9)
    private String hex;

    /** Codigo del proveedor para el color (CH-210 GP). */
    private String codigoProveedor;

    @Builder.Default
    @Column(nullable = false)
    private Double stock = 0d;

    @Builder.Default
    @Column(nullable = false)
    private Double minimo = 8d;

    @Builder.Default
    @Column(nullable = false)
    private Boolean descontinuada = false;

    /** Disponible · Stock bajo · Agotado · Descontinuada. */
    public String getEstado() {
        if (Boolean.TRUE.equals(descontinuada)) return "Descontinuada";
        if (stock <= 0) return "Agotado";
        if (stock < minimo) return "Stock bajo";
        return "Disponible";
    }

    public String getNombre() {
        return tipo.nombre() + " " + color;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
