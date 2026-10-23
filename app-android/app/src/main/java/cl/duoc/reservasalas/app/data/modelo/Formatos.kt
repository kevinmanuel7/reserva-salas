package cl.duoc.reservasalas.app.data.modelo

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ESPANOL_CHILE: Locale = Locale.forLanguageTag("es-CL")
private val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val FORMATO_FECHA_LARGA: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ESPANOL_CHILE)
private val FORMATO_FECHA_CORTA: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d", ESPANOL_CHILE)

/** Ej. "09:05". */
fun formatearHora(hora: LocalTime): String = hora.format(FORMATO_HORA)

/** Ej. "Jueves 15 de octubre". */
fun formatearFechaLarga(fecha: LocalDate): String =
    fecha.format(FORMATO_FECHA_LARGA).replaceFirstChar { it.titlecase(ESPANOL_CHILE) }

/** Ej. "Jueves 15". */
fun formatearFechaCorta(fecha: LocalDate): String =
    fecha.format(FORMATO_FECHA_CORTA).replaceFirstChar { it.titlecase(ESPANOL_CHILE) }
