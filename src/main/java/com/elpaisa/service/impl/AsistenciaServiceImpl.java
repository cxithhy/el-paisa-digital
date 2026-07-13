package com.elpaisa.service.impl;

import com.elpaisa.dao.AsistenciaDao;
import com.elpaisa.model.Asistencia;
import com.elpaisa.model.Usuario;
import com.elpaisa.service.AsistenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * RF05: control de asistencia. Cada usuario marca su propia entrada/salida;
 * un mismo dia solo puede tener un registro (idempotente por usuario+fecha).
 */
@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaServiceImpl.class);

    private final AsistenciaDao asistenciaDao;

    public AsistenciaServiceImpl(AsistenciaDao asistenciaDao) {
        this.asistenciaDao = asistenciaDao;
    }

    @Override
    @Transactional
    public Asistencia marcarEntrada(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        Asistencia asistencia = asistenciaDao.buscarPorUsuarioYFecha(usuario.getIdUsuario(), hoy)
                .orElseGet(() -> new Asistencia(null, usuario, hoy, null, null));
        if (asistencia.getHoraEntrada() == null) {
            asistencia.setHoraEntrada(LocalTime.now());
            log.info("Entrada marcada: usuario={}, hora={}", usuario.getUsername(), asistencia.getHoraEntrada());
        }
        return asistencia.getIdAsistencia() == null ? asistenciaDao.guardar(asistencia) : asistenciaDao.actualizar(asistencia);
    }

    @Override
    @Transactional
    public Asistencia marcarSalida(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        Asistencia asistencia = asistenciaDao.buscarPorUsuarioYFecha(usuario.getIdUsuario(), hoy)
                .orElseGet(() -> new Asistencia(null, usuario, hoy, LocalTime.now(), null));
        asistencia.setHoraSalida(LocalTime.now());
        log.info("Salida marcada: usuario={}, hora={}", usuario.getUsername(), asistencia.getHoraSalida());
        return asistencia.getIdAsistencia() == null ? asistenciaDao.guardar(asistencia) : asistenciaDao.actualizar(asistencia);
    }

    @Override
    public Optional<Asistencia> obtenerDeHoy(Usuario usuario) {
        return asistenciaDao.buscarPorUsuarioYFecha(usuario.getIdUsuario(), LocalDate.now());
    }

    @Override
    public List<Asistencia> listarTodas() {
        return asistenciaDao.listarTodasOrdenadas();
    }
}
