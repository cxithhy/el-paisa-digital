package com.elpaisa.controller;

import com.elpaisa.dao.InsumoDao;
import com.elpaisa.model.Producto;
import com.elpaisa.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final InsumoDao insumoDao;

    public ProductoController(ProductoService productoService, InsumoDao insumoDao) {
        this.productoService = productoService;
        this.insumoDao = insumoDao;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "productos/list";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("insumos", insumoDao.listarTodos());
        return "productos/form";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Integer id, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(id));
        model.addAttribute("insumos", insumoDao.listarTodos());
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto,
                           @RequestParam(required = false) Integer idInsumoPrincipal) {
        // El insumo se resuelve aparte (no via binding directo) para evitar
        // depender de un conversor String->Insumo en Spring MVC.
        if (idInsumoPrincipal != null) {
            producto.setInsumoPrincipal(insumoDao.buscarPorId(idInsumoPrincipal).orElse(null));
        } else {
            producto.setInsumoPrincipal(null);
            producto.setCantidadInsumoPorUnidad(null);
        }

        // Si el formulario trae un id, es una edicion; si no, es un producto nuevo.
        if (producto.getIdProducto() == null) {
            productoService.crear(producto);
        } else {
            productoService.actualizar(producto);
        }
        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return "redirect:/productos";
    }
}
