package com.elpaisa.security;

import com.elpaisa.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * Adaptador entre nuestra entidad Usuario y el UserDetails que exige Spring Security.
 * Permite exponer datos adicionales (sede, nombre del rol) en la sesion autenticada,
 * para poder mostrarlos en la interfaz (navbar) sin tener que volver a consultar
 * la base de datos en cada pantalla.
 */
public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreRol() {
        return usuario.getRol().getNombre();
    }

    public String getNombreSede() {
        return usuario.getSede().getNombre();
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }
}
