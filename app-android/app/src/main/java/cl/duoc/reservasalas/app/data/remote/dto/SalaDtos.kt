package cl.duoc.reservasalas.app.data.remote.dto

import kotlinx.serialization.Serializable
import cl.duoc.reservasalas.app.data.modelo.Equipo
import cl.duoc.reservasalas.app.data.modelo.EstadoEquipo
import cl.duoc.reservasalas.app.data.modelo.HorarioSala
import cl.duoc.reservasalas.app.data.modelo.Sala
import java.time.LocalTime

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
    val equipos: List<EquipoDto>? = null,
    // Versión 9: con true, el servidor deja la sala sin horario propio (usa el general).
    val usaHorarioGeneral: Boolean? = null
)
/** Convierte la sala del servidor en la sala que usan las pantallas. */
fun SalaDto.aModelo(): Sala = Sala(
    codigo = codigo,
    nombre = nombre,
    capacidad = capacidad,
    ubicacion = ubicacion ?: "Sin ubicación",
    equipos = equipos.map { it.aModelo() },
    horarioPropio = if (horaApertura != null && horaCierre != null) {
        HorarioSala(LocalTime.parse(horaApertura), LocalTime.parse(horaCierre))
    } else {
        null
    },
    id = id
)

/** Convierte un equipo del servidor. "CON_FALLA" pasa a EstadoEquipo.CON_FALLA. */
fun EquipoDto.aModelo(): Equipo = Equipo(
    nombre = nombre,
    estado = EstadoEquipo.valueOf(estado),
    comentario = comentario
)

/**
 * Arma el cuerpo del PUT. La app no maneja tipo, activa ni los tres booleanos,
 * así que se copian de la sala original que respondió el servidor.
 * El backend recalcula los booleanos a partir de la lista de equipos.
 */
fun Sala.aPeticion(original: SalaDto): ActualizarSalaPeticionDto = ActualizarSalaPeticionDto(
    codigo = codigo,
    nombre = nombre,
    tipo = original.tipo,
    capacidad = capacidad,
    tieneProyector = original.tieneProyector,
    tieneComputadores = original.tieneComputadores,
    tieneClimatizacion = original.tieneClimatizacion,
    activa = original.activa,
    ubicacion = ubicacion,
    horaApertura = horarioPropio?.apertura?.toString(),
    horaCierre = horarioPropio?.cierre?.toString(),
    equipos = equipos.map { EquipoDto(it.nombre, it.estado.name, it.comentario) },
    // Sin horario propio, la sala vuelve al horario general.
    usaHorarioGeneral = horarioPropio == null
)