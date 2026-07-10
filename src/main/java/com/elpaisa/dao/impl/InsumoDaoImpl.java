package com.elpaisa.dao.impl;

import com.elpaisa.dao.InsumoDao;
import com.elpaisa.model.Insumo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class InsumoDaoImpl extends GenericDaoImpl<Insumo, Integer> implements InsumoDao {

    public InsumoDaoImpl(EntityManager entityManager) {
        super(entityManager, Insumo.class);
    }

    @Override
    public Insumo actualizar(Insumo insumo) {
        return entityManager.merge(insumo);
    }
}
