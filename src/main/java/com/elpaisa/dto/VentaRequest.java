package com.elpaisa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** DTO: cabecera + detalle de una venta nueva, tal como la arma el mozo/cajero en pantalla. */
@Getter
@Setter
@NoArgsConstructor
public class VentaRequest {
    private Integer idSede;
    private List<DetalleVentaRequest> detalles = new ArrayList<>();
}
