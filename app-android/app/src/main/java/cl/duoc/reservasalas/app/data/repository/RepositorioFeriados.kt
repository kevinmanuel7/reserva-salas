package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.Feriado
import cl.duoc.reservasalas.app.data.remote.FeriadosApi
import cl.duoc.reservasalas.app.data.remote.dto.FeriadoDto
import cl.duoc.reservasalas.app.data.remote.dto.aModelo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/**
 * Feriados reales desde Nager.Date, guardados en memoria por año: la primera vez que se
 * necesita un año se pide a la API y las siguientes se usa lo guardado.
 */
class RepositorioFeriadosRemoto(
    private val api: FeriadosApi
) : RepositorioFeriados {

    // Año → feriados de ese año que aplican a la sede, ordenados por fecha.
    private val porAnio = mutableMapOf<Int, List<Feriado>>()

    // Mutex: si dos pantallas piden el mismo año a la vez, la segunda espera a la primera
    // en vez de hacer una segunda petición.
    private val candado = Mutex()

    override suspend fun feriadoEn(fecha: LocalDate): Feriado? =
        feriadosDe(fecha.year).find { it.fecha == fecha }

    override suspend fun proximoFeriado(desde: LocalDate): Feriado? =
        feriadosDe(desde.year).firstOrNull { !it.fecha.isBefore(desde) }
            // Desde el 30 de diciembre ya no quedan feriados ese año: se busca en el siguiente.
            ?: feriadosDe(desde.year + 1).firstOrNull()

    private suspend fun feriadosDe(anio: Int): List<Feriado> = candado.withLock {
        porAnio[anio]?.let { return@withLock it }
        try {
            val feriados = api.feriados(anio)
                .filter { it.aplicaALaSede() }
                .map { it.aModelo() }
                .sortedBy { it.fecha }
            porAnio[anio] = feriados
            feriados
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión o respuesta inesperada: lista vacía y NO se guarda, así la próxima
            // consulta vuelve a intentarlo. La app funciona igual, sin avisos de feriados.
            emptyList()
        }
    }

    /** La sede está en Viña del Mar: sirven los nacionales y los de la región de Valparaíso. */
    private fun FeriadoDto.aplicaALaSede(): Boolean =
        global || counties.orEmpty().contains(REGION_VALPARAISO)

    companion object {
        /** Código ISO 3166-2 de la Región de Valparaíso. */
        const val REGION_VALPARAISO = "CL-VS"
    }
}
