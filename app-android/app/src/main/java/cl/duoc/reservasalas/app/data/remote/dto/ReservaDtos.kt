package cl.duoc.reservasalas.app.data.remote.dto

import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.Reserva
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Reserva tal como la responde ms-reservas (Contrato 1 v6). */
@Serializable
data class ReservaDto(
    val id: Long,
    val salaId: Long,
    val usuarioId: Long,
    val nombreRelator: String,
    val actividad: String,
    val fecha: String,
    val bloqueInicio: Int,
    val bloqueFin: Int,
    // "ACTIVA" o "CANCELADA".
    val estado: String,
    // Campos de la versión 6. Valen null hasta que alguien los marca.
    val horaCheckIn: String? = null,
    val verificada: Boolean? = null,
    val noPresentado: Boolean? = null
)

/** Lo que la app envía en POST /api/v1/reservas. El usuario lo saca el servidor del token. */
@Serializable
data class CrearReservaPeticionDto(
    val salaId: Long,
    val fecha: String,
    val bloqueInicio: Int,
    val bloqueFin: Int,
    val actividad: String
)

/** Hora de inicio del bloque n: el 1 parte a las 08:00 y los demás a las HH:05 (pausa de 5 minutos). */
fun horaInicioBloque(n: Int): LocalTime =
    if (n == 1) LocalTime.of(8, 0) else LocalTime.of(7 + n, 5)

/** Hora de término del bloque n. Ej. el bloque 2 termina a las 10:00. */
fun horaFinBloque(n: Int): LocalTime = LocalTime.of(8 + n, 0)

/** Número del bloque que empieza a esa hora. Ej. 09:05 → bloque 2. */
fun numeroBloque(hora: LocalTime): Int = hora.hour - 7

/**
 * Convierte la reserva del servidor en la que usan las pantallas.
 * El servidor no envía el código de la sala ni el email: los entrega el repositorio.
 */
fun ReservaDto.aModelo(codigoSala: String, emailUsuario: String): Reserva {
    val dia = LocalDate.parse(fecha)
    val inicio = horaInicioBloque(bloqueInicio)
    return Reserva(
        id = id.toInt(),
        codigoSala = codigoSala,
        idUsuario = usuarioId.toInt(),
        nombreUsuario = nombreRelator,
        emailUsuario = emailUsuario,
        fecha = dia,
        horaInicio = inicio,
        horaFin = horaFinBloque(bloqueFin),
        cantidadBloques = bloqueFin - bloqueInicio + 1,
        motivo = actividad,
        estado = when {
            estado == "CANCELADA" -> EstadoReserva.CANCELADA
            noPresentado == true -> EstadoReserva.NO_PRESENTADO
            verificada == true -> EstadoReserva.VERIFICADA
            horaCheckIn != null -> EstadoReserva.EN_USO
            else -> EstadoReserva.RESERVADA
        },
        horaCheckIn = horaCheckIn?.let { LocalTime.parse(it) },
        creada = LocalDateTime.of(dia, inicio)
    )
}
