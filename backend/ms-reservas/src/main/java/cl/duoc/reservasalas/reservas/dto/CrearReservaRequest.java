package cl.duoc.reservasalas.reservas.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearReservaRequest(
        @NotNull(message = "El id de la sala es obligatorio")
        Long salaId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotNull(message = "El bloque de inicio es obligatorio")
        Integer bloqueInicio,

        @NotNull(message = "El bloque de término es obligatorio")
        Integer bloqueFin,

        @NotBlank(message = "La actividad no puede estar vacía")
        String actividad
) {
}
