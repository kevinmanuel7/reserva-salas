package cl.duoc.reservasalas.app.model

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

/** Los únicos dos perfiles que existen (plan, sección 4). */
enum class Rol {
    RELATOR,
    COORDINADOR
}
