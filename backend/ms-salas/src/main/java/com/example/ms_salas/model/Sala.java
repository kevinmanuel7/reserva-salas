package com.example.ms_salas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de la sala no puede estar vacío")
    @Column(unique = true, nullable = false)
    private String codigo;
    @NotBlank(message = "El nombre de la sala no puede estar vacío")
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String tipo; // SALA_CLASES, LABORATORIO, MULTIPROPOSITO
    @Min(value = 1, message = "La capacidad debe ser mayor que 0")
    @Column(nullable = false)
    private Integer capacidad;
    @Column(nullable = false)
    private Boolean tieneProyector;
    @Column(nullable = false)
    private Boolean tieneComputadores;
    @Column(nullable = false)
    private Boolean tieneClimatizacion;
    @Column(nullable = false)
    private Boolean activa = true;

    // Campo adicional para comentarios sobre el estado del equipamiento (requisito del mandante)
    @Column(columnDefinition = "TEXT")
    private String comentariosEquipamiento;
}