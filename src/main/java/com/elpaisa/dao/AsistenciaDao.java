package com.elpaisa.dao;

import com.elpaisa.model.Asistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaDao extends GenericDao<Asistencia, Integer> {
    Optional<Asistencia> buscarPorUsuarioYFecha(Integer idUsuario, LocalDate fecha);
    List<Asistencia> listarTodasOrdenadas();
    Asistencia actualizar(Asistencia asistencia);
}
