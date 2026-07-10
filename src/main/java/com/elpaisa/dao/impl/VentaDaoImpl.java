package com.elpaisa.dao.impl;

import com.elpaisa.dao.VentaDao;
import com.elpaisa.model.Venta;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class VentaDaoImpl extends GenericDaoImpl<Venta, Integer> implements VentaDao {

    public VentaDaoImpl(EntityManager entityManager) {
        super(entityManager, Venta.class);
    }

    @Override
    public List<Venta> buscarEntreFechas(LocalDateTime desde, LocalDateTime hasta) {
        return entityManager.createQuery(
                        "SELECT v FROM Venta v WHERE v.fechaHora BETWEEN :desde AND :hasta ORDER BY v.fechaHora DESC",
                        Venta.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();
    }
}
