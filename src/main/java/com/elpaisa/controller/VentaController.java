package com.elpaisa.controller;

import com.elpaisa.dao.ProductoDao;
import com.elpaisa.dao.SedeDao;
import com.elpaisa.dto.VentaRequest;
import com.elpaisa.service.VentaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final ProductoDao productoDao;
    private final SedeDao sedeDao;

    public VentaController(VentaService ventaService, ProductoDao productoDao, SedeDao sedeDao) {
        this.ventaService = ventaService;
        this.productoDao = productoDao;
        this.sedeDao = sedeDao;
    }

    @GetMapping("/nueva")
    public String formularioNuevaVenta(Model model) {
        model.addAttribute("productos", productoDao.listarTodos());
        model.addAttribute("sedes", sedeDao.listarTodos());
        return "ventas/nueva";
    }

    @PostMapping("/registrar")
    public String registrar(@ModelAttribute VentaRequest ventaRequest) {
        ventaService.registrarVenta(ventaRequest);
        return "redirect:/ventas";
    }

    @GetMapping
    public String listar(Model model) {
        LocalDateTime desde = LocalDateTime.now().minusMonths(1);
        LocalDateTime hasta = LocalDateTime.now();
        model.addAttribute("ventas", ventaService.listarPorRangoFechas(desde, hasta));
        return "ventas/list";
    }
}
