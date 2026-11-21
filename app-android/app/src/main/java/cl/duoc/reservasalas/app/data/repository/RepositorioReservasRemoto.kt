package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.remote.ReservasApi
import cl.duoc.reservasalas.app.data.remote.dto.CrearReservaPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.ErrorDto
import cl.duoc.reservasalas.app.data.remote.dto.ReservaDto
import cl.duoc.reservasalas.app.data.remote.dto.aModelo
import cl.duoc.reservasalas.app.data.remote.dto.numeroBloque
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Reservas reales desde ms-reservas. Cumple el mismo contrato que RepositorioReservasFalso,
 * así que las pantallas no notan la diferencia.
 */
class RepositorioReservasRemoto(
    private val api: ReservasApi,
    private val repositorioSalas: RepositorioSalas,
    private val reloj: Reloj,
    private val json: Json
) : RepositorioReservas {

    // Copia en memoria que observan las pantallas. Parte vacía: no se carga en init porque
    // GET /reservas pide token, y al abrir la app todavía puede no haber sesión.
    private val _reservas = MutableStateFlow<List<Reserva>>(emptyList())
    override val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    override fun buscar(id: Int): Reserva? =
        _reservas.value.find { it.id == id }

    /** Pide las reservas desde hace 7 días hasta 14 días más. La llaman las pantallas al abrirse. */
    override suspend fun refrescar() {
        try {
            // Sin salas no se puede traducir salaId al código de la sala.
            if (repositorioSalas.salas.value.isEmpty()) repositorioSalas.refrescar()

            val hoy = reloj.hoy()
            val dias = (-DIAS_ATRAS..Reserva.DIAS_HACIA_ADELANTE).map { hoy.plusDays(it) }

            // async lanza las 22 peticiones a la vez; awaitAll espera que terminen todas.
            val dtos = coroutineScope {
                dias.map { dia -> async { api.listar(dia.toString()) } }.awaitAll()
            }.flatten()

            _reservas.value = dtos.map { it.aReserva() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión o error del servidor: se conserva la última lista que se tenía.
        }
    }

    override suspend fun crear(
        codigoSala: String,
        usuario: Usuario,
        fecha: LocalDate,
        bloques: List<Bloque>,
        motivo: String
    ): ResultadoReserva {
        val ordenados = bloques.sortedBy { it.inicio }
        val hoy = reloj.hoy()

        // Validaciones locales (las mismas del repositorio falso). Los cruces los revisa el servidor.
        val error = when {
            ordenados.isEmpty() ->
                "Elige al menos un bloque"

            ordenados.size > Reserva.MAXIMO_BLOQUES ->
                "Puedes reservar hasta ${Reserva.MAXIMO_BLOQUES} bloques seguidos"

            !sonSeguidos(ordenados) ->
                "Los bloques deben ser seguidos"

            fecha.isBefore(hoy) || fecha.isAfter(hoy.plusDays(Reserva.DIAS_HACIA_ADELANTE)) ->
                "Solo puedes reservar desde hoy hasta ${Reserva.DIAS_HACIA_ADELANTE} días más"

            fecha == hoy && ordenados.first().inicio.isBefore(reloj.ahora().toLocalTime()) ->
                "Ese bloque ya empezó"

            else -> null
        }
        if (error != null) return ResultadoReserva.Error(error)

        val sala = repositorioSalas.buscar(codigoSala)
            ?: return ResultadoReserva.Error("No se encontró la sala")

        return try {
            val creada = api.crear(
                CrearReservaPeticionDto(
                    salaId = sala.id,
                    fecha = fecha.toString(),
                    bloqueInicio = numeroBloque(ordenados.first().inicio),
                    bloqueFin = numeroBloque(ordenados.last().inicio),
                    actividad = motivo.trim()
                )
            ).aReserva()
            _reservas.update { it + creada }
            ResultadoReserva.Exito(creada)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoReserva.Error(mensajeDeError(e, "No se pudo crear la reserva. Intenta nuevamente."))
        }
    }

    override suspend fun cancelar(id: Int) =
        cambiar("No se pudo cancelar la reserva") { api.cancelar(id.toLong()) }

    // El parámetro hora no se usa: el servidor guarda la hora de Chile.
    override suspend fun registrarCheckIn(id: Int, hora: LocalTime) =
        cambiar("No se pudo registrar el check-in") { api.registrarCheckIn(id.toLong()) }

    override suspend fun marcarVerificada(id: Int) =
        cambiar("No se pudo verificar la reserva") { api.verificar(id.toLong()) }

    override suspend fun marcarNoPresentado(id: Int) =
        cambiar("No se pudo marcar como no presentado") { api.marcarNoPresentado(id.toLong()) }

    /**
     * Llama al endpoint y reemplaza la reserva en la lista con la respuesta del servidor.
     * Si falla, lanza una excepción con un mensaje listo para mostrar en pantalla.
     */
    private suspend fun cambiar(mensajeGenerico: String, llamada: suspend () -> ReservaDto) {
        val actualizada = try {
            llamada().aReserva()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(mensajeDeError(e, mensajeGenerico))
        }
        _reservas.update { lista ->
            lista.map { if (it.id == actualizada.id) actualizada else it }
        }
    }

    /** Completa lo que el servidor no envía: el código de la sala y un texto en vez del email. */
    private fun ReservaDto.aReserva(): Reserva = aModelo(
        codigoSala = repositorioSalas.salas.value.find { it.id == salaId }?.codigo ?: "",
        // El backend no envía el email; se muestra el id para que el texto no quede vacío.
        emailUsuario = "ID de usuario: $usuarioId"
    )

    /** Traduce la excepción al mensaje que ve el usuario (nunca el error técnico). */
    private fun mensajeDeError(e: Exception, mensajeGenerico: String): String = when (e) {
        is IOException -> MENSAJE_SIN_CONEXION
        is HttpException -> {
            val error = try {
                e.response()?.errorBody()?.string()?.let { json.decodeFromString<ErrorDto>(it) }
            } catch (_: Exception) {
                null
            }
            when {
                error == null -> mensajeGenerico
                error.codigo == "USUARIO_OCUPADO" -> "Ya tienes una reserva en ese horario"
                error.codigo == "CRUCE_HORARIO" -> "Uno de los bloques ya está ocupado"
                error.mensaje.isNotBlank() -> error.mensaje
                else -> mensajeGenerico
            }
        }
        else -> mensajeGenerico
    }

    private fun sonSeguidos(ordenados: List<Bloque>): Boolean =
        ordenados.zipWithNext().all { (anterior, siguiente) ->
            Duration.between(anterior.fin, siguiente.inicio).toMinutes() in 0..MAXIMA_PAUSA_MINUTOS
        }

    private companion object {
        const val DIAS_ATRAS = 7L
        const val MAXIMA_PAUSA_MINUTOS = 15L
        const val MENSAJE_SIN_CONEXION = "Sin conexión"
    }
}
