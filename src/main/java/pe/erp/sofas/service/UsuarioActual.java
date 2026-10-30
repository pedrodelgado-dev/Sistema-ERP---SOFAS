/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: UsuarioActual
 * Descripción: Usuario que firma los movimientos del kardex y las aprobaciones (lo fija el login).
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

import org.springframework.stereotype.Component;
import pe.erp.sofas.model.Rol;

/** Usuario que firma los movimientos del kardex y las aprobaciones (lo fija el login). */
@Component
public class UsuarioActual {

    private String username = "sistema";
    private String nombre = "Sistema";
    private Rol rol = Rol.GERENTE;

    public void fijar(String username, String nombre, Rol rol) {
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String username() {
        return username;
    }

    public String nombre() {
        return nombre;
    }

    public Rol rol() {
        return rol;
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
