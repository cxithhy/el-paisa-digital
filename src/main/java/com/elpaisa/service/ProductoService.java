package com.elpaisa.service;

import com.elpaisa.model.Producto;

import java.util.List;

public interface ProductoService {
    Producto crear(Producto producto);
    Producto actualizar(Producto producto);
    Producto obtenerPorId(Integer id);
    List<Producto> listarTodos();
    void eliminar(Integer id);
}
