
package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

interface RepositorioAutenticacion {
    suspend fun login(
        email: String,
        password: String
    ): ResultadoLogin
}

interface RepositorioSalas {
    val salas: StateFlow<List<Sala>>

    fun buscar(codigo: String): Sala?

    suspend fun guardar(sala: Sala)
}

sealed interface ResultadoReserva {
    data class Exito(
        val reserva: Reserva
    ) : ResultadoReserva

    data class Error(
        val mensaje: String
    ) : ResultadoReserva
}

interface RepositorioReservas {
    val reservas: StateFlow<List<Reserva>>

    fun buscar(id: Int): Reserva?

    /**
     * Valida las reglas de reserva:
     * 1 a 4 bloques seguidos,
     * bloques no ocupados por otra reserva de la sala,
     * sin cruces con reservas del mismo usuario
     * y con un máximo de 14 días de anticipación.
     */
    suspend fun crear(
        codigoSala: String,
        usuario: Usuario,
        fecha: LocalDate,
        bloques: List<Bloque>,
        motivo: String
    ): ResultadoReserva

    suspend fun cancelar(id: Int)

    suspend fun registrarCheckIn(
        id: Int,
        hora: LocalTime
    )

    suspend fun marcarVerificada(id: Int)

    suspend fun marcarNoPresentado(id: Int)
}

interface RepositorioHorario {
    val horario: StateFlow<HorarioGeneral>

    suspend fun guardar(horario: HorarioGeneral)
}
