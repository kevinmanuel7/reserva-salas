package cl.duoc.reservasalas.app.data.remote.dto

import cl.duoc.reservasalas.app.data.modelo.Feriado
import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Un feriado tal como llega de la API externa Nager.Date
 * (GET https://date.nager.at/api/v3/PublicHolidays/{anio}/CL).
 * Solo se copian los campos que usa la app; los demás (name, fixed, types...) se ignoran
 * gracias a ignoreUnknownKeys del Json de AppContainer.
 */
@Serializable
data class FeriadoDto(
    /** Fecha "yyyy-MM-dd", ej. "2026-12-25". */
    val date: String,
    /** Nombre en español, ej. "Navidad". */
    val localName: String,
    /** true = feriado en todo Chile. */
    val global: Boolean,
    /** Si no es global: regiones donde rige, ej. ["CL-AP"]. null en los nacionales. */
    val counties: List<String>? = null
)

fun FeriadoDto.aModelo(): Feriado = Feriado(
    fecha = LocalDate.parse(date),
    nombre = localName
)
