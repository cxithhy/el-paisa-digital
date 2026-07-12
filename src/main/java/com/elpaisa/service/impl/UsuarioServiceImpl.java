package com.elpaisa.service.impl;

import com.elpaisa.dao.UsuarioDao;
import com.elpaisa.model.Usuario;
import com.elpaisa.security.UsuarioPrincipal;
import com.elpaisa.service.UsuarioService;
import com.elpaisa.util.ValidacionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RNF01 (Seguridad): autentica contra la tabla Usuario, con contraseñas
 * cifradas via BCrypt (nunca en texto plano) y acceso restringido por rol.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioDao usuarioDao;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioDao usuarioDao, PasswordEncoder passwordEncoder) {
        this.usuarioDao = usuarioDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario crear(Usuario usuario, String passwordPlano) {
        ValidacionUtil.validarTextoNoVacio(usuario.getUsername(), "username");
        ValidacionUtil.validarTextoNoVacio(passwordPlano, "password");
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        Usuario guardado = usuarioDao.guardar(usuario);
        log.info("Usuario creado: username={}, rol={}", guardado.getUsername(), guardado.getRol().getNombre());
        return guardado;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioDao.buscarPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // UsuarioPrincipal (en vez del User generico de Spring) para poder mostrar
        // en la interfaz el rol y la sede de quien esta logueado, sin consultas extra.
        return new UsuarioPrincipal(usuario);
    }
}
