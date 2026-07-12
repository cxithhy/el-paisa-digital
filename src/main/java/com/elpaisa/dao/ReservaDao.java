package com.elpaisa.dao;

import com.elpaisa.model.Reserva;

import java.util.List;

public interface ReservaDao extends GenericDao<Reserva, Integer> {
    List<Reserva> listarProximas();
    List<Reserva> listarTodasOrdenadas();
    Reserva actualizar(Reserva reserva);
}
