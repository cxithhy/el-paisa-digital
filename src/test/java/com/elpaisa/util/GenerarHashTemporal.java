package com.elpaisa.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad TEMPORAL: solo para generar manualmente el hash BCrypt de una
 * contraseña y poder insertarla directo en la base de datos por SQL.
 * Se puede borrar despues de usarla, no es parte del proyecto final.
 */
class GenerarHashTemporal {

    @Test
    void imprimirHashDeMozo123() {
        String hash = new BCryptPasswordEncoder().encode("mozo123");
        System.out.println("=====================================");
        System.out.println("HASH GENERADO: " + hash);
        System.out.println("=====================================");
    }
}
