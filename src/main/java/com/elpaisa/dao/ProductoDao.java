package com.elpaisa.dao;

import com.elpaisa.model.Producto;

public interface ProductoDao extends GenericDao<Producto, Integer> {
    Producto actualizar(Producto producto);
}
