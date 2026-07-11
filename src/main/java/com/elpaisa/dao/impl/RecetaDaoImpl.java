package com.elpaisa.dao.impl;

import com.elpaisa.dao.RecetaDao;
import com.elpaisa.model.Receta;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RecetaDaoImpl extends GenericDaoImpl<Receta, Integer> implements RecetaDao {

    public RecetaDaoImpl(EntityManager entityManager) {
        super(entityManager, Receta.class);
    }

    @Override
    public List<Receta> buscarPorProducto(Integer idProducto) {
        return entityManager.createQuery(
                        "SELECT r FROM Receta r WHERE r.producto.idProducto = :idProducto", Receta.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }
}
