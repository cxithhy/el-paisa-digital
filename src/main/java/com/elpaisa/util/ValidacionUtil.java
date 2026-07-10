package com.elpaisa.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * Utilidad centralizada de validaciones (SOLID - SRP: una unica responsabilidad,
 * validar datos de entrada antes de que lleguen a la capa de negocio).
 *
 * Usa Google Guava (Preconditions) y Apache Commons Lang3 (StringUtils).
 */
public final class ValidacionUtil {

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
}
