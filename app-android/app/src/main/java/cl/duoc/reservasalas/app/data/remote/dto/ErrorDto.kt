package cl.duoc.reservasalas.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Formato estándar de error de los tres microservicios (plan, sección 5):
 * {"codigo": "CREDENCIALES_INVALIDAS", "mensaje": "..."}
 */
@Serializable
data class ErrorDto(
    val codigo: String,
    val mensaje: String
)
