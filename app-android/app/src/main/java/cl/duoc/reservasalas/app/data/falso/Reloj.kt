package cl.duoc.reservasalas.app.data.falso

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * De dónde sale "ahora". Las pantallas y los ViewModels NUNCA usan LocalDateTime.now() directo:
 * piden el reloj al AppContainer (container.reloj).
 */
interface Reloj {
    fun ahora(): LocalDateTime

    fun hoy(): LocalDate = ahora().toLocalDate()
}

/**
 * Reloj de la maqueta: la fecha es la de hoy, pero la hora siempre es 09:55.
 * Así la demo se ve igual a cualquier hora del día: hay un recordatorio (LAB-01 a las 10:05),
 * una reserva en uso, una sin check-in y una verificada.
 */
class RelojMaqueta(private val hora: LocalTime = LocalTime.of(9, 55)) : Reloj {
    override fun ahora(): LocalDateTime = LocalDateTime.of(LocalDate.now(), hora)
}

/** Reloj real del teléfono. Se usará cuando la app se conecte al backend. */
class RelojSistema : Reloj {
    override fun ahora(): LocalDateTime = LocalDateTime.now()
}
