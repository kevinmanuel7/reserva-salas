package cl.duoc.reservasalas.app.data.remote.dto

import cl.duoc.reservasalas.app.model.PerfilUsuario
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.serialization.Serializable
import java.time.LocalDate

// DTO (Data Transfer Object): copian exactamente el JSON de ms-usuarios
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

/**
 * Usuario tal como llega en el JSON. El rol llega como texto ("RELATOR").
 * fechaNacimiento ("yyyy-MM-dd", Contrato 1 v8), telefono y area (v10) pueden venir null.
 * "= null" les da un valor por defecto: si el JSON no los trae, el usuario igual se lee.
 */
@Serializable
data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val fechaNacimiento: String? = null,
    val telefono: String? = null,
    val area: String? = null
)

/** Cuerpo de PUT /usuarios/me (Contrato 1 v10). Un campo null conserva lo guardado en el servidor. */
@Serializable
data class ActualizarPerfilPeticionDto(
    val fechaNacimiento: String?,
    val telefono: String?,
    val area: String?
)

/** Cuerpo de PUT /usuarios/me/password (Contrato 1 v10). */
@Serializable
data class CambiarPasswordPeticionDto(
    val passwordActual: String,
    val passwordNueva: String
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

/** Igual que aModelo(), pero con los datos del perfil. LocalDate.parse lee "yyyy-MM-dd". */
fun UsuarioDto.aPerfil(): PerfilUsuario = PerfilUsuario(
    usuario = aModelo(),
    fechaNacimiento = fechaNacimiento?.let { LocalDate.parse(it) },
    telefono = telefono,
    area = area
)
