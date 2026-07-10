package com.elpaisa.service.impl;

import com.elpaisa.model.DetalleVenta;
import com.elpaisa.model.Venta;
import com.elpaisa.service.ReporteService;
import com.elpaisa.service.VentaService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RF04: Generacion de reportes de ventas exportables a Excel usando Apache POI.
 */
@Service
public class ReporteServiceImpl implements ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteServiceImpl.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VentaService ventaService;

    public ReporteServiceImpl(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @Override
    public ByteArrayInputStream generarReporteVentasExcel(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> ventas = ventaService.listarPorRangoFechas(desde, hasta);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Ventas");

            CellStyle estiloEncabezado = workbook.createCellStyle();
            Font fuenteNegrita = workbook.createFont();
            fuenteNegrita.setBold(true);
            estiloEncabezado.setFont(fuenteNegrita);

            Row encabezado = sheet.createRow(0);
            String[] columnas = {"ID Venta", "Fecha", "Sede", "Producto", "Cantidad", "Total Venta"};
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            int filaIdx = 1;
            for (Venta venta : ventas) {
                for (DetalleVenta detalle : venta.getDetalle()) {
                    Row fila = sheet.createRow(filaIdx++);
                    fila.createCell(0).setCellValue(venta.getIdVenta());
                    fila.createCell(1).setCellValue(venta.getFechaHora().format(FORMATO_FECHA));
                    fila.createCell(2).setCellValue(venta.getSede().getNombre());
                    fila.createCell(3).setCellValue(detalle.getProducto().getNombre());
                    fila.createCell(4).setCellValue(detalle.getCantidad());
                    fila.createCell(5).setCellValue(venta.getTotal().doubleValue());
                }
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Reporte Excel generado: {} ventas, rango {} - {}", ventas.size(), desde, hasta);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Error generando reporte Excel", e);
            throw new UncheckedIOException("No se pudo generar el reporte de ventas", e);
        }
    }
}
