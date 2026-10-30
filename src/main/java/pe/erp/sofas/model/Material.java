/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Material
 * Descripción: Materia prima o insumo del almacen del taller.
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

import java.util.Set;

/** Materia prima o insumo del almacen del taller. */
@Entity
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Material {

    public static final java.util.List<String> CATEGORIAS = java.util.List.of(
            "Madera", "Tableros", "Espuma", "Tapicería", "Patas", "Insumos", "Acabados", "Mecanismos", "Embalaje");

    /** Unidades que se consumen enteras (se redondean hacia arriba en la explosion). */
    private static final Set<String> ENTERAS = Set.of("unidad", "par", "juego");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String categoria;

    @Column(nullable = false, length = 20)
    private String unidad;

    @Builder.Default
    @Column(nullable = false)
    private Double stock = 0d;

    /** Suma de las reservas activas de las ordenes de produccion. */
    @Builder.Default
    @Column(nullable = false)
    private Double reservado = 0d;

    @Builder.Default
    @Column(nullable = false)
    private Double minimo = 0d;

    @Column(nullable = false)
    private Double costo;

    private String ubicacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    /** No se mantiene en stock: se compra cuando hay un pedido (viscoelastica, zocalo, motor electrico). */
    @Builder.Default
    @Column(nullable = false)
    private Boolean bajoPedido = false;

    public double getDisponible() {
        return Math.max(0, stock - reservado);
    }

    /** Bajo pedido · Agotado · Critico · Stock bajo · Normal, segun el disponible (stock − reservado). */
    public String getEstado() {
        if (Boolean.TRUE.equals(bajoPedido) && stock <= 0) return "Bajo pedido";
        double d = stock - reservado;
        if (d <= 0.0001) return "Agotado";
        if (d < minimo) return "Crítico";
        if (d < minimo * 1.3) return "Stock bajo";
        return "Normal";
    }

    public boolean isCritico() {
        String e = getEstado();
        return "Agotado".equals(e) || "Crítico".equals(e);
    }

    public boolean isBajoMinimo() {
        return !Boolean.TRUE.equals(bajoPedido) && stock - reservado < minimo;
    }

    public boolean isEntera() {
        return ENTERAS.contains(unidad);
    }

    public boolean isTela() {
        return TipoTela.porMaterial(codigo) != null;
    }

    public double getValor() {
        return costo * stock;
    }

    @Override
    public String toString() {
        return codigo + " · " + nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
