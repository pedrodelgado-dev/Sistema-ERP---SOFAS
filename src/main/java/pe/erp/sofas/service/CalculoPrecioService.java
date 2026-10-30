/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CalculoPrecioService
 * Descripción: Archivo fuente Java de la clase CalculoPrecioService.
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
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.FichaTecnicaRepository;
import pe.erp.sofas.repository.TelaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalculoPrecioService {

    private final FichaTecnicaRepository fichaTecnicaRepo;
    private final TelaRepository telaRepo;

    private static final BigDecimal COSTO_HORA_MO = BigDecimal.valueOf(45);  // S/ 45 por hora
    private static final BigDecimal MARGEN_BASE = BigDecimal.valueOf(1.40);  // 40% de margen
    private static final BigDecimal IVA = BigDecimal.valueOf(1.18);  // 18% IGV

    public BigDecimal calcularPrecio(ConfigSofa config) {
        return calcularPrecio(config, BigDecimal.ZERO);
    }

    public BigDecimal calcularPrecio(ConfigSofa config, BigDecimal descuentoPct) {
        try {
            BigDecimal costoBOM = calcularCostoBOM(config);
            BigDecimal costoMO = calcularCostoMO(config);
            BigDecimal costoTotal = costoBOM.add(costoMO);

            // Aplicar margen
            BigDecimal conMargen = costoTotal.multiply(MARGEN_BASE);

            // Aplicar descuento
            if (descuentoPct.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal factorDescuento = BigDecimal.ONE.subtract(descuentoPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                conMargen = conMargen.multiply(factorDescuento);
            }

            // Aplicar IGV
            BigDecimal conIGV = conMargen.multiply(IVA);

            return conIGV.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("Error calculando precio: " + e.getMessage(), e);
        }
    }

    private BigDecimal calcularCostoBOM(ConfigSofa config) {
        FichaTecnica ficha = obtenerFichaTecnica(config);
        if (ficha == null) {
            return BigDecimal.valueOf(800);  // Precio base por defecto
        }

        BigDecimal costoBOM = BigDecimal.ZERO;

        // Sumar costos de materiales
        for (LineaBOM linea : ficha.getLineas()) {
            if (linea.getMaterial() != null && linea.getMaterial().getCosto() != null) {
                // Ajustar cantidad por número de cuerpos si aplica
                BigDecimal cantidad = BigDecimal.valueOf(linea.getCantidad() != null ? linea.getCantidad() : 1);
                // Escalado por fondo aumenta con más cuerpos
                if ("F".equals(linea.getDependencia().toString())) {
                    cantidad = cantidad.multiply(BigDecimal.valueOf(config.cuerpos()));
                }
                costoBOM = costoBOM.add(BigDecimal.valueOf(linea.getMaterial().getCosto()).multiply(cantidad));
            }
        }

        // Agregar recargas por opcionales
        costoBOM = costoBOM.add(calcularRecargosOpcionales(config));

        return costoBOM;
    }

    private BigDecimal calcularRecargosOpcionales(ConfigSofa config) {
        BigDecimal recargas = BigDecimal.ZERO;

        // Recargo por tipo de espuma
        if (config.espuma() == ConfigSofa.Espuma.D30) {
            recargas = recargas.add(BigDecimal.valueOf(80));
        } else if (config.espuma() == ConfigSofa.Espuma.VISCO) {
            recargas = recargas.add(BigDecimal.valueOf(150));
        }

        // Recargo por patas
        ConfigSofa.Patas patas = config.patas();
        if (patas == ConfigSofa.Patas.METAL_NEGRO) {
            recargas = recargas.add(BigDecimal.valueOf(14 * 4));  // 4 patas
        } else if (patas == ConfigSofa.Patas.METAL_DORADO) {
            recargas = recargas.add(BigDecimal.valueOf(20 * 4));
        } else if (patas == ConfigSofa.Patas.ZOCALO) {
            recargas = recargas.add(BigDecimal.valueOf(60));
        }

        // Recargo por mecanismo
        if (config.mecanismo() == ConfigSofa.Mecanismo.RECLINABLE_MANUAL) {
            recargas = recargas.add(BigDecimal.valueOf(250));
        } else if (config.mecanismo() == ConfigSofa.Mecanismo.RECLINABLE_ELECTRICO) {
            recargas = recargas.add(BigDecimal.valueOf(600));
        } else if (config.mecanismo() == ConfigSofa.Mecanismo.CAMA) {
            recargas = recargas.add(BigDecimal.valueOf(300));
        }

        // Recargo por chaise
        if (config.conChaise()) {
            recargas = recargas.add(BigDecimal.valueOf(200));
        }

        // Recargo por tela premium (según tipo de tela)
        if (config.telaId() != null) {
            Optional<Tela> tela = telaRepo.findById(config.telaId());
            if (tela.isPresent() && tela.get().getTipo() != null) {
                double recargoPct = tela.get().getTipo().recargoPct();
                if (recargoPct > 0) {
                    recargas = recargas.add(BigDecimal.valueOf(recargoPct * 0.8));  // Aprox. S/ 0.80 por punto %
                }
            }
        }

        return recargas;
    }

    private BigDecimal calcularCostoMO(ConfigSofa config) {
        FichaTecnica ficha = obtenerFichaTecnica(config);
        if (ficha == null) {
            return BigDecimal.valueOf(150);  // Costo MO base por defecto
        }

        double horas = ficha.getHorasMO();
        // Ajustar horas por cuerpos (más cuerpos = más tiempo)
        horas = horas * (1 + (config.cuerpos() - 1) * 0.15);

        return COSTO_HORA_MO.multiply(BigDecimal.valueOf(horas)).setScale(2, RoundingMode.HALF_UP);
    }

    private FichaTecnica obtenerFichaTecnica(ConfigSofa config) {
        try {
            String codigo = config.modelo().producto();
            var fichas = fichaTecnicaRepo.findByCodigoOrderByVersionDesc(codigo);
            return fichas.isEmpty() ? null : fichas.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public DesglosePrecio desglosar(ConfigSofa config, BigDecimal descuentoPct) {
        BigDecimal costoBOM = calcularCostoBOM(config);
        BigDecimal costoMO = calcularCostoMO(config);
        BigDecimal costoTotal = costoBOM.add(costoMO);

        BigDecimal conMargen = costoTotal.multiply(MARGEN_BASE);
        BigDecimal montoDescuento = conMargen.multiply(descuentoPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal conDescuento = conMargen.subtract(montoDescuento);

        BigDecimal igv = conDescuento.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal conIGV = conDescuento.add(igv);

        return new DesglosePrecio(
            costoBOM,
            costoMO,
            conMargen,
            descuentoPct,
            conDescuento,
            igv,
            conIGV
        );
    }

    public record DesglosePrecio(
        BigDecimal costoBOM,
        BigDecimal costoMO,
        BigDecimal conMargen,
        BigDecimal descuentoPct,
        BigDecimal conDescuento,
        BigDecimal igv,
        BigDecimal precioFinal
    ) {}
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
