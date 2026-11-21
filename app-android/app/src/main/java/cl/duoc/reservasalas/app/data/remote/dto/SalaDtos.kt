package cl.duoc.reservasalas.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Sala tal como la responde ms-salas en GET /api/v1/salas. */
@Serializable
data class SalaDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val capacidad: Int,
    val tieneProyector: Boolean,
    val tieneComputadores: Boolean,
    val tieneClimatizacion: Boolean,
    val activa: Boolean,
    // Campos de la versión 5 del contrato. Tienen valor por defecto por si
    // el servidor (por ejemplo, AWS antes de desplegar) todavía no los envía.
    val ubicacion: String? = null,
    val horaApertura: String? = null,
    val horaCierre: String? = null,
    val equipos: List<EquipoDto> = emptyList()
)

/** Un equipo de la sala. estado llega como texto: "OPERATIVO" o "CON_FALLA". */
@Serializable
data class EquipoDto(
    val nombre: String,
    val estado: String,
    val comentario: String? = null
)

/** Lo que la app envía en PUT /api/v1/salas/{id}. Igual que SalaDto, pero sin id. */
@Serializable
data class ActualizarSalaPeticionDto(
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val capacidad: Int,
    val tieneProyector: Boolean,
    val tieneComputadores: Boolean,
    val tieneClimatizacion: Boolean,
    val activa: Boolean,
    val ubicacion: String? = null,
    val horaApertura: String? = null,
    val horaCierre: String? = null,
    val equipos: List<EquipoDto>? = null
)