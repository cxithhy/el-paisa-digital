package com.elpaisa.service;

import com.elpaisa.dao.*;
import com.elpaisa.dto.DetalleVentaRequest;
import com.elpaisa.dto.VentaRequest;
import com.elpaisa.exception.StockInsuficienteException;
import com.elpaisa.model.*;
import com.elpaisa.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD - Pruebas unitarias de la regla de negocio mas critica del sistema:
 * el registro de una venta debe descontar el inventario del insumo principal
 * asociado al producto (RF02) y rechazar la venta si no hay stock suficiente.
 *
 * Se usan mocks de los DAO (SOLID - DIP permite esto sin tocar base de datos real).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VentaServiceImpl - registro de ventas y descuento de stock")
class VentaServiceImplTest {

    @Mock private VentaDao ventaDao;
    @Mock private ProductoDao productoDao;
    @Mock private InsumoDao insumoDao;
    @Mock private SedeDao sedeDao;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Sede sede;
    private Producto ceviche;
    private Producto limonada; // producto SIN insumo asociado, para probar el caso opcional
    private Insumo pescado;

    @BeforeEach
    void setUp() {
        sede = new Sede(1, "Huanchaco", "Av. Larco 123");
        pescado = new Insumo(1, "Pescado (kg)", new BigDecimal("10.00"), new BigDecimal("5.00"));

        // Ceviche SI tiene insumo principal asociado (0.30 kg de pescado por unidad)
        ceviche = new Producto(1, "Ceviche El Paisa", new BigDecimal("35.00"), pescado, new BigDecimal("0.30"));

        // Limonada NO tiene insumo asociado (relacion opcional)
        limonada = new Producto(2, "Limonada", new BigDecimal("8.00"), null, null);
    }

    @Test
    @DisplayName("Debe registrar la venta y descontar el stock correctamente cuando el producto tiene insumo asociado")
    void registrarVenta_conInsumoAsociadoYStockSuficiente_descuentaInventario() {
        VentaRequest request = new VentaRequest();
        request.setIdSede(1);
        request.setDetalles(List.of(new DetalleVentaRequest(1, 2))); // 2 ceviches

        when(sedeDao.buscarPorId(1)).thenReturn(Optional.of(sede));
        when(productoDao.buscarPorId(1)).thenReturn(Optional.of(ceviche));
        when(ventaDao.guardar(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.registrarVenta(request);

        assertNotNull(resultado);
        assertEquals(0, new BigDecimal("70.00").compareTo(resultado.getTotal())); // 2 x 35.00
        // 2 ceviches x 0.30 kg = 0.60 kg descontados de 10.00 -> queda 9.40
        assertEquals(0, new BigDecimal("9.40").compareTo(pescado.getStockActual()));
        verify(insumoDao, times(1)).actualizar(pescado);
        verify(ventaDao, times(1)).guardar(any(Venta.class));
    }

    @Test
    @DisplayName("Debe registrar la venta normalmente cuando el producto NO tiene insumo asociado")
    void registrarVenta_sinInsumoAsociado_noDescuentaNadaYRegistraIgual() {
        VentaRequest request = new VentaRequest();
        request.setIdSede(1);
        request.setDetalles(List.of(new DetalleVentaRequest(2, 3))); // 3 limonadas

        when(sedeDao.buscarPorId(1)).thenReturn(Optional.of(sede));
        when(productoDao.buscarPorId(2)).thenReturn(Optional.of(limonada));
        when(ventaDao.guardar(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.registrarVenta(request);

        assertNotNull(resultado);
        assertEquals(0, new BigDecimal("24.00").compareTo(resultado.getTotal())); // 3 x 8.00
        verifyNoInteractions(insumoDao); // no debe tocar ningun insumo
        verify(ventaDao, times(1)).guardar(any(Venta.class));
    }

    @Test
    @DisplayName("Debe rechazar la venta cuando el insumo asociado no alcanza para la cantidad pedida")
    void registrarVenta_conStockInsuficiente_lanzaExcepcionYNoGuardaNada() {
        // Se piden 40 ceviches (requieren 12.0 kg de pescado, solo hay 10.0 kg)
        VentaRequest request = new VentaRequest();
        request.setIdSede(1);
        request.setDetalles(List.of(new DetalleVentaRequest(1, 40)));

        when(sedeDao.buscarPorId(1)).thenReturn(Optional.of(sede));
        when(productoDao.buscarPorId(1)).thenReturn(Optional.of(ceviche));

        assertThrows(StockInsuficienteException.class, () -> ventaService.registrarVenta(request));

        // El stock no debe alterarse y la venta no debe persistirse
        assertEquals(0, new BigDecimal("10.00").compareTo(pescado.getStockActual()));
        verify(ventaDao, never()).guardar(any(Venta.class));
        verify(insumoDao, never()).actualizar(any(Insumo.class));
    }

    @Test
    @DisplayName("Debe rechazar una venta sin ningun producto en el detalle")
    void registrarVenta_sinDetalles_lanzaExcepcion() {
        VentaRequest request = new VentaRequest();
        request.setIdSede(1);
        request.setDetalles(List.of());

        assertThrows(IllegalArgumentException.class, () -> ventaService.registrarVenta(request));
        verifyNoInteractions(ventaDao);
    }
}
