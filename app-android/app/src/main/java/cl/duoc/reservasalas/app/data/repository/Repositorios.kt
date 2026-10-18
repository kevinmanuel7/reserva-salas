package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

// Interfaces de los repositorios. Los ViewModels SIEMPRE dependen de estas interfaces,
// nunca de una implementación. En la etapa maqueta, AppContainer entrega las versiones falsas
// (data/falso/); en la próxima evaluación entregará las reales, sin tocar ninguna pantalla.

/** Inicia sesión. La implementación real es [AuthRepository]; la de la maqueta, AutenticacionFalsa. */
interface RepositorioAutenticacion {
    suspend fun login(email: String, password: String): ResultadoLogin
}

/** Salas y su equipamiento. */
interface RepositorioSalas {
    /** Todas las salas. Emite de nuevo cada vez que el coordinador edita una. */
    val salas: StateFlow<List<Sala>>

    fun buscar(codigo: String): Sala?

    /** Reemplaza la sala que tiene el mismo código (lo usa "Editar sala"). */
    suspend fun guardar(sala: Sala)
}

/** Resultado de crear una reserva: o salió bien, o hay un mensaje listo para mostrar. */
sealed interface ResultadoReserva {
    data class Exito(val reserva: Reserva) : ResultadoReserva
    data class Error(val mensaje: String) : ResultadoReserva
}

/** Reservas de todos los usuarios. */
interface RepositorioReservas {
    /** Todas las reservas. Emite de nuevo cada vez que algo cambia (crear, cancelar, check-in...). */
    val reservas: StateFlow<List<Reserva>>

    fun buscar(id: Int): Reserva?

    /** Valida las reglas de reserva (1 a 4 bloques seguidos, no ocupados, hasta 14 días) y la crea. */
    suspend fun crear(
        codigoSala: String,
        usuario: Usuario,
        fecha: LocalDate,
        bloques: List<Bloque>,
        motivo: String
    ): ResultadoReserva

    /** Pasa a Cancelada. */
    suspend fun cancelar(id: Int)

    /** Pasa a En uso y guarda la hora del check-in. */
    suspend fun registrarCheckIn(id: Int, hora: LocalTime)

    /** El coordinador confirmó en persona: pasa a Verificada. */
    suspend fun marcarVerificada(id: Int)

    /** El coordinador la marcó sin check-in: pasa a No presentado (la sala no se libera). */
    suspend fun marcarNoPresentado(id: Int)
}

/** Horario general de bloques del semestre. */
interface RepositorioHorario {
    val horario: StateFlow<HorarioGeneral>

    suspend fun guardar(horario: HorarioGeneral)
}
