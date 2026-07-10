package com.elpaisa.controller;

import com.elpaisa.dao.ProductoDao;
import com.elpaisa.dao.SedeDao;
import com.elpaisa.dto.VentaRequest;
import com.elpaisa.model.DetalleVenta;
import com.elpaisa.model.Venta;
import com.elpaisa.service.VentaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Venta> ventas = ventaService.listarPorRangoFechas(desde, hasta);
        model.addAttribute("ventas", ventas);
        model.addAttribute("resumen", calcularResumenDelMes(ventas));
        return "ventas/list";
    }

    /**
     * Calcula indicadores del mes a partir de las ventas ya cargadas:
     * cantidad de ventas, ingresos totales y el producto mas vendido.
     * No requiere una consulta adicional a la base de datos.
     */
    private Map<String, Object> calcularResumenDelMes(List<Venta> ventas) {
        int totalVentas = ventas.size();

        BigDecimal ingresosTotales = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> unidadesPorProducto = ventas.stream()
                .flatMap(v -> v.getDetalle().stream())
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getNombre(),
                        Collectors.summingInt(DetalleVenta::getCantidad)));

        Map.Entry<String, Integer> masVendido = unidadesPorProducto.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(Map.entry("Sin ventas aún", 0));

        return Map.of(
                "totalVentas", totalVentas,
                "ingresosTotales", ingresosTotales,
                "productoMasVendidoNombre", masVendido.getKey(),
                "productoMasVendidoCantidad", masVendido.getValue()
        );
    }
}
