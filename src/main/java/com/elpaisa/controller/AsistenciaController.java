package com.elpaisa.controller;

import com.elpaisa.dao.UsuarioDao;
import com.elpaisa.model.Usuario;
import com.elpaisa.service.AsistenciaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/asistencia")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final UsuarioDao usuarioDao;

    public AsistenciaController(AsistenciaService asistenciaService, UsuarioDao usuarioDao) {
        this.asistenciaService = asistenciaService;
        this.usuarioDao = usuarioDao;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        Usuario usuario = usuarioDao.buscarPorUsername(authentication.getName()).orElseThrow();
        model.addAttribute("registroHoy", asistenciaService.obtenerDeHoy(usuario).orElse(null));
        model.addAttribute("esAdmin", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        if (model.getAttribute("esAdmin").equals(true)) {
            model.addAttribute("todas", asistenciaService.listarTodas());
        }
        return "asistencia/index";
    }

    @PostMapping("/entrada")
    public String marcarEntrada(Authentication authentication) {
        Usuario usuario = usuarioDao.buscarPorUsername(authentication.getName()).orElseThrow();
        asistenciaService.marcarEntrada(usuario);
        return "redirect:/asistencia";
    }

    @PostMapping("/salida")
    public String marcarSalida(Authentication authentication) {
        Usuario usuario = usuarioDao.buscarPorUsername(authentication.getName()).orElseThrow();
        asistenciaService.marcarSalida(usuario);
        return "redirect:/asistencia";
    }
}
