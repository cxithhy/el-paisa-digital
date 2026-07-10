package com.elpaisa.dao.impl;

import com.elpaisa.dao.SedeDao;
import com.elpaisa.model.Sede;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class SedeDaoImpl extends GenericDaoImpl<Sede, Integer> implements SedeDao {

    public SedeDaoImpl(EntityManager entityManager) {
        super(entityManager, Sede.class);
    }
}
