/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Carrito
 * Descripción: Archivo fuente Java de la clase Carrito.
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Builder.Default
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("fechaAnadido ASC")
    private List<CarritoItem> items = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime fechaModificacion = LocalDateTime.now();

    @PreUpdate
    void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    public void agregarItem(ConfiguracionGuardada config, Integer cantidad) {
        CarritoItem item = CarritoItem.builder()
            .carrito(this)
            .configuracion(config)
            .cantidad(cantidad)
            .build();
        items.add(item);
    }

    public void removerItem(Long itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }

    public void actualizarCantidad(Long itemId, Integer nuevaCantidad) {
        items.stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst()
            .ifPresent(item -> item.setCantidad(nuevaCantidad));
    }

    public void vaciar() {
        items.clear();
    }

    public Integer obtenerCantidadTotal() {
        return items.stream()
            .mapToInt(CarritoItem::getCantidad)
            .sum();
    }

    public Integer obtenerCantidadItems() {
        return items.size();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
