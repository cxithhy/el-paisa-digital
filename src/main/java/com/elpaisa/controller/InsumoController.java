package com.elpaisa.controller;

import com.elpaisa.model.Insumo;
import com.elpaisa.service.InsumoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/insumos")
public class InsumoController {

    private final InsumoService insumoService;

    public InsumoController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("insumos", insumoService.listarTodos());
        model.addAttribute("stockBajo", insumoService.listarConStockBajo());
        return "insumos/list";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("insumo", new Insumo());
        return "insumos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Insumo insumo) {
        insumoService.crear(insumo);
        return "redirect:/insumos";
    }
}
