package com.elpaisa.service.impl;

import com.elpaisa.dao.ReservaDao;
import com.elpaisa.exception.RecursoNoEncontradoException;
import com.elpaisa.model.Reserva;
import com.elpaisa.service.ReservaService;
import com.elpaisa.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaServiceImpl implements ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaServiceImpl.class);

    // Horario general: 8:00am - 5:00pm. Huanchaco abre una hora despues (9:00am).
    private static final LocalTime APERTURA_GENERAL = LocalTime.of(8, 0);
    private static final LocalTime APERTURA_HUANCHACO = LocalTime.of(9, 0);
    private static final LocalTime CIERRE = LocalTime.of(17, 0);

    private final ReservaDao reservaDao;

    public ReservaServiceImpl(ReservaDao reservaDao) {
        this.reservaDao = reservaDao;
    }

    @Override
    @Transactional
    public Reserva crear(Reserva reserva) {
        ValidacionUtil.validarTextoNoVacio(reserva.getNombreCliente(), "nombreCliente");
        ValidacionUtil.validarTelefonoPeruMovil(reserva.getTelefonoCliente(), "telefonoCliente");
        ValidacionUtil.validarCantidadPositiva(reserva.getNumeroPersonas(), "numeroPersonas");

        if (reserva.getSede() == null) {
            throw new IllegalArgumentException("Debe seleccionar una sede.");
        }
        LocalTime apertura = "Huanchaco".equalsIgnoreCase(reserva.getSede().getNombre())
                ? APERTURA_HUANCHACO : APERTURA_GENERAL;
        ValidacionUtil.validarFechaHoraReserva(reserva.getFechaHora(), apertura, CIERRE);

        reserva.setEstado("PENDIENTE");
        Reserva guardada = reservaDao.guardar(reserva);
        log.info("Reserva creada: id={}, cliente={}, fecha={}", guardada.getIdReserva(),
                guardada.getNombreCliente(), guardada.getFechaHora());
        return guardada;
    }

    @Override
    public List<Reserva> listarProximas() {
        return reservaDao.listarProximas();
    }

    @Override
    public List<Reserva> listarTodasOrdenadas() {
        return reservaDao.listarTodasOrdenadas();
    }

    @Override
    @Transactional
    public Reserva cambiarEstado(Integer id, String nuevoEstado) {
        Reserva reserva = reservaDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: id=" + id));
        reserva.setEstado(nuevoEstado);
        log.info("Reserva id={} cambio de estado a {}", id, nuevoEstado);
        return reservaDao.actualizar(reserva);
    }
}
