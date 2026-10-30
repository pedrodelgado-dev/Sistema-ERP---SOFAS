/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ParametroService
 * Descripción: Parametros de produccion: mermas por defecto, costos por hora, indirectos, margen objetivo y destajo.
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
import pe.erp.sofas.model.Etapa;
import pe.erp.sofas.model.EtapaMO;
import pe.erp.sofas.model.Parametro;
import pe.erp.sofas.repository.ParametroRepository;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parametros de produccion: mermas por defecto, costos por hora, indirectos, margen objetivo y destajo. */
@Service
@RequiredArgsConstructor
public class ParametroService {

    public static final String INDIRECTOS = "produccion.indirectos";
    public static final String MARGEN_OBJETIVO = "produccion.margen";
    public static final Map<String, Double> MERMAS = new LinkedHashMap<>();

    static {
        MERMAS.put("Madera", 18d);
        MERMAS.put("Espuma", 12d);
        MERMAS.put("Tela", 10d);
        MERMAS.put("Tableros", 8d);
        MERMAS.put("Insumos", 5d);
    }

    /** Tarifa por sofa del pago a destajo por etapa (S/). */
    public static final Map<Etapa, Double> DESTAJO = new LinkedHashMap<>();

    static {
        DESTAJO.put(Etapa.CORTE_MADERA, 15d);
        DESTAJO.put(Etapa.ARMADO, 45d);
        DESTAJO.put(Etapa.CINCHADO, 15d);
        DESTAJO.put(Etapa.ESPUMADO, 20d);
        DESTAJO.put(Etapa.CORTE_TELA, 18d);
        DESTAJO.put(Etapa.COSTURA, 40d);
        DESTAJO.put(Etapa.TAPIZADO, 60d);
        DESTAJO.put(Etapa.ACABADO, 15d);
        DESTAJO.put(Etapa.CONTROL_CALIDAD, 8d);
    }

    private final ParametroRepository repository;

    public double numero(String clave, double defecto) {
        return repository.findById(clave).map(p -> {
            try {
                return Double.parseDouble(p.getValor());
            } catch (NumberFormatException e) {
                return defecto;
            }
        }).orElse(defecto);
    }

    @Transactional
    public void fijar(String clave, double valor) {
        repository.save(new Parametro(clave, String.valueOf(valor)));
    }

    public double indirectos() {
        return numero(INDIRECTOS, 6);
    }

    public double margenObjetivo() {
        return numero(MARGEN_OBJETIVO, 35);
    }

    public double merma(String tipo) {
        return numero("merma." + tipo, MERMAS.getOrDefault(tipo, 5d));
    }

    public double costoHora(EtapaMO etapa) {
        return numero("costoHora." + etapa.name(), etapa.costoHora());
    }

    public double tarifaDestajo(Etapa etapa) {
        return numero("destajo." + etapa.name(), DESTAJO.getOrDefault(etapa, 0d));
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
