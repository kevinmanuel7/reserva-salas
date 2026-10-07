package cl.duoc.reservasalas.usuarios.dto;

/**
 * Respuesta 200 de POST /auth/login: el token y los datos del usuario.
 */
public record LoginResponse(String token, UsuarioDto usuario) {
}
