package com.elpaisa.dao.impl;

import com.elpaisa.dao.GenericDao;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Implementacion base del patron DAO usando JPA EntityManager directamente
 * (en vez de delegar en Spring Data JPA), para dejar explicita la capa de acceso a datos.
 *
 * SOLID - OCP: las clases hijas (ProductoDaoImpl, InsumoDaoImpl, etc.) extienden
 * este comportamiento sin necesidad de modificarlo.
 * SOLID - LSP: cualquier DAO concreto puede sustituir a GenericDao sin romper el contrato.
 *
 * @param <T>  tipo de entidad
 * @param <ID> tipo de la clave primaria
 */
public abstract class GenericDaoImpl<T, ID> implements GenericDao<T, ID> {

    protected final EntityManager entityManager;
    private final Class<T> entidadClase;

    protected GenericDaoImpl(EntityManager entityManager, Class<T> entidadClase) {
        this.entityManager = entityManager;
        this.entidadClase = entidadClase;
    }

    @Override
    public T guardar(T entidad) {
        // Si no tiene ID persistido, se hace persist; si ya existe, merge (actualizar).
        entityManager.persist(entidad);
        return entidad;
    }

    @Override
    public Optional<T> buscarPorId(ID id) {
        return Optional.ofNullable(entityManager.find(entidadClase, id));
    }

    @Override
    public List<T> listarTodos() {
        String jpql = "SELECT e FROM " + entidadClase.getSimpleName() + " e";
        return entityManager.createQuery(jpql, entidadClase).getResultList();
    }

    @Override
    public void eliminar(ID id) {
        buscarPorId(id).ifPresent(entityManager::remove);
    }
}
