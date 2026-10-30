/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: InventarioService
 * Descripción: Materiales del almacen y kardex. Reserva, Liberacion y Retazo no alteran el saldo fisico.
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
package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClock;
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.MaterialRepository;
import pe.erp.sofas.repository.MovimientoRepository;
import pe.erp.sofas.repository.TelaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Materiales del almacen y kardex. Reserva, Liberacion y Retazo no alteran el saldo fisico. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioService {

    public static final List<String> UNIDADES = List.of("pt", "plancha", "m", "unidad", "caja", "galón", "kg", "juego", "par");

    private final MaterialRepository materialRepository;
    private final MovimientoRepository movimientoRepository;
    private final TelaRepository telaRepository;
    private final UsuarioActual usuario;

    public List<Material> materiales() {
        return materialRepository.findAllByOrderByCodigoAsc();
    }

    public Material material(Long id) {
        return materialRepository.findById(id).orElseThrow(() -> new NegocioException("El material ya no existe."));
    }

    public Material material(String codigo) {
        return materialRepository.findByCodigo(codigo).orElseThrow(() -> new NegocioException("No existe el material " + codigo + "."));
    }

    public List<Movimiento> movimientos() {
        return movimientoRepository.findAllByOrderByFechaDescIdDesc();
    }

    public List<Movimiento> kardex(Long materialId) {
        return movimientoRepository.findByMaterialIdOrderByFechaAscIdAsc(materialId);
    }

    /** Materiales bajo el minimo (por disponible), del mas urgente al menos urgente. */
    public List<Material> bajoMinimo() {
        return materiales().stream().filter(Material::isBajoMinimo)
                .sorted(Comparator.comparingDouble(m -> m.getDisponible() / Math.max(0.01, m.getMinimo())))
                .toList();
    }

    public List<Material> criticos() {
        return materiales().stream().filter(Material::isCritico).toList();
    }

    public double valorTotal() {
        return materiales().stream().mapToDouble(Material::getValor).sum();
    }

    public long movimientosDelMes() {
        LocalDate inicio = AppClock.hoy().withDayOfMonth(1);
        return movimientoRepository.findAll().stream().filter(m -> !m.getFecha().toLocalDate().isBefore(inicio)).count();
    }

    public String siguienteCodigo() {
        return Codigos.siguiente(materialRepository.findAll(), Material::getCodigo, "MP-", 3);
    }

    @Transactional
    public Material guardarMaterial(Material material) {
        if (material.getNombre() == null || material.getNombre().isBlank()) throw new NegocioException("El nombre del material es obligatorio.");
        if (material.getCosto() == null || material.getCosto() < 0) throw new NegocioException("Ingresa un costo unitario válido.");
        if (material.getStock() < 0 || material.getMinimo() < 0) throw new NegocioException("Las cantidades no pueden ser negativas.");
        boolean nuevo = material.getId() == null;
        if (nuevo) material.setCodigo(siguienteCodigo());
        double inicial = material.getStock();
        if (nuevo) material.setStock(0d);
        Material g = materialRepository.save(material);
        if (nuevo && inicial > 0) registrar(g, TipoMovimiento.ENTRADA, inicial, "INI-" + g.getCodigo(), "Inventario inicial");
        return g;
    }

    /**
     * Entrada, salida o ajuste manual. En las telas los metros se aplican a un color del muestrario
     * (el stock del material es siempre la suma de sus colores).
     */
    @Transactional
    public Movimiento movimientoManual(Long materialId, TipoMovimiento tipo, double cantidad, Long telaId,
                                       String referencia, String motivo) {
        if (tipo != TipoMovimiento.ENTRADA && tipo != TipoMovimiento.SALIDA && tipo != TipoMovimiento.AJUSTE) {
            throw new NegocioException("Movimiento no permitido.");
        }
        if (tipo == TipoMovimiento.AJUSTE ? cantidad == 0 : cantidad <= 0) throw new NegocioException("La cantidad debe ser mayor a cero.");
        Material m = material(materialId);
        if (m.isTela()) {
            if (telaId == null) throw new NegocioException("Elige el color de la tela.");
            Tela t = telaRepository.findById(telaId).orElseThrow();
            double efecto = tipo == TipoMovimiento.SALIDA ? -cantidad : cantidad;
            if (t.getStock() + efecto < -1e-9) throw new NegocioException("El color " + t.getColor() + " solo tiene " + fmt(t.getStock()) + " m.");
            t.setStock(t.getStock() + efecto);
            telaRepository.save(t);
            motivo = (motivo == null || motivo.isBlank() ? tipo.nombre() : motivo) + " · " + t.getColor() + " " + t.getCodigo();
        }
        return registrar(m, tipo, cantidad, referencia, motivo);
    }

    /**
     * Registra un movimiento y actualiza el saldo fisico. Una salida o consumo nunca deja el stock en negativo
     * ni por debajo de lo reservado por otras ordenes (salvo el propio consumo de la OP).
     */
    @Transactional
    public Movimiento registrar(Material material, TipoMovimiento tipo, double cantidad, String referencia, String concepto) {
        double saldo = material.getStock();
        double efecto = switch (tipo) {
            case ENTRADA -> cantidad;
            case SALIDA, CONSUMO_OP -> -cantidad;
            case AJUSTE -> cantidad;
            default -> 0;
        };
        if (saldo + efecto < -1e-9) {
            throw new NegocioException("Stock insuficiente: hay " + fmt(saldo) + " " + material.getUnidad() + " de " + material.getNombre() + ".");
        }
        if (tipo == TipoMovimiento.SALIDA && saldo + efecto < material.getReservado() - 1e-9) {
            throw new NegocioException("No se puede sacar material reservado para producción: disponible "
                    + fmt(material.getDisponible()) + " " + material.getUnidad() + ".");
        }
        material.setStock(Math.max(0, saldo + efecto));
        materialRepository.save(material);
        return movimientoRepository.save(Movimiento.builder()
                .material(material).fecha(LocalDateTime.of(AppClock.hoy(), AppClock.HORA)).tipo(tipo)
                .cantidad(cantidad).saldo(material.getStock())
                .referencia(referencia == null || referencia.isBlank() ? "—" : referencia.trim())
                .concepto(concepto == null || concepto.isBlank() ? tipo.nombre() : concepto.trim())
                .usuario(usuario.username()).build());
    }

    public static String fmt(double n) {
        return n == Math.floor(n) ? String.valueOf((long) n) : String.format(java.util.Locale.US, "%.2f", n);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
