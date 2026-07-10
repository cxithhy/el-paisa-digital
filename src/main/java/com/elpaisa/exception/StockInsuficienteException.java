package com.elpaisa.exception;

/**
 * Se lanza cuando una venta requiere mas cantidad de un insumo
 * de la que hay disponible en stock (evita mermas y ventas invalidas).
 */
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
