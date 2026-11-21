package cl.duoc.reservasalas.app.model

import java.time.LocalDate

/**
 * Usuario con sesión iniciada, tal como lo usa la app.
 * Es distinto del DTO: aquí el rol ya es un [Rol] y no un texto libre.
 */
data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: Rol
)

/**
 * Datos del perfil que guarda ms-usuarios (GET /usuarios/me y GET /usuarios/{id}).
 * Los tres campos extra son opcionales: null = el usuario todavía no los registra.
 * telefono viene completo, ej. "+56912345678".
 */
data class PerfilUsuario(
    val usuario: Usuario,
    val fechaNacimiento: LocalDate?,
    val telefono: String?,
    val area: String?
)

/** Los únicos dos perfiles que existen (plan, sección 4). */
enum class Rol {
    RELATOR,
    COORDINADOR
}
