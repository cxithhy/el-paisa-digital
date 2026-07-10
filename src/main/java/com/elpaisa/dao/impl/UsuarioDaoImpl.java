package com.elpaisa.dao.impl;

import com.elpaisa.dao.UsuarioDao;
import com.elpaisa.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioDaoImpl extends GenericDaoImpl<Usuario, Integer> implements UsuarioDao {

    public UsuarioDaoImpl(EntityManager entityManager) {
        super(entityManager, Usuario.class);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        try {
            Usuario usuario = entityManager.createQuery(
                            "SELECT u FROM Usuario u WHERE u.username = :username", Usuario.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
