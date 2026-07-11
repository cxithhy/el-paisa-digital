package com.elpaisa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Insumo principal que se descuenta del stock al vender este producto (RF02).
     * Es opcional: si un producto no tiene insumo asociado, la venta se registra
     * normal y simplemente no se descuenta stock de nada.
     * Reemplaza a una tabla "receta" separada para simplificar el modelo:
     * cada plato consume, como maximo, un insumo principal.
     */
    @ManyToOne
    @JoinColumn(name = "id_insumo_principal")
    private Insumo insumoPrincipal;

    @Column(name = "cantidad_insumo_por_unidad", precision = 10, scale = 2)
    private BigDecimal cantidadInsumoPorUnidad;
}
