package com.elpaisa.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Manejo centralizado de errores de negocio para toda la capa de controllers (MVC).
 * Usa Logback (via SLF4J) para dejar trazabilidad de los errores.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StockInsuficienteException.class)
    public String manejarStockInsuficiente(StockInsuficienteException ex, Model model) {
        log.warn("Stock insuficiente: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "ventas/nueva";
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String manejarNoEncontrado(RecursoNoEncontradoException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "error";
    }
}
