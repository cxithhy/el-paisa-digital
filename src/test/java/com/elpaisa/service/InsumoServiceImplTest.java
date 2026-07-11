package com.elpaisa.service;

import com.elpaisa.dao.InsumoDao;
import com.elpaisa.model.Insumo;
import com.elpaisa.service.impl.InsumoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InsumoServiceImpl - validaciones y calculo de stock bajo")
class InsumoServiceImplTest {

    @Mock
    private InsumoDao insumoDao;

    private InsumoServiceImpl insumoService;

    @BeforeEach
    void setUp() {
        insumoService = new InsumoServiceImpl(insumoDao);
    }

    @Test
    @DisplayName("Debe rechazar un insumo con nombre vacio")
    void crear_conNombreVacio_lanzaExcepcion() {
        Insumo insumo = new Insumo(null, "  ", new BigDecimal("10"), new BigDecimal("2"));
        assertThrows(IllegalArgumentException.class, () -> insumoService.crear(insumo));
    }

    @Test
    @DisplayName("Debe identificar correctamente los insumos con stock igual o por debajo del minimo")
    void listarConStockBajo_filtraSoloLosQueEstanEnOAlDebajoDelMinimo() {
        Insumo bajo = new Insumo(1, "Pescado", new BigDecimal("3.00"), new BigDecimal("5.00")); // bajo
        Insumo enElLimite = new Insumo(2, "Limon", new BigDecimal("3.00"), new BigDecimal("3.00")); // igual al minimo, tambien cuenta
        Insumo ok = new Insumo(3, "Sal", new BigDecimal("50.00"), new BigDecimal("5.00")); // normal

        when(insumoDao.listarTodos()).thenReturn(List.of(bajo, enElLimite, ok));

        List<Insumo> resultado = insumoService.listarConStockBajo();

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(bajo));
        assertTrue(resultado.contains(enElLimite));
        assertFalse(resultado.contains(ok));
    }
}
