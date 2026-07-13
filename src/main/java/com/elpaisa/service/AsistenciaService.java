package com.elpaisa.service;

import com.elpaisa.model.Asistencia;
import com.elpaisa.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface AsistenciaService {
    Asistencia marcarEntrada(Usuario usuario);
    Asistencia marcarSalida(Usuario usuario);
    Optional<Asistencia> obtenerDeHoy(Usuario usuario);
    List<Asistencia> listarTodas();
}
