package cl.duoc.reservasalas.app.data.modelo

import java.time.LocalDate

/** Un feriado que aplica a la sede Viña del Mar (nacional o de la región de Valparaíso). */
data class Feriado(
    val fecha: LocalDate,
    /** Nombre en español, ej. "Navidad". */
    val nombre: String
)
