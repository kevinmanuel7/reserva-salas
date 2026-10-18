package cl.duoc.reservasalas.app.data.modelo

import java.time.LocalTime

/** Estado de un equipo. El coordinador lo cambia en "Editar sala". */
enum class EstadoEquipo(val texto: String) {
    OPERATIVO("Operativo"),
    CON_FALLA("Con falla")
}

/** Un equipo de una sala (proyector, computadores, etc.). */
data class Equipo(
    val nombre: String,
    val estado: EstadoEquipo = EstadoEquipo.OPERATIVO,
    /** Comentario del coordinador, ej. "La imagen parpadea". Obligatorio si el equipo tiene falla. */
    val comentario: String? = null
)

/** Horario propio de una sala, cuando no usa el horario general. Horas exactas, ej. 09:00 a 18:00. */
data class HorarioSala(
    val apertura: LocalTime,
    val cierre: LocalTime
)

/** Una sala o espacio que se puede reservar. */
data class Sala(
    val codigo: String,
    val nombre: String,
    val capacidad: Int,
    val ubicacion: String,
    val equipos: List<Equipo>,
    /** null = la sala usa el horario general. */
    val horarioPropio: HorarioSala? = null
) {
    /** true si al menos un equipo tiene falla (la tarjeta muestra el chip rojo "Con falla"). */
    val tieneFallas: Boolean
        get() = equipos.any { it.estado == EstadoEquipo.CON_FALLA }
}

/** Equipos que el coordinador puede agregar a una sala ("+ Agregar equipo"). */
val CATALOGO_EQUIPOS = listOf("Proyector", "Computadores", "Climatización", "Pizarra", "Parlantes")
