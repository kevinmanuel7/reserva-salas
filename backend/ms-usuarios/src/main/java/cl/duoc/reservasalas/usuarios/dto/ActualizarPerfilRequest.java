package cl.duoc.reservasalas.usuarios.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Cuerpo de PUT /api/v1/usuarios/me (Contrato 1 v10). Los tres campos son opcionales:
 * el que llega null (o no viene) conserva el valor guardado. Si la petición envía
 * nombre, email o rol, Jackson los ignora porque no existen en este record.
 * Las reglas de cada campo se revisan en UsuarioService.
 */
public record ActualizarPerfilRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento,
        String telefono,
        String area) {
}
