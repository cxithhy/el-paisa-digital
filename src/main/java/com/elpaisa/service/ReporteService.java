package com.elpaisa.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

public interface ReporteService {
    /** Genera un reporte de ventas en formato Excel (RF04) usando Apache POI. */
    ByteArrayInputStream generarReporteVentasExcel(LocalDateTime desde, LocalDateTime hasta);
}
