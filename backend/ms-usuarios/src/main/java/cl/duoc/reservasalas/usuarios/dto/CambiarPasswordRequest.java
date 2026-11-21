package cl.duoc.reservasalas.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de PUT /api/v1/usuarios/me/password (Contrato 1 v10).
 * El largo mínimo y que la nueva sea distinta se revisan en UsuarioService.
 */
public record CambiarPasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String passwordActual,
        @NotBlank(message = "La contraseña nueva es obligatoria")
        String passwordNueva) {
}
