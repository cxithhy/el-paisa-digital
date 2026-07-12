package com.elpaisa.dao.impl;

import com.elpaisa.dao.ReservaDao;
import com.elpaisa.model.Reserva;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReservaDaoImpl extends GenericDaoImpl<Reserva, Integer> implements ReservaDao {

    public ReservaDaoImpl(EntityManager entityManager) {
        super(entityManager, Reserva.class);
    }

    @Override
    public List<Reserva> listarProximas() {
        return entityManager.createQuery(
                        "SELECT r FROM Reserva r WHERE r.fechaHora >= :ahora ORDER BY r.fechaHora ASC", Reserva.class)
                .setParameter("ahora", LocalDateTime.now().minusHours(2))
                .getResultList();
    }

    /**
     * Muestra TODAS las reservas (pasadas y futuras), no solo las proximas.
     * Se agrego porque el personal necesita ver el historial completo, no solo
     * las pendientes de confirmar - de lo contrario una reserva ya pasada
     * "desaparece" de la vista aunque siga existiendo en la base de datos.
     */
    @Override
    public List<Reserva> listarTodasOrdenadas() {
        return entityManager.createQuery(
                        "SELECT r FROM Reserva r ORDER BY r.fechaHora DESC", Reserva.class)
                .getResultList();
    }

    @Override
    public Reserva actualizar(Reserva reserva) {
        return entityManager.merge(reserva);
    }
}
