package com.elpaisa.controller;

import com.elpaisa.service.ReporteService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** RF04: descarga del reporte de ventas en Excel (Apache POI). */
@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ventas/excel")
    public ResponseEntity<InputStreamResource> descargarReporteVentas() {
        LocalDateTime desde = LocalDateTime.now().minusMonths(1);
        LocalDateTime hasta = LocalDateTime.now();

        InputStreamResource archivo = new InputStreamResource(
                reporteService.generarReporteVentasExcel(desde, hasta));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_ventas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }
}
