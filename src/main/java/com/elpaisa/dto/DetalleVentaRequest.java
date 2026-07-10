package com.elpaisa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO: linea de pedido que llega desde el formulario de "Nueva Venta". */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaRequest {
    private Integer idProducto;
    private Integer cantidad;
}
