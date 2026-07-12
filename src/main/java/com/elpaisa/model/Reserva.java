package com.elpaisa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * RF03: Reserva de mesa hecha por un cliente para una sede y fecha/hora especifica.
 */
@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer idReserva;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(name = "telefono_cliente", nullable = false, length = 20)
    private String telefonoCliente;

    @ManyToOne
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "numero_personas", nullable = false)
    private Integer numeroPersonas;

    @Column(nullable = false, length = 20)
    private String estado; // PENDIENTE, CONFIRMADA, CANCELADA

    @Column(length = 255)
    private String observaciones;
}
