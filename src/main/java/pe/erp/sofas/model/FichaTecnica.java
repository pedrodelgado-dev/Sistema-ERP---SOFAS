/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: FichaTecnica
 * Descripción: Ficha tecnica (BOM + mano de obra) de un modelo. Cada version tiene un uid "FT-001v3";
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ficha tecnica (BOM + mano de obra) de un modelo. Cada version tiene un uid "FT-001v3";
 * las OP guardan el uid de la version con la que se planificaron.
 */
@Entity
@Table(name = "ficha_tecnica", uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "version"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class FichaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, unique = true, length = 16)
    private String uid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoFicha estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private String nombre;

    /** Medidas base L0 / F0 / A0 (cm) y cuerpos n0. */
    private Integer largo;
    private Integer fondo;
    private Integer alto;
    @Builder.Default
    private Integer cuerpos = 3;

    @Builder.Default
    private Boolean chaise = false;

    /** Recto · Seccional · Chesterfield · Puff (para el despiece de la hoja de corte). */
    private String tipoCorte;

    @Builder.Default
    private Double indirectosPct = 6d;

    private String aprobadoPor;
    private LocalDate fechaAprobacion;

    @Column(length = 500)
    private String notas;

    @Builder.Default
    private Boolean compuesta = false;

    /** Codigos de las fichas hijas (juego 3-2-1: FT-001, FT-002, FT-003). */
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ficha_hijo", joinColumns = @JoinColumn(name = "ficha_id"))
    @OrderColumn(name = "orden")
    @Column(name = "codigo_hijo", length = 10)
    @Fetch(FetchMode.SUBSELECT)
    private List<String> hijos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Fetch(FetchMode.SUBSELECT)
    private List<LineaBOM> lineas = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Fetch(FetchMode.SUBSELECT)
    private List<OperacionMO> operaciones = new ArrayList<>();

    public static String uid(String codigo, int version) {
        return codigo + "v" + version;
    }

    /** "FT-001 v3". */
    public String getEtiqueta() {
        return codigo + " v" + version;
    }

    public boolean isEditable() {
        return estado == EstadoFicha.BORRADOR;
    }

    public void agregarLinea(LineaBOM l) {
        l.setFicha(this);
        l.setOrden(lineas.size());
        lineas.add(l);
    }

    public void agregarOperacion(OperacionMO o) {
        o.setFicha(this);
        o.setOrden(operaciones.size());
        operaciones.add(o);
    }

    public double getHorasMO() {
        return operaciones.stream().mapToDouble(OperacionMO::getHoras).sum();
    }

    @Override
    public String toString() {
        return getEtiqueta() + " · " + nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
