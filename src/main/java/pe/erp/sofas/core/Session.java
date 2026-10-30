/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: Session
 * Descripción: Usuario con sesion iniciada (tambien firma los movimientos del kardex).
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

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.model.Rol;
import pe.erp.sofas.model.Usuario;
import pe.erp.sofas.service.UsuarioActual;

/** Usuario con sesion iniciada (tambien firma los movimientos del kardex). */
@Component
@Lazy
@RequiredArgsConstructor
public class Session {

    private final UsuarioActual usuarioActual;
    private Usuario usuario;

    public Usuario usuario() {
        return usuario;
    }

    public void iniciar(Usuario usuario) {
        this.usuario = usuario;
        usuarioActual.fijar(usuario.getUsername(), usuario.getNombre(), usuario.getRol());
    }

    public void cerrar() {
        this.usuario = null;
    }

    public String nombre() {
        return usuario == null ? "Carlos Mendoza" : usuario.getNombre();
    }

    public String cargo() {
        return usuario == null ? "Gerente General" : usuario.getCargo();
    }

    public Rol rol() {
        return usuario == null ? Rol.GERENTE : usuario.getRol();
    }

    public String username() {
        return usuario == null ? "cmendoza" : usuario.getUsername();
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
