package cl.duoc.reservasalas.app.data.remote.dto

import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.serialization.Serializable

// DTO (Data Transfer Object): copian exactamente el JSON de POST /auth/login
// (plan, sección 5). Los nombres de campo deben ser idénticos al JSON.

/** Lo que la app envía a POST /auth/login. */
@Serializable
data class LoginPeticionDto(
    val email: String,
    val password: String
)

/** Respuesta 200 de POST /auth/login. */
@Serializable
data class LoginRespuestaDto(
    val token: String,
    val usuario: UsuarioDto
)

/** Usuario tal como llega en el JSON. El rol llega como texto ("RELATOR"). */
@Serializable
data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String
)

/**
 * Convierte el DTO en el modelo de la app.
 * Si el rol no es RELATOR ni COORDINADOR, lanza IllegalArgumentException.
 */
fun UsuarioDto.aModelo(): Usuario = Usuario(
    id = id,
    nombre = nombre,
    email = email,
    rol = Rol.valueOf(rol)
)
