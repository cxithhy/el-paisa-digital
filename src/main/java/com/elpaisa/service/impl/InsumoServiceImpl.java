package com.elpaisa.service.impl;

import com.elpaisa.dao.InsumoDao;
import com.elpaisa.model.Insumo;
import com.elpaisa.service.InsumoService;
import com.elpaisa.util.ValidacionUtil;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InsumoServiceImpl implements InsumoService {

    private static final Logger log = LoggerFactory.getLogger(InsumoServiceImpl.class);

    private final InsumoDao insumoDao;

    public InsumoServiceImpl(InsumoDao insumoDao) {
        this.insumoDao = insumoDao;
    }

    @Override
    @Transactional
    public Insumo crear(Insumo insumo) {
        ValidacionUtil.validarTextoNoVacio(insumo.getNombre(), "nombre");
        Insumo guardado = insumoDao.guardar(insumo);
        log.info("Insumo creado: id={}, nombre={}", guardado.getIdInsumo(), guardado.getNombre());
        return guardado;
    }

    @Override
    @Transactional
    public Insumo actualizar(Insumo insumo) {
        return insumoDao.actualizar(insumo);
    }

    @Override
    public List<Insumo> listarTodos() {
        return insumoDao.listarTodos();
    }

    @Override
    public List<Insumo> listarConStockBajo() {
        // Google Guava: coleccion inmutable para exponer un resultado de solo lectura
        List<Insumo> bajos = insumoDao.listarTodos().stream()
                .filter(i -> i.getStockActual().compareTo(i.getStockMin()) <= 0)
                .toList();
        return ImmutableList.copyOf(bajos);
    }
}
