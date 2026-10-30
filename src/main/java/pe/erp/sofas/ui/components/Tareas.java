/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Tareas
 * Descripción: Ejecuta trabajos pesados (PDF, Excel, MRP, optimizacion de corte) en un Task con indicador de progreso.
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
package pe.erp.sofas.ui.components;

import javafx.concurrent.Task;
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.core.Overlays;
import pe.erp.sofas.core.Toast;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Ejecuta trabajos pesados (PDF, Excel, MRP, optimizacion de corte) en un Task con indicador de progreso. */
public final class Tareas {

    private Tareas() {
    }

    public static <T> void ejecutar(Overlays overlays, Toast toast, String mensaje, Supplier<T> trabajo, Consumer<T> alTerminar) {
        Overlays.Handle espera = overlays.cargando(mensaje);
        Task<T> t = new Task<>() {
            @Override
            protected T call() {
                return trabajo.get();
            }
        };
        t.setOnSucceeded(e -> {
            espera.cerrar();
            alTerminar.accept(t.getValue());
        });
        t.setOnFailed(e -> {
            espera.cerrar();
            Throwable ex = t.getException();
            while (ex != null && !(ex instanceof NegocioException) && ex.getCause() != null) ex = ex.getCause();
            toast.error(ex instanceof NegocioException ? ex.getMessage() : "No se pudo completar: " + (ex == null ? "error" : ex.getMessage()));
            if (!(ex instanceof NegocioException) && t.getException() != null) {
                org.slf4j.LoggerFactory.getLogger(Tareas.class).error("Tarea fallida", t.getException());
            }
        });
        Thread hilo = new Thread(t, "innova-tarea");
        hilo.setDaemon(true);
        hilo.start();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
