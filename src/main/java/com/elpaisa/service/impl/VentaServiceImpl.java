package com.elpaisa.service.impl;

import com.elpaisa.dao.*;
import com.elpaisa.dto.DetalleVentaRequest;
import com.elpaisa.dto.VentaRequest;
import com.elpaisa.exception.RecursoNoEncontradoException;
import com.elpaisa.exception.StockInsuficienteException;
import com.elpaisa.model.*;
import com.elpaisa.service.VentaService;
import com.elpaisa.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementa RF01 (registrar pedido) + RF02 (descuento automatico de inventario
 * segun receta del plato vendido).
 *
 * SOLID - SRP: esta clase solo se ocupa de la logica de negocio de "vender";
 * no sabe como se persiste nada en detalle (eso es responsabilidad de los DAO).
 * SOLID - DIP: depende de las abstracciones VentaDao/ProductoDao/RecetaDao/InsumoDao,
 * no de sus implementaciones concretas -> permite testear con mocks (TDD).
 */
@Service
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final VentaDao ventaDao;
    private final ProductoDao productoDao;
    private final RecetaDao recetaDao;
    private final InsumoDao insumoDao;
    private final SedeDao sedeDao;

    public VentaServiceImpl(VentaDao ventaDao, ProductoDao productoDao, RecetaDao recetaDao,
                             InsumoDao insumoDao, SedeDao sedeDao) {
        this.ventaDao = ventaDao;
        this.productoDao = productoDao;
        this.recetaDao = recetaDao;
        this.insumoDao = insumoDao;
        this.sedeDao = sedeDao;
    }

    @Override
    @Transactional
    public Venta registrarVenta(VentaRequest request) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto.");
        }

        Sede sede = sedeDao.buscarPorId(request.getIdSede())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada: id=" + request.getIdSede()));

        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setSede(sede);
        venta.setTotal(BigDecimal.ZERO);

        BigDecimal totalVenta = BigDecimal.ZERO;

        for (DetalleVentaRequest item : request.getDetalles()) {
            ValidacionUtil.validarCantidadPositiva(item.getCantidad(), "cantidad");

            Producto producto = productoDao.buscarPorId(item.getIdProducto())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado: id=" + item.getIdProducto()));

            // 1) Verificar y descontar stock de insumos segun receta (RF02)
            descontarInsumosSegunReceta(producto, item.getCantidad());

            // 2) Armar el detalle de venta (RF01)
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            venta.getDetalle().add(detalle);

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            totalVenta = totalVenta.add(subtotal);
        }

        venta.setTotal(totalVenta);
        Venta guardada = ventaDao.guardar(venta);
        log.info("Venta registrada: id={}, total={}, sede={}", guardada.getIdVenta(), totalVenta, sede.getNombre());
        return guardada;
    }

    /**
     * Por cada insumo de la receta del producto, valida que haya stock suficiente
     * y lo descuenta. Si algun insumo no alcanza, se aborta toda la venta
     * (la anotacion @Transactional revierte los cambios ya aplicados).
     */
    private void descontarInsumosSegunReceta(Producto producto, int cantidadVendida) {
        List<Receta> receta = recetaDao.buscarPorProducto(producto.getIdProducto());

        for (Receta r : receta) {
            Insumo insumo = r.getInsumo();
            BigDecimal cantidadRequerida = r.getCantidad().multiply(BigDecimal.valueOf(cantidadVendida));

            if (insumo.getStockActual().compareTo(cantidadRequerida) < 0) {
                throw new StockInsuficienteException(
                        "Stock insuficiente de '" + insumo.getNombre() + "' para preparar "
                                + cantidadVendida + " x " + producto.getNombre()
                                + " (disponible: " + insumo.getStockActual() + ")");
            }

            insumo.setStockActual(insumo.getStockActual().subtract(cantidadRequerida));
            insumoDao.actualizar(insumo);
        }
    }

    @Override
    public List<Venta> listarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        return ventaDao.buscarEntreFechas(desde, hasta);
    }
}
