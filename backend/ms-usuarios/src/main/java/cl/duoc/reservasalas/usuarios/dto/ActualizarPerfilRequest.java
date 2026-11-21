package cl.duoc.reservasalas.usuarios.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de PUT /api/v1/usuarios/me. Solo trae fechaNacimiento: si la petición
 * envía nombre, email o rol, Jackson los ignora porque no existen en este record.
 */
public record ActualizarPerfilRequest(
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento) {
}
