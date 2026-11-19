
package cl.duoc.reservasalas.app.data.modelo

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Bloque(
    val inicio: LocalTime,
    val fin: LocalTime
) {
    val texto: String
        get() = "${formatearHora(inicio)}–${formatearHora(fin)}"
}

data class HorarioGeneral(
    val apertura: LocalTime = LocalTime.of(8, 0),
    val cierre: LocalTime = LocalTime.of(21, 0),
    val pausaMinutos: Int = 5,
    val dias: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    )
) {
    fun bloques(): List<Bloque> =
        bloquesEntre(apertura, cierre, pausaMinutos)
}

fun bloquesEntre(
    apertura: LocalTime,
    cierre: LocalTime,
    pausaMinutos: Int
): List<Bloque> =
    (apertura.hour until cierre.hour).mapIndexed { indice, hora ->
        val inicio = if (indice == 0) {
            LocalTime.of(hora, 0)
        } else {
            LocalTime.of(hora, pausaMinutos)
        }

        Bloque(
            inicio = inicio,
            fin = LocalTime.of(hora + 1, 0)
        )
    }

fun Sala.bloques(general: HorarioGeneral): List<Bloque> {
    val propio = horarioPropio ?: return general.bloques()

    return bloquesEntre(
        propio.apertura,
        propio.cierre,
        general.pausaMinutos
    )
}

enum class EstadoBloque {
    LIBRE,
    OCUPADO,
    PASADO
}

fun estadoBloque(
    bloque: Bloque,
    codigoSala: String,
    fecha: LocalDate,
    reservas: List<Reserva>,
    ahora: LocalDateTime
): EstadoBloque {
    val ocupado = reservas.any { reserva ->
        reserva.codigoSala == codigoSala &&
                reserva.fecha == fecha &&
                reserva.ocupa(bloque)
    }

    val pasado = fecha == ahora.toLocalDate() &&
            !bloque.inicio.isAfter(ahora.toLocalTime())

    return when {
        ocupado -> EstadoBloque.OCUPADO
        pasado -> EstadoBloque.PASADO
        else -> EstadoBloque.LIBRE
    }
}
