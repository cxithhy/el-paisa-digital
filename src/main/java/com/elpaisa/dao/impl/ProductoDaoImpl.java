package com.elpaisa.dao.impl;

import com.elpaisa.dao.ProductoDao;
import com.elpaisa.model.Producto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class ProductoDaoImpl extends GenericDaoImpl<Producto, Integer> implements ProductoDao {

    public ProductoDaoImpl(EntityManager entityManager) {
        super(entityManager, Producto.class);
    }

    @Override
    public Producto actualizar(Producto producto) {
        return entityManager.merge(producto);
    }
}
