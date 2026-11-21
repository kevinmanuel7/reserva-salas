package cl.duoc.reservasalas.app.data.remote.dto

import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.formatearHora
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Horario general tal como lo responde y lo recibe ms-reservas en /api/v1/horario.
 * Ej.: {"apertura": "08:00", "cierre": "21:00", "pausaMinutos": 5, "dias": ["MONDAY", "FRIDAY"]}
 */
@Serializable
data class HorarioDto(
    val apertura: String,
    val cierre: String,
    val pausaMinutos: Int,
    val dias: List<String>
)

/** Convierte el horario del servidor en el que usan las pantallas. "MONDAY" pasa a DayOfWeek.MONDAY. */
fun HorarioDto.aModelo(): HorarioGeneral = HorarioGeneral(
    apertura = LocalTime.parse(apertura),
    cierre = LocalTime.parse(cierre),
    pausaMinutos = pausaMinutos,
    dias = dias.map { DayOfWeek.valueOf(it) }.toSet()
)

/** Arma el cuerpo del PUT. Las horas van siempre con dos dígitos ("08:00") y los días en orden. */
fun HorarioGeneral.aDto(): HorarioDto = HorarioDto(
    apertura = formatearHora(apertura),
    cierre = formatearHora(cierre),
    pausaMinutos = pausaMinutos,
    dias = dias.sorted().map { it.name }
)
