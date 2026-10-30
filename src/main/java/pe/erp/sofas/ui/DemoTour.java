/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: DemoTour
 * Descripción: Recorrido automatico para revisar la interfaz: {@code -Dinnova.tour=carpeta} recorre los modulos y guarda una
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
package pe.erp.sofas.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.Intent;
import pe.erp.sofas.core.Navigator;
import pe.erp.sofas.core.Session;
import pe.erp.sofas.core.ThemeManager;
import pe.erp.sofas.service.AuthService;
import pe.erp.sofas.ui.components.Snapshot;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Recorrido automatico para revisar la interfaz: {@code -Dinnova.tour=carpeta} recorre los modulos y guarda una
 * captura PNG de cada pantalla. Pasos con {@code -Dinnova.tour.steps=login,dashboard,modelos:NEW,pedidos:OPEN=PE-0241,theme:dark}.
 */
@Component
@Lazy
@RequiredArgsConstructor
@Slf4j
public class DemoTour {

    private static final String PASOS = "login,dashboard,modelos,telas,configurador,sala,produccion,fichas,mrp,corte,consumo,"
            + "pedidos,ventas,clientes,posventa,inventario,proveedores,compras,personal,finanzas,reportes,configuracion,theme:dark,dashboard,produccion";

    private final AppWindow appWindow;
    private final AuthService authService;
    private final Session session;
    private final Navigator navigator;
    private final ThemeManager themeManager;

    public void ejecutar(String carpeta) {
        File dir = new File(carpeta);
        dir.mkdirs();
        String[] pasos = System.getProperty("innova.tour.steps", PASOS).split(",");
        List<Runnable> acciones = new ArrayList<>();
        int n = 0;
        for (String paso : pasos) {
            String nombre = String.format("%02d-%s", ++n, paso.replaceAll("[^a-zA-Z0-9-]", "_"));
            acciones.add(() -> aplicar(paso.trim()));
            acciones.add(() -> capturar(new File(dir, nombre + ".png")));
        }
        acciones.add(Platform::exit);
        encadenar(acciones, 0, Double.parseDouble(System.getProperty("innova.tour.delay", "1500")));
    }

    private void encadenar(List<Runnable> acciones, int i, double espera) {
        if (i >= acciones.size()) return;
        PauseTransition p = new PauseTransition(Duration.millis(i % 2 == 0 ? 300 : espera));
        p.setOnFinished(e -> {
            try {
                acciones.get(i).run();
            } catch (RuntimeException ex) {
                log.error("Paso del recorrido falló", ex);
            }
            encadenar(acciones, i + 1, espera);
        });
        p.play();
    }

    private void aplicar(String paso) {
        if (paso.equals("login")) return;
        if (paso.startsWith("theme:")) {
            themeManager.setOscuro(paso.endsWith("dark"));
            return;
        }
        if (session.usuario() == null) {
            session.iniciar(authService.login(AuthService.USUARIO_DEMO, "innova2026"));
            appWindow.mostrarShell("dashboard");
        }
        String[] partes = paso.split(":", 2);
        Intent intent = Intent.NONE;
        if (partes.length > 1) {
            String accion = partes[1];
            if (accion.equals("NEW")) intent = Intent.NEW;
            else if (accion.startsWith("OPEN=")) intent = Intent.abrir(accion.substring(5));
            else if (accion.contains("=")) intent = new Intent(accion.substring(0, accion.indexOf('=')), accion.substring(accion.indexOf('=') + 1));
            else intent = new Intent(accion, null);
        }
        navigator.goTo(partes[0], intent);
    }

    private void capturar(File destino) {
        try {
            ImageIO.write(Snapshot.imagen(appWindow.scene().snapshot(null)), "png", destino);
            log.info("Captura {}", destino);
        } catch (IOException e) {
            log.error("No se pudo guardar la captura", e);
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
