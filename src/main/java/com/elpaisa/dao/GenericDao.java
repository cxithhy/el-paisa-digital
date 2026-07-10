package com.elpaisa.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato generico del patron DAO.
 * SOLID - ISP: interfaz pequeña y enfocada solo en operaciones de persistencia.
 * SOLID - DIP: los Services dependen de esta abstraccion, no de una implementacion concreta.
 *
 * @param <T>  tipo de entidad
 * @param <ID> tipo de la clave primaria
 */
public interface GenericDao<T, ID> {
    T guardar(T entidad);
    Optional<T> buscarPorId(ID id);
    List<T> listarTodos();
    void eliminar(ID id);
}
