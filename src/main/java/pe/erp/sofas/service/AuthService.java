/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: AuthService
 * Descripción: Inicio de sesion con contrasenas BCrypt (jBCrypt). En modo prototipo ({@code app.auth.modo-prototipo=true})
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
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClock;
import pe.erp.sofas.core.NegocioException;
import pe.erp.sofas.model.Usuario;
import pe.erp.sofas.repository.UsuarioRepository;

/**
 * Inicio de sesion con contrasenas BCrypt (jBCrypt). En modo prototipo ({@code app.auth.modo-prototipo=true})
 * cualquier credencial no vacia ingresa como el usuario demo.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String USUARIO_DEMO = "cmendoza";

    private final UsuarioRepository repository;
    private final UsuarioActual usuarioActual;

    @Value("${app.auth.modo-prototipo:false}")
    private boolean modoPrototipo;

    @Transactional
    public Usuario login(String username, String password) {
        String user = username == null ? "" : username.trim();
        if (user.isEmpty() || password == null || password.isEmpty()) throw new NegocioException("Ingresa tu usuario y contraseña.");
        var encontrado = repository.findByUsernameIgnoreCase(user);
        Usuario u;
        if (modoPrototipo) {
            u = encontrado.filter(Usuario::getActivo).orElseGet(() -> repository.findByUsernameIgnoreCase(USUARIO_DEMO).orElseThrow());
        } else {
            u = encontrado.orElseThrow(() -> new NegocioException("Usuario o contraseña incorrectos."));
            if (!u.getActivo()) throw new NegocioException("El usuario está desactivado.");
            if (!verificar(password, u.getPasswordHash())) throw new NegocioException("Usuario o contraseña incorrectos.");
        }
        u.setUltimoAcceso(AppClock.hoy());
        usuarioActual.fijar(u.getUsername(), u.getNombre(), u.getRol());
        return repository.save(u);
    }

    /** Valida la clave de un Supervisor (o Gerente/Administrador) para autorizar una excepcion. */
    public Usuario autorizar(String username, String password) {
        Usuario u = repository.findByUsernameIgnoreCase(username == null ? "" : username.trim())
                .orElseThrow(() -> new NegocioException("Usuario no encontrado."));
        if (!u.getRol().autoriza()) throw new NegocioException(u.getNombre() + " no puede autorizar: se requiere un Supervisor.");
        if (!verificar(password == null ? "" : password, u.getPasswordHash())) throw new NegocioException("Contraseña de autorización incorrecta.");
        return u;
    }

    public java.util.List<Usuario> autorizadores() {
        return repository.findAll().stream().filter(u -> u.getActivo() && u.getRol().autoriza()).toList();
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public static boolean verificar(String password, String hash) {
        try {
            return hash != null && BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
