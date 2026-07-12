package com.elpaisa.service;

import com.elpaisa.dao.ReservaDao;
import com.elpaisa.model.Reserva;
import com.elpaisa.model.Sede;
import com.elpaisa.service.impl.ReservaServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaServiceImpl - validaciones de telefono y horario")
class ReservaServiceImplTest {

    @Mock
    private ReservaDao reservaDao;

    private ReservaServiceImpl reservaService;

    private Reserva reservaBase(Sede sede, String telefono, LocalDateTime fechaHora) {
        Reserva r = new Reserva();
        r.setNombreCliente("Carlos");
        r.setTelefonoCliente(telefono);
        r.setSede(sede);
        r.setFechaHora(fechaHora);
        r.setNumeroPersonas(2);
        return r;
    }

    @Test
    @DisplayName("Debe rechazar un telefono que no empieza en 9")
    void crear_telefonoNoEmpiezaEn9_lanzaExcepcion() {
        reservaService = new ReservaServiceImpl(reservaDao);
        Sede sede = new Sede(1, "San Andres", "direccion");
        Reserva r = reservaBase(sede, "812345678", LocalDateTime.now().plusDays(2).withHour(12).withMinute(0));

        assertThrows(IllegalArgumentException.class, () -> reservaService.crear(r));
    }

    @Test
    @DisplayName("Debe rechazar un telefono con menos de 9 digitos")
    void crear_telefonoConPocosDigitos_lanzaExcepcion() {
        reservaService = new ReservaServiceImpl(reservaDao);
        Sede sede = new Sede(1, "San Andres", "direccion");
        Reserva r = reservaBase(sede, "98765", LocalDateTime.now().plusDays(2).withHour(12).withMinute(0));

        assertThrows(IllegalArgumentException.class, () -> reservaService.crear(r));
    }

    @Test
    @DisplayName("Debe rechazar una reserva para el mismo dia (se requiere minimo 1 dia de anticipacion)")
    void crear_reservaParaHoy_lanzaExcepcion() {
        reservaService = new ReservaServiceImpl(reservaDao);
        Sede sede = new Sede(1, "San Andres", "direccion");
        Reserva r = reservaBase(sede, "987654321", LocalDateTime.now().withHour(12).withMinute(0));

        assertThrows(IllegalArgumentException.class, () -> reservaService.crear(r));
    }

    @Test
    @DisplayName("Debe rechazar una hora fuera del horario de atencion (antes de apertura)")
    void crear_horaAntesDeApertura_lanzaExcepcion() {
        reservaService = new ReservaServiceImpl(reservaDao);
        Sede sede = new Sede(1, "San Andres", "direccion");
        Reserva r = reservaBase(sede, "987654321", LocalDateTime.now().plusDays(2).withHour(6).withMinute(0));

        assertThrows(IllegalArgumentException.class, () -> reservaService.crear(r));
    }

    @Test
    @DisplayName("PRUEBA DE NEGOCIO: en Huanchaco no se permite reservar a las 8:00am (abre a las 9:00am)")
    void crear_huanchacoAntesDeLas9_lanzaExcepcion() {
        reservaService = new ReservaServiceImpl(reservaDao);
        Sede huanchaco = new Sede(1, "Huanchaco", "direccion");
        Reserva r = reservaBase(huanchaco, "987654321", LocalDateTime.now().plusDays(2).withHour(8).withMinute(30));

        assertThrows(IllegalArgumentException.class, () -> reservaService.crear(r));
    }

    @Test
    @DisplayName("Debe crear la reserva correctamente cuando todos los datos son validos")
    void crear_conDatosValidos_creaLaReservaComoPendiente() {
        reservaService = new ReservaServiceImpl(reservaDao);
        Sede sede = new Sede(1, "San Andres", "direccion");
        Reserva r = reservaBase(sede, "987654321", LocalDateTime.now().plusDays(2).withHour(12).withMinute(0));
        when(reservaDao.guardar(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva guardada = reservaService.crear(r);

        org.junit.jupiter.api.Assertions.assertEquals("PENDIENTE", guardada.getEstado());
    }
}
