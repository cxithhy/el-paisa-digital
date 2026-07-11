package com.elpaisa.service;

import com.elpaisa.dao.UsuarioDao;
import com.elpaisa.model.Rol;
import com.elpaisa.model.Sede;
import com.elpaisa.model.Usuario;
import com.elpaisa.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas de seguridad (RNF01): verifican que las contraseñas nunca se
 * almacenen en texto plano y que la autenticacion/autorizacion por rol
 * funcione correctamente a nivel de UserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl - seguridad de autenticacion")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioDao usuarioDao;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UsuarioServiceImpl usuarioService;

    private Sede sede;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioDao, passwordEncoder);
        sede = new Sede(1, "Huanchaco", "Av. Larco 123");
        rolAdmin = new Rol(1, "ADMIN");
    }

    @Test
    @DisplayName("PRUEBA DE SEGURIDAD: la contraseña nunca se guarda en texto plano, siempre cifrada con BCrypt")
    void crear_cifraLaContrasenaConBCrypt_nuncaTextoPlano() {
        Usuario usuario = new Usuario(null, "admin", null, rolAdmin, sede, true);
        when(usuarioDao.guardar(usuario)).thenReturn(usuario);

        usuarioService.crear(usuario, "admin123");

        // La contraseña guardada NUNCA debe ser igual al texto plano original
        assertNotEquals("admin123", usuario.getPassword());
        // Debe tener el formato de un hash BCrypt (empieza con $2a$, $2b$ o similar)
        assertTrue(usuario.getPassword().startsWith("$2"));
        // Y debe poder verificarse correctamente contra el texto plano original
        assertTrue(passwordEncoder.matches("admin123", usuario.getPassword()));
    }

    @Test
    @DisplayName("PRUEBA DE SEGURIDAD: no debe permitir crear un usuario con contraseña vacia")
    void crear_conPasswordVacio_lanzaExcepcion() {
        Usuario usuario = new Usuario(null, "admin", null, rolAdmin, sede, true);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.crear(usuario, "   "));
    }

    @Test
    @DisplayName("PRUEBA DE SEGURIDAD: un usuario inactivo no debe poder autenticarse aunque exista")
    void loadUserByUsername_usuarioInactivo_lanzaExcepcion() {
        Usuario inactivo = new Usuario(2, "exempleado", "hash", rolAdmin, sede, false);
        when(usuarioDao.buscarPorUsername("exempleado")).thenReturn(Optional.of(inactivo));

        assertThrows(UsernameNotFoundException.class, () -> usuarioService.loadUserByUsername("exempleado"));
    }

    @Test
    @DisplayName("PRUEBA DE SEGURIDAD: un usuario que no existe no debe revelar informacion, solo lanzar excepcion generica")
    void loadUserByUsername_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioDao.buscarPorUsername("noexiste")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> usuarioService.loadUserByUsername("noexiste"));
    }

    @Test
    @DisplayName("El rol del usuario se traduce correctamente a un authority de Spring Security con prefijo ROLE_")
    void loadUserByUsername_asignaElAuthorityCorrectoSegunElRol() {
        Usuario admin = new Usuario(1, "admin", "$2a$hashSimulado", rolAdmin, sede, true);
        when(usuarioDao.buscarPorUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = usuarioService.loadUserByUsername("admin");

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
