package com.elpaisa.dao;

import com.elpaisa.model.Venta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaDao extends GenericDao<Venta, Integer> {
    List<Venta> buscarEntreFechas(LocalDateTime desde, LocalDateTime hasta);
}
