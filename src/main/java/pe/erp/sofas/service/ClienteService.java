/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ClienteService
 * Descripción: Total comprado = ventas + pedidos entregados; saldo = saldos de pedidos abiertos.
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
import pe.erp.sofas.repository.*;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    public record Resumen(BigDecimal totalComprado, BigDecimal saldo, long compras, long pedidos) {
    }

    private final ClienteRepository repository;
    private final VentaRepository ventaRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final RetapizadoRepository retapizadoRepository;

    public List<Cliente> listar() {
        return repository.findAllByOrderByCodigoAsc();
    }

    public Cliente cliente(Long id) {
        return repository.findById(id).orElseThrow(() -> new NegocioException("El cliente ya no existe."));
    }

    public List<Venta> ventas(Cliente c) {
        return ventaRepository.findAllByOrderByFechaDesc().stream().filter(v -> v.getCliente() != null && v.getCliente().getId().equals(c.getId())).toList();
    }

    public List<Pedido> pedidos(Cliente c) {
        return pedidoRepository.findAllByOrderByCodigoDesc().stream().filter(p -> p.getCliente().getId().equals(c.getId())).toList();
    }

    public List<Pago> pagos(Cliente c) {
        return pagoRepository.findByClienteIdOrderByFechaDesc(c.getId());
    }

    /** Total comprado = ventas + pedidos entregados; saldo = saldos de pedidos abiertos. */
    public Resumen resumen(Cliente c) {
        List<Venta> v = ventas(c);
        List<Pedido> p = pedidos(c);
        BigDecimal ventas = v.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pedidos = p.stream().filter(x -> x.getEstado() != EstadoPedido.CANCELADO).map(Pedido::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retap = retapizadoRepository.findAll().stream().filter(r -> r.getCliente().getId().equals(c.getId())
                && r.getEstado() == EstadoRetapizado.ENTREGADO && r.getTotal() != null).map(Retapizado::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = p.stream().filter(x -> !x.getEstado().cerrado()).map(Pedido::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resumen(ventas.add(pedidos).add(retap), saldo, v.size(), p.size());
    }

    public String siguienteCodigo() {
        return Codigos.siguiente(repository.findAll(), Cliente::getCodigo, "C-", 4);
    }

    @Transactional
    public Cliente guardar(Cliente c) {
        validar(c);
        boolean duplicado = listar().stream().anyMatch(x -> x.getDocumento() != null && x.getDocumento().equals(c.getDocumento())
                && !x.getId().equals(c.getId()));
        if (duplicado) throw new NegocioException("Ya existe un cliente con el documento " + c.getDocumento() + ".");
        if (c.getId() == null) {
            c.setCodigo(siguienteCodigo());
            c.setFechaRegistro(AppClock.hoy());
        }
        return repository.save(c);
    }

    /** DNI de 8 digitos para personas; RUC de 11 para empresas. */
    public static void validar(Cliente c) {
        if (c.getNombre() == null || c.getNombre().isBlank()) throw new NegocioException("El nombre es obligatorio.");
        String doc = c.getDocumento() == null ? "" : c.getDocumento().trim();
        if (c.getTipo() == Cliente.Tipo.PERSONA && !doc.matches("\\d{8}")) throw new NegocioException("El DNI debe tener 8 dígitos.");
        if (c.getTipo() == Cliente.Tipo.EMPRESA && !doc.matches("\\d{11}")) throw new NegocioException("El RUC debe tener 11 dígitos.");
        if (c.getTelefono() != null && !c.getTelefono().isBlank() && !c.getTelefono().replace(" ", "").matches("\\+?\\d{6,12}")) {
            throw new NegocioException("Revisa el teléfono.");
        }
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente c = cliente(id);
        if (!ventas(c).isEmpty() || !pedidos(c).isEmpty()) throw new NegocioException("El cliente tiene ventas o pedidos; no se puede eliminar.");
        repository.delete(c);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
