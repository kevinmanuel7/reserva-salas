package cl.duoc.reservasalas.app.data.modelo

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Los 5 estados de una reserva (especificación, "Estados de reserva"). */
enum class EstadoReserva(val texto: String) {
    RESERVADA("Reservada"),
    EN_USO("En uso"),
    VERIFICADA("Verificada"),
    NO_PRESENTADO("No presentado"),
    CANCELADA("Cancelada")
}

/** Una reserva de una sala, de 1 a 4 bloques seguidos del mismo día. */
data class Reserva(
    val id: Int,
    val codigoSala: String,
    val idUsuario: Int,
    val nombreUsuario: String,
    val emailUsuario: String,
    val fecha: LocalDate,
    /** Inicio del primer bloque, ej. 10:05. */
    val horaInicio: LocalTime,
    /** Fin del último bloque, ej. 12:00. */
    val horaFin: LocalTime,
    val cantidadBloques: Int,
    val motivo: String,
    val estado: EstadoReserva,
    /** Hora en que el relator escaneó el QR. null = sin check-in. */
    val horaCheckIn: LocalTime? = null,
    val creada: LocalDateTime
) {
    val inicio: LocalDateTime
        get() = LocalDateTime.of(fecha, horaInicio)

    /** Ej. "10:05–12:00" (de corrido aunque sean varios bloques). */
    val horario: String
        get() = "${formatearHora(horaInicio)}–${formatearHora(horaFin)}"

    /** Desde 10 minutos antes hasta 15 minutos después del inicio, solo si está Reservada. */
    fun checkInHabilitado(ahora: LocalDateTime): Boolean =
        estado == EstadoReserva.RESERVADA &&
            !ahora.isBefore(inicio.minusMinutes(MINUTOS_ANTES_CHECK_IN)) &&
            !ahora.isAfter(inicio.plusMinutes(MINUTOS_DESPUES_CHECK_IN))

    /** Empezó hace más de 15 minutos y nadie hizo check-in. El coordinador decide si marca "No presentado". */
    fun sinCheckIn(ahora: LocalDateTime): Boolean =
        estado == EstadoReserva.RESERVADA &&
            ahora.isAfter(inicio.plusMinutes(MINUTOS_DESPUES_CHECK_IN))

    /** El dueño puede cancelar mientras la reserva no haya empezado. */
    fun sePuedeCancelar(ahora: LocalDateTime): Boolean =
        estado == EstadoReserva.RESERVADA && ahora.isBefore(inicio)

    /** Recordatorio: la reserva empieza en 15 minutos o menos. */
    fun mostrarRecordatorio(ahora: LocalDateTime): Boolean =
        estado == EstadoReserva.RESERVADA &&
            ahora.isBefore(inicio) &&
            !ahora.isBefore(inicio.minusMinutes(MINUTOS_RECORDATORIO))

    /** true si esta reserva ocupa el [bloque]. Las canceladas no ocupan; "No presentado" sí (la sala no se libera). */
    fun ocupa(bloque: Bloque): Boolean =
        estado != EstadoReserva.CANCELADA &&
            bloque.inicio.isBefore(horaFin) &&
            bloque.fin.isAfter(horaInicio)

    companion object {
        const val MINUTOS_ANTES_CHECK_IN = 10L
        const val MINUTOS_DESPUES_CHECK_IN = 15L
        const val MINUTOS_RECORDATORIO = 15L
        const val MAXIMO_BLOQUES = 4
        const val DIAS_HACIA_ADELANTE = 14L
    }
}
