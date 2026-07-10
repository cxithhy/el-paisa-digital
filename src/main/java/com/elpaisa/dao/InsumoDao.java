package com.elpaisa.dao;

import com.elpaisa.model.Insumo;

public interface InsumoDao extends GenericDao<Insumo, Integer> {
    Insumo actualizar(Insumo insumo);
}
