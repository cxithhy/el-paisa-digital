package com.elpaisa.service;

import com.elpaisa.dao.ProductoDao;
import com.elpaisa.exception.RecursoNoEncontradoException;
import com.elpaisa.model.Producto;
import com.elpaisa.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoServiceImpl - validaciones de negocio")
class ProductoServiceImplTest {

    @Mock
    private ProductoDao productoDao;

    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoServiceImpl(productoDao);
    }

    @Test
    @DisplayName("Debe rechazar un producto con precio negativo o cero")
    void crear_conPrecioInvalido_lanzaExcepcion() {
        Producto producto = new Producto(null, "Ceviche Mixto", BigDecimal.ZERO, null, null);

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
    }

    @Test
    @DisplayName("Debe rechazar un producto con nombre vacio")
    void crear_conNombreVacio_lanzaExcepcion() {
        Producto producto = new Producto(null, "   ", new BigDecimal("20.00"), null, null);

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException si el producto no existe")
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(productoDao.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtenerPorId(99));
    }
}
