package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.remote.ReservasApi
import cl.duoc.reservasalas.app.data.remote.dto.ErrorDto
import cl.duoc.reservasalas.app.data.remote.dto.aDto
import cl.duoc.reservasalas.app.data.remote.dto.aModelo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * Horario general real desde ms-reservas. Cumple el mismo contrato que RepositorioHorarioFalso,
 * así que las pantallas no notan la diferencia.
 */
class RepositorioHorarioRemoto(
    private val api: ReservasApi,
    private val json: Json
) : RepositorioHorario {

    // Copia en memoria que observan las pantallas. Mientras el servidor no responde,
    // parte con el horario predeterminado (08:00 a 21:00, pausa 5, lunes a viernes).
    private val _horario = MutableStateFlow(HorarioGeneral())
    override val horario: StateFlow<HorarioGeneral> = _horario.asStateFlow()

    // Alcance propio para la primera carga, que no depende de ninguna pantalla.
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // GET /horario es público, así que se puede cargar al abrir la app aunque no haya sesión.
    init {
        alcance.launch { refrescar() }
    }

    override suspend fun refrescar() {
        try {
            _horario.value = api.obtenerHorario().aModelo()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión o error del servidor: se conserva el último horario que se tenía.
        }
    }

    /** Guarda en el servidor. Si falla, lanza una excepción con un mensaje listo para mostrar. */
    override suspend fun guardar(horario: HorarioGeneral) {
        val guardado = try {
            api.actualizarHorario(horario.aDto()).aModelo()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(mensajeDeError(e))
        }
        _horario.value = guardado
    }

    /** Traduce la excepción al mensaje que ve el usuario (nunca el error técnico). */
    private fun mensajeDeError(e: Exception): String = when (e) {
        is IOException -> MENSAJE_SIN_CONEXION
        is HttpException -> {
            val error = try {
                e.response()?.errorBody()?.string()?.let { json.decodeFromString<ErrorDto>(it) }
            } catch (_: Exception) {
                null
            }
            error?.mensaje?.takeIf { it.isNotBlank() } ?: MENSAJE_GENERICO
        }
        else -> MENSAJE_GENERICO
    }

    private companion object {
        const val MENSAJE_SIN_CONEXION = "Sin conexión"
        const val MENSAJE_GENERICO = "No se pudo guardar el horario. Inténtalo nuevamente."
    }
}
