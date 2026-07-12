package com.elpaisa.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Utilidad centralizada de validaciones (SOLID - SRP: una unica responsabilidad,
 * validar datos de entrada antes de que lleguen a la capa de negocio).
 *
 * Usa Google Guava (Preconditions) y Apache Commons Lang3 (StringUtils).
 */
public final class ValidacionUtil {

    // Celular peruano: empieza en 9 y tiene exactamente 9 digitos (ej. 987654321)
    private static final Pattern TELEFONO_PERU_MOVIL = Pattern.compile("^9\\d{8}$");

    private ValidacionUtil() {
        // Clase de utilidades: constructor privado
    }

    public static void validarTextoNoVacio(String valor, String nombreCampo) {
        // Apache Commons: forma limpia de chequear blanco/nulo
        if (StringUtils.isBlank(valor)) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede estar vacio.");
        }
    }

    public static void validarMontoPositivo(BigDecimal monto, String nombreCampo) {
        // Google Guava: Preconditions para validar invariantes de negocio
        Preconditions.checkArgument(monto != null && monto.compareTo(BigDecimal.ZERO) > 0,
                "El campo '%s' debe ser un monto mayor a 0.", nombreCampo);
    }

    public static void validarCantidadPositiva(Integer cantidad, String nombreCampo) {
        Preconditions.checkArgument(cantidad != null && cantidad > 0,
                "El campo '%s' debe ser mayor a 0.", nombreCampo);
    }

    /** Valida que el telefono sea un celular peruano real: empieza en 9, 9 digitos exactos. */
    public static void validarTelefonoPeruMovil(String telefono, String nombreCampo) {
        validarTextoNoVacio(telefono, nombreCampo);
        String limpio = telefono.trim();
        if (!TELEFONO_PERU_MOVIL.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "El campo '" + nombreCampo + "' debe ser un celular peruano valido: "
                            + "empezar con 9 y tener exactamente 9 digitos (ej. 987654321).");
        }
    }

    /**
     * Valida que la fecha/hora de una reserva sea al menos un dia despues de hoy,
     * y que caiga dentro del horario de atencion (que puede variar segun la sede).
     */
    public static void validarFechaHoraReserva(LocalDateTime fechaHora, LocalTime apertura, LocalTime cierre) {
        if (fechaHora == null) {
            throw new IllegalArgumentException("Debe indicar la fecha y hora de la reserva.");
        }

        LocalDate fechaMinima = LocalDate.now().plusDays(1);
        if (fechaHora.toLocalDate().isBefore(fechaMinima)) {
            throw new IllegalArgumentException(
                    "La reserva debe hacerse con al menos un dia de anticipacion (a partir del "
                            + fechaMinima + ").");
        }

        LocalTime hora = fechaHora.toLocalTime();
        if (hora.isBefore(apertura) || hora.isAfter(cierre)) {
            throw new IllegalArgumentException(
                    "El horario de atencion es de " + apertura + " a " + cierre + ". "
                            + "Elige una hora dentro de ese rango.");
        }
    }
}
