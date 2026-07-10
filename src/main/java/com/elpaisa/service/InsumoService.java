package com.elpaisa.service;

import com.elpaisa.model.Insumo;

import java.util.List;

public interface InsumoService {
    Insumo crear(Insumo insumo);
    Insumo actualizar(Insumo insumo);
    List<Insumo> listarTodos();
    List<Insumo> listarConStockBajo();
}
