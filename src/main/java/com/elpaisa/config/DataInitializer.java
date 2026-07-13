package com.elpaisa.config;

import com.elpaisa.model.*;
import com.elpaisa.service.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos iniciales de demostracion (sedes, roles, usuarios, insumos,
 * productos y ventas de ejemplo) para poder exponer el sistema sin configurar
 * datos a mano. Al ser el mismo codigo para todo el equipo, cualquier laptop
 * que arranque el proyecto por primera vez contra una base de datos vacia
 * termina con exactamente los mismos datos de demostracion.
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

        // ===== Sedes =====
        Sede huanchaco = new Sede(null, "Huanchaco", "Av. Larco 123, Huanchaco");
        Sede sanAndres = new Sede(null, "San Andrés", "Av. América Oeste 456, San Andrés");
        Sede santaInes = new Sede(null, "Santa Inés", "Av. Vista Alegre 789, Santa Inés");
        Sede santaTeresa = new Sede(null, "Santa Teresa", "Av. Prolongación César Vallejo 321, Santa Teresa");
        em.persist(huanchaco);
        em.persist(sanAndres);
        em.persist(santaInes);
        em.persist(santaTeresa);

        // ===== Roles y usuarios =====
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

        Usuario userMozo = new Usuario(null, "mozo1", null, mozo, huanchaco, true);
        usuarioService.crear(userMozo, "mozo123");

        // ===== Insumos =====
        Insumo pescado = new Insumo(null, "Pescado (kg)", new BigDecimal("50.00"), new BigDecimal("5.00"));
        Insumo mariscos = new Insumo(null, "Mariscos mixtos (kg)", new BigDecimal("40.00"), new BigDecimal("5.00"));
        Insumo limon = new Insumo(null, "Limon (kg)", new BigDecimal("20.00"), new BigDecimal("3.00"));
        Insumo arroz = new Insumo(null, "Arroz (kg)", new BigDecimal("35.00"), new BigDecimal("5.00"));
        Insumo yuca = new Insumo(null, "Yuca (kg)", new BigDecimal("15.00"), new BigDecimal("3.00"));
        Insumo cebolla = new Insumo(null, "Cebolla (kg)", new BigDecimal("18.00"), new BigDecimal("3.00"));
        em.persist(pescado);
        em.persist(mariscos);
        em.persist(limon);
        em.persist(arroz);
        em.persist(yuca);
        em.persist(cebolla);

        // ===== Productos: platos (con insumo principal, para demostrar RF02) =====
        Producto ceviche = new Producto(null, "Ceviche El Paisa", new BigDecimal("35.00"), pescado, new BigDecimal("0.30"));
        Producto cevicheMixto = new Producto(null, "Ceviche Mixto", new BigDecimal("40.00"), mariscos, new BigDecimal("0.35"));
        Producto arrozMariscos = new Producto(null, "Arroz con Mariscos", new BigDecimal("38.00"), mariscos, new BigDecimal("0.30"));
        Producto chicharronPescado = new Producto(null, "Chicharrón de Pescado", new BigDecimal("32.00"), pescado, new BigDecimal("0.28"));
        Producto jaleaMixta = new Producto(null, "Jalea Mixta", new BigDecimal("45.00"), mariscos, new BigDecimal("0.40"));
        em.persist(ceviche);
        em.persist(cevicheMixto);
        em.persist(arrozMariscos);
        em.persist(chicharronPescado);
        em.persist(jaleaMixta);

        // ===== Productos: bebidas (sin insumo asociado, es una relacion opcional) =====
        Producto chichaMorada = new Producto(null, "Chicha Morada (jarra)", new BigDecimal("15.00"), null, null);
        Producto limonada = new Producto(null, "Limonada (jarra)", new BigDecimal("12.00"), null, null);
        Producto incaKola = new Producto(null, "Inca Kola 500ml", new BigDecimal("6.00"), null, null);
        Producto cerveza = new Producto(null, "Cerveza Cusqueña", new BigDecimal("12.00"), null, null);
        em.persist(chichaMorada);
        em.persist(limonada);
        em.persist(incaKola);
        em.persist(cerveza);

        // ===== Ventas de ejemplo (para que Reportes, historial y resumen no se vean vacios en la demo) =====
        crearVentaDemo(huanchaco, LocalDateTime.now().minusDays(4).withHour(13).withMinute(15),
                new Object[]{ceviche, 2}, new Object[]{chichaMorada, 1});

        crearVentaDemo(sanAndres, LocalDateTime.now().minusDays(3).withHour(19).withMinute(40),
                new Object[]{cevicheMixto, 1}, new Object[]{incaKola, 2});

        crearVentaDemo(huanchaco, LocalDateTime.now().minusDays(2).withHour(14).withMinute(5),
                new Object[]{arrozMariscos, 3}, new Object[]{limonada, 2}, new Object[]{cerveza, 2});

        crearVentaDemo(santaInes, LocalDateTime.now().minusDays(1).withHour(20).withMinute(30),
                new Object[]{chicharronPescado, 2}, new Object[]{incaKola, 1});

        crearVentaDemo(santaTeresa, LocalDateTime.now().minusHours(6),
                new Object[]{jaleaMixta, 1}, new Object[]{cerveza, 3});

        crearVentaDemo(huanchaco, LocalDateTime.now().minusHours(2),
                new Object[]{ceviche, 4}, new Object[]{chichaMorada, 2});
    }

    /**
     * Crea una venta de ejemplo con uno o mas items (producto, cantidad), descontando
     * el stock del insumo asociado si el producto tiene uno (igual que RF02 en produccion),
     * y calculando el total en base al precio de cada producto.
     */
    private void crearVentaDemo(Sede sede, LocalDateTime fechaHora, Object[]... items) {
        Venta venta = new Venta();
        venta.setSede(sede);
        venta.setFechaHora(fechaHora);
        venta.setTotal(BigDecimal.ZERO);

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Object[] item : items) {
            Producto producto = (Producto) item[0];
            int cantidad = (Integer) item[1];

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalles.add(detalle);

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));

            if (producto.getInsumoPrincipal() != null && producto.getCantidadInsumoPorUnidad() != null) {
                Insumo insumo = producto.getInsumoPrincipal();
                BigDecimal consumo = producto.getCantidadInsumoPorUnidad().multiply(BigDecimal.valueOf(cantidad));
                insumo.setStockActual(insumo.getStockActual().subtract(consumo));
            }
        }

        venta.setDetalle(detalles);
        venta.setTotal(total);
        em.persist(venta);
    }
}
