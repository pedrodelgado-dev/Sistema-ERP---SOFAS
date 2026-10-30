/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: GeneradorPDFService
 * Descripción: Archivo fuente Java de la clase GeneradorPDFService.
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

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.erp.sofas.model.ConfigSofa;
import pe.erp.sofas.model.Usuario;

import javafx.scene.image.WritableImage;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class GeneradorPDFService {

    private final CalculoPrecioService precioService;
    private final PersistenciaConfiguracionService configService;

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generarPresupuesto(ConfigSofa config, String nombreCliente, Usuario usuario) {
        return generarPresupuesto(config, nombreCliente, usuario, null);
    }

    public byte[] generarPresupuesto(ConfigSofa config, String nombreCliente, Usuario usuario, WritableImage imagenSofa) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Encabezado con logo y datos empresa
            agregarEncabezado(doc);

            // Datos del cliente y presupuesto
            agregarDatosPresupuesto(doc, nombreCliente);

            // Especificaciones del sofá
            agregarEspecificaciones(doc, config);

            // Imagen del sofá (si está disponible)
            if (imagenSofa != null) {
                agregarImagen(doc, imagenSofa);
            }

            // Desglose de precios
            agregarDesglosePrecio(doc, config);

            // Términos y condiciones
            agregarTerminos(doc);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    private void agregarEncabezado(Document doc) throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);

        // Logo/Nombre empresa
        PdfPCell celdaEmpresa = new PdfPCell();
        celdaEmpresa.addElement(new Paragraph("SISTEMA ERP SOFAS SOFÁS",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        celdaEmpresa.addElement(new Paragraph("Fábrica y tienda de sofás premium",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        celdaEmpresa.addElement(new Paragraph("Juliaca, Puno - Perú",
            FontFactory.getFont(FontFactory.HELVETICA, 9)));
        tabla.addCell(celdaEmpresa);

        // Datos contacto
        PdfPCell celdaContacto = new PdfPCell();
        celdaContacto.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaContacto.addElement(new Paragraph("PRESUPUESTO",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        celdaContacto.addElement(new Paragraph("Fecha: " + LocalDate.now().format(FECHA_FMT),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tabla.addCell(celdaContacto);

        tabla.setSpacingBefore(10);
        tabla.setSpacingAfter(20);
        doc.add(tabla);
    }

    private void agregarDatosPresupuesto(Document doc, String nombreCliente) throws DocumentException {
        Paragraph cliente = new Paragraph("Cliente: " + nombreCliente,
            FontFactory.getFont(FontFactory.HELVETICA, 11));
        cliente.setSpacingAfter(10);
        doc.add(cliente);
    }

    private void agregarEspecificaciones(Document doc, ConfigSofa config) throws DocumentException {
        doc.add(new Paragraph("ESPECIFICACIONES DEL SOFÁ",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);

        // Fila de encabezado
        PdfPCell headerCell = new PdfPCell(new Paragraph("Propiedad",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        headerCell.setBackgroundColor(new Color(200, 200, 200));
        tabla.addCell(headerCell);

        headerCell = new PdfPCell(new Paragraph("Valor",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        headerCell.setBackgroundColor(new Color(200, 200, 200));
        tabla.addCell(headerCell);

        // Datos
        agregarFilaEspecificacion(tabla, "Modelo", config.modelo().nombre());
        agregarFilaEspecificacion(tabla, "Cuerpos", String.valueOf(config.cuerpos()));
        agregarFilaEspecificacion(tabla, "Largo", config.largo() + " cm");
        agregarFilaEspecificacion(tabla, "Fondo", config.profundidad() + " cm");
        agregarFilaEspecificacion(tabla, "Alto", config.alto() + " cm");
        agregarFilaEspecificacion(tabla, "Brazo", config.brazo().nombre());
        agregarFilaEspecificacion(tabla, "Patas", config.patas().nombre());
        agregarFilaEspecificacion(tabla, "Espuma", config.espuma().nombre());
        agregarFilaEspecificacion(tabla, "Mecanismo", config.mecanismo().nombre());
        if (config.conChaise()) {
            agregarFilaEspecificacion(tabla, "Chaise", config.chaise().nombre());
        }

        tabla.setSpacingAfter(20);
        doc.add(tabla);
    }

    private void agregarFilaEspecificacion(PdfPTable tabla, String propiedad, String valor) {
        PdfPCell celda1 = new PdfPCell(new Paragraph(propiedad,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tabla.addCell(celda1);

        PdfPCell celda2 = new PdfPCell(new Paragraph(valor,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tabla.addCell(celda2);
    }

    private void agregarImagen(Document doc, WritableImage imagen) throws DocumentException {
        try {
            // Convertir WritableImage a archivo temporal
            File tempFile = File.createTempFile("sofa-", ".png");
            // Nota: en Fase 6.4 se implementaría la conversión usando ImageIO
            // Por ahora, simplemente agregamos un placeholder
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("VISTA 3D DEL SOFÁ",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            doc.add(new Paragraph("(Imagen capturada del visor 3D)",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)));
        } catch (Exception e) {
            // Ignorar error si no se puede agregar imagen
        }
    }

    private void agregarDesglosePrecio(Document doc, ConfigSofa config) throws DocumentException {
        doc.add(new Paragraph("\nDESGLOSE DE PRECIOS",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

        CalculoPrecioService.DesglosePrecio desglose = precioService.desglosar(config, java.math.BigDecimal.ZERO);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(50);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setSpacingBefore(10);

        agregarFilaPrecio(tabla, "Costo BOM", desglose.costoBOM());
        agregarFilaPrecio(tabla, "Costo M.O.", desglose.costoMO());
        agregarFilaPrecio(tabla, "Subtotal", desglose.conMargen());

        PdfPCell igvCell = new PdfPCell(new Paragraph("IGV (18%)",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tabla.addCell(igvCell);

        PdfPCell igvValor = new PdfPCell(new Paragraph("S/ " + desglose.igv(),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tabla.addCell(igvValor);

        // Total
        PdfPCell totalCell = new PdfPCell(new Paragraph("TOTAL",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        totalCell.setBackgroundColor(new Color(200, 200, 200));
        tabla.addCell(totalCell);

        PdfPCell totalValor = new PdfPCell(new Paragraph("S/ " + desglose.precioFinal(),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        totalValor.setBackgroundColor(new Color(200, 200, 200));
        tabla.addCell(totalValor);

        tabla.setSpacingAfter(20);
        doc.add(tabla);
    }

    private void agregarFilaPrecio(PdfPTable tabla, String concepto, java.math.BigDecimal monto) {
        PdfPCell celda1 = new PdfPCell(new Paragraph(concepto,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tabla.addCell(celda1);

        PdfPCell celda2 = new PdfPCell(new Paragraph("S/ " + monto,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tabla.addCell(celda2);
    }

    private void agregarTerminos(Document doc) throws DocumentException {
        doc.add(new Paragraph("\nTÉRMINOS Y CONDICIONES",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));

        Paragraph terminos = new Paragraph(
            "Este presupuesto es válido por 7 días. Los precios incluyen IGV (18%). " +
            "Sujeto a disponibilidad de materiales y cambios en el mercado. " +
            "Se requiere 50% de adelanto para iniciar la fabricación.",
            FontFactory.getFont(FontFactory.HELVETICA, 9));
        terminos.setSpacingBefore(5);
        doc.add(terminos);
    }

    public void guardarPresupuesto(ConfigSofa config, String nombreCliente, Usuario usuario,
                                   String rutaArchivo, WritableImage imagenSofa) {
        try {
            byte[] pdf = generarPresupuesto(config, nombreCliente, usuario, imagenSofa);
            FileOutputStream fos = new FileOutputStream(rutaArchivo);
            fos.write(pdf);
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Error guardando PDF: " + e.getMessage(), e);
        }
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
