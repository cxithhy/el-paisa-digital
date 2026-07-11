package com.elpaisa.dao;

import com.elpaisa.model.Receta;

import java.util.List;

public interface RecetaDao extends GenericDao<Receta, Integer> {
    List<Receta> buscarPorProducto(Integer idProducto);
}
