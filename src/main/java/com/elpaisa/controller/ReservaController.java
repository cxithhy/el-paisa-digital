package com.elpaisa.controller;

import com.elpaisa.dao.SedeDao;
import com.elpaisa.model.Reserva;
import com.elpaisa.service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final SedeDao sedeDao;

    public ReservaController(ReservaService reservaService, SedeDao sedeDao) {
        this.reservaService = reservaService;
        this.sedeDao = sedeDao;
    }

    @GetMapping
    public String listar(Model model) {
        // Se muestran TODAS las reservas (no solo las proximas), para que el personal
        // vea tambien el historial y no "desaparezcan" reservas ya pasadas de la vista.
        model.addAttribute("reservas", reservaService.listarTodasOrdenadas());
        return "reservas/list";
    }

    @GetMapping("/nueva")
    public String formularioNueva(Model model) {
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("sedes", sedeDao.listarTodos());
        return "reservas/nueva";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Reserva reserva, @RequestParam Integer idSede, Model model) {
        sedeDao.buscarPorId(idSede).ifPresent(reserva::setSede);
        try {
            reservaService.crear(reserva);
        } catch (IllegalArgumentException e) {
            // Validacion fallida (telefono invalido, fecha/hora fuera de horario, etc.):
            // se regresa al mismo formulario mostrando el error, sin perder los datos ya ingresados.
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reserva", reserva);
            model.addAttribute("sedes", sedeDao.listarTodos());
            return "reservas/nueva";
        }
        return "redirect:/reservas";
    }

    @GetMapping("/confirmar/{id}")
    public String confirmar(@PathVariable Integer id) {
        reservaService.cambiarEstado(id, "CONFIRMADA");
        return "redirect:/reservas";
    }

    @GetMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Integer id) {
        reservaService.cambiarEstado(id, "CANCELADA");
        return "redirect:/reservas";
    }
}
