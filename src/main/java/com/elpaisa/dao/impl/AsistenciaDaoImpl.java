package com.elpaisa.dao.impl;

import com.elpaisa.dao.AsistenciaDao;
import com.elpaisa.model.Asistencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AsistenciaDaoImpl extends GenericDaoImpl<Asistencia, Integer> implements AsistenciaDao {

    public AsistenciaDaoImpl(EntityManager entityManager) {
        super(entityManager, Asistencia.class);
    }

    @Override
    public Optional<Asistencia> buscarPorUsuarioYFecha(Integer idUsuario, LocalDate fecha) {
        try {
            Asistencia a = entityManager.createQuery(
                            "SELECT a FROM Asistencia a WHERE a.usuario.idUsuario = :idUsuario AND a.fecha = :fecha",
                            Asistencia.class)
                    .setParameter("idUsuario", idUsuario)
                    .setParameter("fecha", fecha)
                    .getSingleResult();
            return Optional.of(a);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Asistencia> listarTodasOrdenadas() {
        return entityManager.createQuery(
                        "SELECT a FROM Asistencia a ORDER BY a.fecha DESC, a.horaEntrada DESC", Asistencia.class)
                .getResultList();
    }

    @Override
    public Asistencia actualizar(Asistencia asistencia) {
        return entityManager.merge(asistencia);
    }
}
