/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Producto
 * Descripción: Modelo de sofa del catalogo.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Modelo de sofa del catalogo. */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Producto {

    public static final List<String> CATEGORIAS = List.of(
            "Sofá", "Juego de sala", "Seccional", "Sillón", "Sofá cama", "Chesterfield", "Reclinable", "Puff");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Forma forma;

    /** Material de tapiz por defecto ("Chenille"). */
    private String material;

    /** Color por defecto ("Gris perla"). */
    private String color;

    /** Hex del color por defecto, para el render 3D. */
    private String colorHex;

    @Builder.Default
    private Integer largo = 0;
    @Builder.Default
    private Integer fondo = 0;
    @Builder.Default
    private Integer alto = 0;
    @Builder.Default
    private Integer cuerpos = 3;

    /** Texto libre que reemplaza a L × F × A (juegos: "3 + 2 + 1 cuerpos"). */
    private String medidasTexto;

    @Column(precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 600)
    private String descripcion;

    /** Rutas de imagen: principal + 4 miniaturas separadas por "|" (vacio = sin foto). */
    @Column(length = 1500)
    private String imagenes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    /** 0 → A pedido; 1–2 → Stock bajo; 3 o mas → Disponible. */
    public String getEstado() {
        return estadoPara(stock);
    }

    public static String estadoPara(int stock) {
        if (stock <= 0) return "A pedido";
        if (stock <= 2) return "Stock bajo";
        return "Disponible";
    }

    public String getMedidas() {
        if (medidasTexto != null && !medidasTexto.isBlank()) return medidasTexto;
        return largo + " × " + fondo + " × " + alto + " cm";
    }

    public BigDecimal getMargen() {
        if (costo == null || precio == null || precio.signum() == 0) return BigDecimal.ZERO;
        return precio.subtract(costo).multiply(BigDecimal.valueOf(100)).divide(precio, 1, java.math.RoundingMode.HALF_UP);
    }

    /** Lista de 5 posiciones (principal + 4); null donde no hay imagen. */
    public List<String> getListaImagenes() {
        List<String> l = new ArrayList<>(Arrays.asList(new String[5]));
        if (imagenes != null && !imagenes.isEmpty()) {
            String[] p = imagenes.split("\\|", -1);
            for (int i = 0; i < Math.min(5, p.length); i++) l.set(i, p[i].isBlank() ? null : p[i]);
        }
        return l;
    }

    public void setImagen(int indice, String ruta) {
        List<String> l = getListaImagenes();
        l.set(indice, ruta);
        imagenes = String.join("|", l.stream().map(s -> s == null ? "" : s).toList());
    }

    public String getImagenPrincipal() {
        return getListaImagenes().get(0);
    }

    @Override
    public String toString() {
        return nombre;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
