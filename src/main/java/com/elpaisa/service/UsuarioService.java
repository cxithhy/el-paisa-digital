package com.elpaisa.service;

import com.elpaisa.model.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsuarioService extends UserDetailsService {
    Usuario crear(Usuario usuario, String passwordPlano);
}
