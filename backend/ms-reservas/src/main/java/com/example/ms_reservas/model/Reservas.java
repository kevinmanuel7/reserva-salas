package com.example.ms_reservas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El id de la sala es obligatorio")
    private Long salaId;

    @NotNull(message = "El id del usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El nombre del relator es obligatorio")
    private String nombreRelator;

    @NotBlank(message = "La actividad no puede estar vacía")
    private String actividad;

    @NotBlank(message = "La fecha es obligatoria")
    private String fecha; // Formato AAAA-MM-DD

    @NotNull(message = "El bloque de inicio es obligatorio")
    @Min(value = 1, message = "El bloque debe ser entre 1 y 13")
    @Max(value = 13, message = "El bloque debe ser entre 1 y 13")
    private Integer bloqueInicio;

    @NotNull(message = "El bloque de fin es obligatorio")
    @Min(value = 1, message = "El bloque debe ser entre 1 y 13")
    @Max(value = 13, message = "El bloque debe ser entre 1 y 13")
    private Integer bloqueFin;

    @NotBlank(message = "El estado es obligatorio")
    private String estado; // "ACTIVA" o "CANCELADA"
}