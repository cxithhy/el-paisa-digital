package com.elpaisa.service.impl;

import com.elpaisa.model.DetalleVenta;
import com.elpaisa.model.Venta;
import com.elpaisa.service.ReporteService;
import com.elpaisa.service.VentaService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RF04: Generacion de reportes de ventas exportables a Excel usando Apache POI,
 * con la identidad visual de la marca (logo, colores) y un resumen del periodo.
 */
@Service
public class ReporteServiceImpl implements ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteServiceImpl.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Mismos colores de marca que el CSS de la aplicacion (mar profundo, aji, arena)
    private static final byte[] COLOR_MAR_PROFUNDO = {(byte) 0x0E, (byte) 0x3B, (byte) 0x3E};
    private static final byte[] COLOR_AJI = {(byte) 0xD6, (byte) 0x57, (byte) 0x2C};
    private static final byte[] COLOR_ARENA = {(byte) 0xFB, (byte) 0xF6, (byte) 0xEC};
    private static final byte[] COLOR_GRIS_TEXTO = {(byte) 0x5C, (byte) 0x6B, (byte) 0x6B};

    private final VentaService ventaService;

    public ReporteServiceImpl(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @Override
    public ByteArrayInputStream generarReporteVentasExcel(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> ventas = ventaService.listarPorRangoFechas(desde, hasta);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Reporte de Ventas");
            sheet.setColumnWidth(0, 10 * 256);
            sheet.setColumnWidth(1, 18 * 256);
            sheet.setColumnWidth(2, 22 * 256);
            sheet.setColumnWidth(3, 26 * 256);
            sheet.setColumnWidth(4, 12 * 256);
            sheet.setColumnWidth(5, 16 * 256);
            sheet.setColumnWidth(6, 16 * 256);

            insertarLogo(workbook, sheet);
            escribirTitulo(workbook, sheet, desde, hasta);

            Map<String, Object> resumen = calcularResumen(ventas);
            escribirResumen(workbook, sheet, resumen);

            int filaEncabezadoTabla = 10;
            escribirEncabezadoTabla(workbook, sheet, filaEncabezadoTabla);
            int ultimaFila = escribirDetalle(workbook, sheet, ventas, filaEncabezadoTabla + 1);
            escribirTotalGeneral(workbook, sheet, ventas, ultimaFila);

            sheet.createFreezePane(0, filaEncabezadoTabla + 1);
            sheet.setAutoFilter(new CellRangeAddress(filaEncabezadoTabla, filaEncabezadoTabla, 0, 6));

            workbook.write(out);
            log.info("Reporte Excel generado: {} ventas, rango {} - {}", ventas.size(), desde, hasta);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Error generando reporte Excel", e);
            throw new UncheckedIOException("No se pudo generar el reporte de ventas", e);
        }
    }

    private void insertarLogo(XSSFWorkbook workbook, XSSFSheet sheet) {
        try (InputStream logoStream = new ClassPathResource("static/img/logo-elpaisa.png").getInputStream()) {
            byte[] bytesLogo = logoStream.readAllBytes();
            int idxImagen = workbook.addPicture(bytesLogo, Workbook.PICTURE_TYPE_PNG);

            XSSFDrawing dibujo = sheet.createDrawingPatriarch();
            XSSFClientAnchor ancla = new XSSFClientAnchor();
            ancla.setCol1(0);
            ancla.setRow1(0);
            ancla.setCol2(2);
            ancla.setRow2(5);
            dibujo.createPicture(ancla, idxImagen);
        } catch (IOException e) {
            // Si el logo no se encuentra, el reporte igual se genera sin el (no es critico).
            log.warn("No se pudo insertar el logo en el reporte Excel: {}", e.getMessage());
        }
    }

    private void escribirTitulo(XSSFWorkbook workbook, XSSFSheet sheet, LocalDateTime desde, LocalDateTime hasta) {
        Font fuenteTitulo = workbook.createFont();
        fuenteTitulo.setBold(true);
        fuenteTitulo.setFontHeightInPoints((short) 16);
        fuenteTitulo.setColor(colorPersonalizado(workbook, COLOR_MAR_PROFUNDO).getIndex());
        CellStyle estiloTitulo = workbook.createCellStyle();
        estiloTitulo.setFont(fuenteTitulo);

        Font fuenteSubtitulo = workbook.createFont();
        fuenteSubtitulo.setFontHeightInPoints((short) 11);
        fuenteSubtitulo.setColor(colorPersonalizado(workbook, COLOR_GRIS_TEXTO).getIndex());
        CellStyle estiloSubtitulo = workbook.createCellStyle();
        estiloSubtitulo.setFont(fuenteSubtitulo);

        crearCelda(sheet, 0, 2, "EL PAISA DIGITAL", estiloTitulo);
        crearCelda(sheet, 1, 2, "Reporte de Ventas", estiloSubtitulo);
        crearCelda(sheet, 2, 2, "Periodo: " + desde.format(FORMATO_FECHA_CORTA) + " al " + hasta.format(FORMATO_FECHA_CORTA), estiloSubtitulo);
        crearCelda(sheet, 3, 2, "Generado el: " + LocalDateTime.now().format(FORMATO_FECHA), estiloSubtitulo);
    }

    /** Calcula indicadores del periodo: cantidad de ventas, ingresos totales y producto mas vendido. */
    private Map<String, Object> calcularResumen(List<Venta> ventas) {
        int totalVentas = ventas.size();
        BigDecimal ingresosTotales = ventas.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> unidadesPorProducto = ventas.stream()
                .flatMap(v -> v.getDetalle().stream())
                .collect(Collectors.groupingBy(d -> d.getProducto().getNombre(), Collectors.summingInt(DetalleVenta::getCantidad)));

        Map.Entry<String, Integer> masVendido = unidadesPorProducto.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(Map.entry("Sin ventas aun", 0));

        return Map.of(
                "totalVentas", totalVentas,
                "ingresosTotales", ingresosTotales,
                "productoMasVendidoNombre", masVendido.getKey(),
                "productoMasVendidoCantidad", masVendido.getValue()
        );
    }

    private void escribirResumen(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, Object> resumen) {
        Font fuenteEtiqueta = workbook.createFont();
        fuenteEtiqueta.setBold(true);
        fuenteEtiqueta.setFontHeightInPoints((short) 10);
        CellStyle estiloEtiqueta = workbook.createCellStyle();
        estiloEtiqueta.setFont(fuenteEtiqueta);
        estiloEtiqueta.setFillForegroundColor(colorPersonalizado(workbook, COLOR_ARENA));
        estiloEtiqueta.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        aplicarBordeFino(estiloEtiqueta);

        CellStyle estiloValor = workbook.createCellStyle();
        estiloValor.setFont(defaultFont(workbook, 12, true));
        aplicarBordeFino(estiloValor);

        int fila = 7;
        crearCelda(sheet, fila, 0, "Ventas registradas", estiloEtiqueta);
        crearCelda(sheet, fila, 2, "Ingresos totales", estiloEtiqueta);
        crearCelda(sheet, fila, 4, "Producto mas vendido", estiloEtiqueta);

        crearCelda(sheet, fila + 1, 0, String.valueOf(resumen.get("totalVentas")), estiloValor);
        crearCelda(sheet, fila + 1, 2, "S/ " + resumen.get("ingresosTotales"), estiloValor);
        crearCelda(sheet, fila + 1, 4,
                resumen.get("productoMasVendidoNombre") + " (" + resumen.get("productoMasVendidoCantidad") + " uds.)",
                estiloValor);
    }

    private void escribirEncabezadoTabla(XSSFWorkbook workbook, XSSFSheet sheet, int fila) {
        Font fuenteBlanca = workbook.createFont();
        fuenteBlanca.setBold(true);
        fuenteBlanca.setColor(IndexedColors.WHITE.getIndex());
        CellStyle estiloEncabezado = workbook.createCellStyle();
        estiloEncabezado.setFont(fuenteBlanca);
        estiloEncabezado.setFillForegroundColor(colorPersonalizado(workbook, COLOR_MAR_PROFUNDO));
        estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordeFino(estiloEncabezado);

        String[] columnas = {"ID Venta", "Fecha", "Sede", "Producto", "Cantidad", "Precio Unit.", "Subtotal"};
        for (int i = 0; i < columnas.length; i++) {
            crearCelda(sheet, fila, i, columnas[i], estiloEncabezado);
        }
    }

    private int escribirDetalle(XSSFWorkbook workbook, XSSFSheet sheet, List<Venta> ventas, int filaInicio) {
        CellStyle estiloFilaPar = estiloFilaDatos(workbook, colorPersonalizado(workbook, COLOR_ARENA));
        CellStyle estiloFilaImpar = estiloFilaDatos(workbook, colorPersonalizado(workbook, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
        CellStyle estiloMonedaPar = estiloMoneda(workbook, colorPersonalizado(workbook, COLOR_ARENA));
        CellStyle estiloMonedaImpar = estiloMoneda(workbook, colorPersonalizado(workbook, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));

        int fila = filaInicio;
        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetalle()) {
                boolean esPar = (fila - filaInicio) % 2 == 0;
                CellStyle estiloTexto = esPar ? estiloFilaPar : estiloFilaImpar;
                CellStyle estiloMoneda = esPar ? estiloMonedaPar : estiloMonedaImpar;

                BigDecimal precioUnitario = detalle.getProducto().getPrecio();
                BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(detalle.getCantidad()));

                crearCelda(sheet, fila, 0, String.valueOf(venta.getIdVenta()), estiloTexto);
                crearCelda(sheet, fila, 1, venta.getFechaHora().format(FORMATO_FECHA), estiloTexto);
                crearCelda(sheet, fila, 2, venta.getSede().getNombre(), estiloTexto);
                crearCelda(sheet, fila, 3, detalle.getProducto().getNombre(), estiloTexto);
                crearCelda(sheet, fila, 4, String.valueOf(detalle.getCantidad()), estiloTexto);
                crearCeldaNumerica(sheet, fila, 5, precioUnitario.doubleValue(), estiloMoneda);
                crearCeldaNumerica(sheet, fila, 6, subtotal.doubleValue(), estiloMoneda);
                fila++;
            }
        }
        return fila;
    }

    private void escribirTotalGeneral(XSSFWorkbook workbook, XSSFSheet sheet, List<Venta> ventas, int fila) {
        Font fuenteBlanca = workbook.createFont();
        fuenteBlanca.setBold(true);
        fuenteBlanca.setColor(IndexedColors.WHITE.getIndex());
        CellStyle estiloTotal = workbook.createCellStyle();
        estiloTotal.setFont(fuenteBlanca);
        estiloTotal.setFillForegroundColor(colorPersonalizado(workbook, COLOR_AJI));
        estiloTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        aplicarBordeFino(estiloTotal);

        CellStyle estiloTotalMoneda = workbook.createCellStyle();
        estiloTotalMoneda.setFont(fuenteBlanca);
        estiloTotalMoneda.setFillForegroundColor(colorPersonalizado(workbook, COLOR_AJI));
        estiloTotalMoneda.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloTotalMoneda.setDataFormat(workbook.createDataFormat().getFormat("\"S/\" #,##0.00"));
        aplicarBordeFino(estiloTotalMoneda);

        BigDecimal totalGeneral = ventas.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        for (int i = 0; i <= 5; i++) {
            crearCelda(sheet, fila, i, i == 3 ? "TOTAL GENERAL" : "", estiloTotal);
        }
        crearCeldaNumerica(sheet, fila, 6, totalGeneral.doubleValue(), estiloTotalMoneda);
    }

    // ---- Utilidades de estilo ----

    private XSSFColor colorPersonalizado(XSSFWorkbook workbook, byte[] rgb) {
        return new XSSFColor(rgb, null);
    }

    private Font defaultFont(XSSFWorkbook workbook, int tamano, boolean negrita) {
        Font fuente = workbook.createFont();
        fuente.setFontHeightInPoints((short) tamano);
        fuente.setBold(negrita);
        return fuente;
    }

    private void aplicarBordeFino(CellStyle estilo) {
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
    }

    private CellStyle estiloFilaDatos(XSSFWorkbook workbook, XSSFColor colorFondo) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(colorFondo);
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        aplicarBordeFino(estilo);
        return estilo;
    }

    private CellStyle estiloMoneda(XSSFWorkbook workbook, XSSFColor colorFondo) {
        CellStyle estilo = estiloFilaDatos(workbook, colorFondo);
        estilo.setDataFormat(workbook.createDataFormat().getFormat("\"S/\" #,##0.00"));
        return estilo;
    }

    private void crearCelda(Sheet sheet, int fila, int columna, String valor, CellStyle estilo) {
        Row row = sheet.getRow(fila);
        if (row == null) {
            row = sheet.createRow(fila);
        }
        Cell celda = row.createCell(columna);
        celda.setCellValue(valor);
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }

    private void crearCeldaNumerica(Sheet sheet, int fila, int columna, double valor, CellStyle estilo) {
        Row row = sheet.getRow(fila);
        if (row == null) {
            row = sheet.createRow(fila);
        }
        Cell celda = row.createCell(columna);
        celda.setCellValue(valor);
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }
}
