package com.elpaisa.service;

import com.elpaisa.dto.VentaRequest;
import com.elpaisa.model.Venta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    /**
     * Registra una venta y descuenta automaticamente del inventario los
     * insumos utilizados segun la receta de cada plato vendido (RF01 + RF02).
     */
    Venta registrarVenta(VentaRequest request);
    List<Venta> listarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);
}
