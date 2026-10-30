/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Navigator
 * Descripción: Navegacion entre modulos con historial (boton Regresar) e intenciones:
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
package pe.erp.sofas.core;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Navegacion entre modulos con historial (boton Regresar) e intenciones:
 * {@code navigator.goTo("pedidos", Intent.NEW)} abre Pedidos con el formulario de nuevo pedido.
 */
@Component
@Lazy
public class Navigator {

    /** Implementado por el shell. */
    public interface Host {
        void mostrar(Modulo modulo, Intent intent, boolean registrarHistorial);

        boolean regresar();

        void refrescarContadores();

        void cerrarSesion();
    }

    private Host host;

    public void registrar(Host host) {
        this.host = host;
    }

    public void goTo(String modulo) {
        goTo(modulo, Intent.NONE);
    }

    public void goTo(String modulo, Intent intent) {
        if (host != null) host.mostrar(Modulo.porClave(modulo), intent, true);
    }

    public boolean back() {
        return host != null && host.regresar();
    }

    /** Pide confirmacion y vuelve al login. */
    public void cerrarSesion() {
        if (host != null) host.cerrarSesion();
    }

    /** Recalcula los contadores del menu lateral y la campana tras un cambio de datos. */
    public void datosCambiaron() {
        if (host != null) host.refrescarContadores();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
