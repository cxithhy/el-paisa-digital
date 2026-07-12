package com.elpaisa.service;

import com.elpaisa.model.Reserva;

import java.util.List;

public interface ReservaService {
    Reserva crear(Reserva reserva);
    List<Reserva> listarProximas();
    List<Reserva> listarTodasOrdenadas();
    Reserva cambiarEstado(Integer id, String nuevoEstado);
}
