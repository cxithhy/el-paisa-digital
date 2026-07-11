package com.elpaisa.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRUEBAS DE SEGURIDAD (integracion HTTP real, no solo unitarias):
 * verifican que el control de acceso por rol (RBAC) definido en SecurityConfig
 * realmente bloquea/permite las rutas correctas, y que las paginas protegidas
 * no son accesibles sin autenticarse.
 *
 * Se usa el perfil "h2" para que la prueba corra de forma aislada y repetible,
 * sin depender de que MySQL este corriendo en la maquina.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@DisplayName("Pruebas de seguridad - control de acceso por rol (RBAC)")
class SecurityWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("La pagina de login debe ser publica, accesible sin autenticacion")
    void login_esPublico() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un usuario NO autenticado debe ser redirigido al login al pedir el dashboard")
    void dashboard_sinAutenticar_redirigeALogin() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("PRUEBA DE SEGURIDAD: un CAJERO no debe poder acceder a Productos (solo ADMIN)")
    @WithMockUser(roles = "CAJERO")
    void productos_comoCajero_devuelveAccesoDenegado() throws Exception {
        mockMvc.perform(get("/productos")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PRUEBA DE SEGURIDAD: un MOZO no debe poder acceder a Insumos (solo ADMIN)")
    @WithMockUser(roles = "MOZO")
    void insumos_comoMozo_devuelveAccesoDenegado() throws Exception {
        mockMvc.perform(get("/insumos")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PRUEBA DE SEGURIDAD: un MOZO no debe poder descargar el Reporte Excel (solo ADMIN/CAJERO)")
    @WithMockUser(roles = "MOZO")
    void reporteExcel_comoMozo_devuelveAccesoDenegado() throws Exception {
        mockMvc.perform(get("/reportes/ventas/excel")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un ADMIN si debe poder acceder a Productos")
    @WithMockUser(roles = "ADMIN")
    void productos_comoAdmin_accedeCorrectamente() throws Exception {
        mockMvc.perform(get("/productos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un MOZO si debe poder acceder al formulario de Nueva Venta (rol operativo minimo)")
    @WithMockUser(roles = "MOZO")
    void nuevaVenta_comoMozo_accedeCorrectamente() throws Exception {
        mockMvc.perform(get("/ventas/nueva")).andExpect(status().isOk());
    }
}
