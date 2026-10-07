package cl.duoc.reservasalas.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo que la app envía a POST /auth/login.
 * Las anotaciones se revisan antes de llegar al servicio; si fallan, la respuesta
 * es 400 DATOS_INVALIDOS (ver exception/ManejadorGlobalErrores).
 */
public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
