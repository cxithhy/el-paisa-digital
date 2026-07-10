package com.elpaisa.service.impl;

import com.elpaisa.dao.ProductoDao;
import com.elpaisa.exception.RecursoNoEncontradoException;
import com.elpaisa.model.Producto;
import com.elpaisa.service.ProductoService;
import com.elpaisa.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SOLID - SRP: esta clase solo orquesta reglas de negocio de Producto,
 * la persistencia se delega al ProductoDao (SOLID - DIP: se inyecta la abstraccion).
 */
@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoDao productoDao;

    public ProductoServiceImpl(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    @Override
    @Transactional
    public Producto crear(Producto producto) {
        ValidacionUtil.validarTextoNoVacio(producto.getNombre(), "nombre");
        ValidacionUtil.validarMontoPositivo(producto.getPrecio(), "precio");
        Producto guardado = productoDao.guardar(producto);
        log.info("Producto creado: id={}, nombre={}", guardado.getIdProducto(), guardado.getNombre());
        return guardado;
    }

    @Override
    @Transactional
    public Producto actualizar(Producto producto) {
        ValidacionUtil.validarTextoNoVacio(producto.getNombre(), "nombre");
        ValidacionUtil.validarMontoPositivo(producto.getPrecio(), "precio");
        return productoDao.actualizar(producto);
    }

    @Override
    public Producto obtenerPorId(Integer id) {
        return productoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: id=" + id));
    }

    @Override
    public List<Producto> listarTodos() {
        return productoDao.listarTodos();
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando producto id={}", id);
        productoDao.eliminar(id);
    }
}
