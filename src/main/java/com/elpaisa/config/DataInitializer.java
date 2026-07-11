package com.elpaisa.config;

import com.elpaisa.model.*;
import com.elpaisa.service.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga datos iniciales de demostracion (sedes, roles, usuarios, insumos,
 * productos y receta) para poder exponer el sistema sin configurar datos a mano.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    private final UsuarioService usuarioService;

    public DataInitializer(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Idempotencia: en H2 (memoria) la BD se borra en cada reinicio, pero en MySQL
        // los datos persisten. Sin esta validacion, reiniciar la app con MySQL
        // fallaria al intentar insertar de nuevo los mismos roles/usuarios (clave duplicada).
        Long rolesExistentes = em.createQuery("SELECT COUNT(r) FROM Rol r", Long.class).getSingleResult();
        if (rolesExistentes > 0) {
            return; // Ya se cargaron los datos de prueba anteriormente, no hacer nada.
        }

        Sede huanchaco = new Sede(null, "Huanchaco", "Av. Larco 123, Huanchaco");
        Sede sanAndres = new Sede(null, "San Andrés", "Av. América Oeste 456, San Andrés");
        Sede santaInes = new Sede(null, "Santa Inés", "Av. Vista Alegre 789, Santa Inés");
        Sede santaTeresa = new Sede(null, "Santa Teresa", "Av. Prolongación César Vallejo 321, Santa Teresa");
        em.persist(huanchaco);
        em.persist(sanAndres);
        em.persist(santaInes);
        em.persist(santaTeresa);

        Rol admin = new Rol(null, "ADMIN");
        Rol cajero = new Rol(null, "CAJERO");
        Rol mozo = new Rol(null, "MOZO");
        em.persist(admin);
        em.persist(cajero);
        em.persist(mozo);

        Usuario userAdmin = new Usuario(null, "admin", null, admin, huanchaco, true);
        usuarioService.crear(userAdmin, "admin123");

        Usuario userCajero = new Usuario(null, "cajero1", null, cajero, huanchaco, true);
        usuarioService.crear(userCajero, "cajero123");

        Insumo pescado = new Insumo(null, "Pescado (kg)", new BigDecimal("50.00"), new BigDecimal("5.00"));
        Insumo limon = new Insumo(null, "Limon (kg)", new BigDecimal("20.00"), new BigDecimal("3.00"));
        em.persist(pescado);
        em.persist(limon);

        Producto ceviche = new Producto(null, "Ceviche El Paisa", new BigDecimal("35.00"), null);
        em.persist(ceviche);

        Receta recetaPescado = new Receta(null, ceviche, pescado, new BigDecimal("0.30"));
        Receta recetaLimon = new Receta(null, ceviche, limon, new BigDecimal("0.15"));
        em.persist(recetaPescado);
        em.persist(recetaLimon);
    }
}
