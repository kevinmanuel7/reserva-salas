package cl.duoc.reservasalas.salas.dto;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import cl.duoc.reservasalas.salas.model.TipoSala;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActualizarSalaRequest(
        @NotBlank(message = "El código de la sala no puede estar vacío")
        String codigo,

        @NotBlank(message = "El nombre de la sala no puede estar vacío")
        String nombre,

        @NotNull(message = "El tipo de sala es obligatorio")
        TipoSala tipo,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser mayor que 0")
        Integer capacidad,

        @NotNull(message = "Debe indicar si tiene proyector")
        Boolean tieneProyector,

        @NotNull(message = "Debe indicar si tiene computadores")
        Boolean tieneComputadores,

        @NotNull(message = "Debe indicar si tiene climatización")
        Boolean tieneClimatizacion,

        @NotNull(message = "Debe indicar si está activa")
        Boolean activa,

        // Campos opcionales de la versión 5 del Contrato 1. Si llegan null, el PUT
        // conserva los valores guardados (la web no los envía).
        String ubicacion,

        @JsonFormat(pattern = "HH:mm")
        LocalTime horaApertura,

        @JsonFormat(pattern = "HH:mm")
        LocalTime horaCierre,

        List<@Valid EquipoDto> equipos
) {
}
