package com.elpaisa.dao;

import com.elpaisa.model.Usuario;

import java.util.Optional;

public interface UsuarioDao extends GenericDao<Usuario, Integer> {
    Optional<Usuario> buscarPorUsername(String username);
}
