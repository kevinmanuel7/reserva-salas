package cl.duoc.reservasalas.app.data.modelo

import java.time.DayOfWeek
import java.time.LocalTime

/** Un bloque que se puede reservar, ej. 09:05–10:00. */
data class Bloque(
    val inicio: LocalTime,
    val fin: LocalTime
) {
    /** Ej. "09:05–10:00". */
    val texto: String
        get() = "${formatearHora(inicio)}–${formatearHora(fin)}"
}

/**
 * Horario general del semestre. Lo edita el coordinador en "Horario general".
 * Por defecto: 08:00 a 21:00, 5 minutos de pausa entre bloques, lunes a viernes.
 */
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
    /** Los bloques de un día: 08:00–09:00, 09:05–10:00 ... 20:05–21:00 (13 bloques). */
    fun bloques(): List<Bloque> = bloquesEntre(apertura, cierre, pausaMinutos)
}

/**
 * Bloques entre dos horas exactas. Cada bloque termina en hora exacta y,
 * desde el segundo, empieza [pausaMinutos] después, para que un grupo salga antes de que entre el siguiente.
 */
fun bloquesEntre(apertura: LocalTime, cierre: LocalTime, pausaMinutos: Int): List<Bloque> =
    (apertura.hour until cierre.hour).mapIndexed { indice, hora ->
        val inicio = if (indice == 0) LocalTime.of(hora, 0) else LocalTime.of(hora, pausaMinutos)
        Bloque(inicio, LocalTime.of(hora + 1, 0))
    }

/** Los bloques de una sala: los de su horario propio, o los del horario general si no tiene. */
fun Sala.bloques(general: HorarioGeneral): List<Bloque> {
    val propio = horarioPropio ?: return general.bloques()
    return bloquesEntre(propio.apertura, propio.cierre, general.pausaMinutos)
}
