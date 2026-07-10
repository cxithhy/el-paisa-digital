package com.elpaisa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalle = new ArrayList<>();

    /**
     * Cantidad total de platos vendidos (suma las cantidades de cada linea),
     * a diferencia de detalle.size() que solo cuenta cuantos productos distintos hay.
     * No es un campo persistido, se calcula al vuelo.
     */
    public int getTotalItems() {
        return detalle.stream().mapToInt(DetalleVenta::getCantidad).sum();
    }
}
