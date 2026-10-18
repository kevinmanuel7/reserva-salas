package cl.duoc.reservasalas.app.data.falso

import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.AuthRepository
import cl.duoc.reservasalas.app.data.repository.RepositorioAutenticacion
import cl.duoc.reservasalas.app.data.repository.RepositorioHorario
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import cl.duoc.reservasalas.app.data.repository.ResultadoLogin
import cl.duoc.reservasalas.app.data.repository.ResultadoReserva
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

// Repositorios de la etapa maqueta: guardan todo en memoria (MutableStateFlow).
// Los cambios se ven en todas las pantallas mientras la app esté abierta;
// al cerrarla, los datos vuelven a ser los de DatosFalsos.

/** Login sin backend: valida contra los 7 usuarios de DatosFalsos y guarda la sesión real en DataStore. */
class AutenticacionFalsa(private val sesionDataStore: SesionDataStore) : RepositorioAutenticacion {

    override suspend fun login(email: String, password: String): ResultadoLogin {
        delay(RETARDO_FALSO_MS)
        val usuario = DatosFalsos.usuarios.find { it.email.equals(email, ignoreCase = true) }
        if (usuario == null || password != DatosFalsos.CONTRASENA_MAQUETA) {
            return ResultadoLogin.Error(AuthRepository.MENSAJE_CREDENCIALES_INVALIDAS)
        }
        sesionDataStore.guardarSesion(Sesion(TOKEN_MAQUETA, usuario))
        return ResultadoLogin.Exito(usuario)
    }

    private companion object {
        const val TOKEN_MAQUETA = "token-maqueta"
    }
}

class RepositorioSalasFalso : RepositorioSalas {

    private val _salas = MutableStateFlow(DatosFalsos.salas)
    override val salas: StateFlow<List<Sala>> = _salas.asStateFlow()

    override fun buscar(codigo: String): Sala? = _salas.value.find { it.codigo == codigo }

    override suspend fun guardar(sala: Sala) {
        delay(RETARDO_FALSO_MS)
        _salas.update { lista -> lista.map { if (it.codigo == sala.codigo) sala else it } }
    }
}

class RepositorioHorarioFalso : RepositorioHorario {

    private val _horario = MutableStateFlow(HorarioGeneral())
    override val horario: StateFlow<HorarioGeneral> = _horario.asStateFlow()

    override suspend fun guardar(horario: HorarioGeneral) {
        delay(RETARDO_FALSO_MS)
        _horario.value = horario
    }
}

class RepositorioReservasFalso(private val reloj: Reloj) : RepositorioReservas {

    private val _reservas = MutableStateFlow(DatosFalsos.reservas(reloj.hoy()))
    override val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    override fun buscar(id: Int): Reserva? = _reservas.value.find { it.id == id }

    override suspend fun crear(
        codigoSala: String,
        usuario: Usuario,
        fecha: LocalDate,
        bloques: List<Bloque>,
        motivo: String
    ): ResultadoReserva {
        delay(RETARDO_FALSO_MS)
        val ordenados = bloques.sortedBy { it.inicio }
        val hoy = reloj.hoy()

        val error = when {
            ordenados.isEmpty() -> "Elige al menos un bloque"
            ordenados.size > Reserva.MAXIMO_BLOQUES -> "Puedes reservar hasta ${Reserva.MAXIMO_BLOQUES} bloques seguidos"
            !sonSeguidos(ordenados) -> "Los bloques deben ser seguidos"
            fecha.isBefore(hoy) || fecha.isAfter(hoy.plusDays(Reserva.DIAS_HACIA_ADELANTE)) ->
                "Solo puedes reservar desde hoy hasta ${Reserva.DIAS_HACIA_ADELANTE} días más"
            fecha == hoy && ordenados.first().inicio.isBefore(reloj.ahora().toLocalTime()) ->
                "Ese bloque ya empezó"
            estaOcupado(codigoSala, fecha, ordenados) -> "Uno de los bloques ya está ocupado"
            else -> null
        }
        if (error != null) return ResultadoReserva.Error(error)

        val nueva = Reserva(
            id = (_reservas.value.maxOfOrNull { it.id } ?: 0) + 1,
            codigoSala = codigoSala,
            idUsuario = usuario.id,
            nombreUsuario = usuario.nombre,
            emailUsuario = usuario.email,
            fecha = fecha,
            horaInicio = ordenados.first().inicio,
            horaFin = ordenados.last().fin,
            cantidadBloques = ordenados.size,
            motivo = motivo.trim(),
            estado = EstadoReserva.RESERVADA,
            creada = reloj.ahora()
        )
        _reservas.update { it + nueva }
        return ResultadoReserva.Exito(nueva)
    }

    override suspend fun cancelar(id: Int) =
        cambiar(id) { it.copy(estado = EstadoReserva.CANCELADA) }

    override suspend fun registrarCheckIn(id: Int, hora: LocalTime) =
        cambiar(id) { it.copy(estado = EstadoReserva.EN_USO, horaCheckIn = hora) }

    override suspend fun marcarVerificada(id: Int) =
        cambiar(id) { it.copy(estado = EstadoReserva.VERIFICADA) }

    override suspend fun marcarNoPresentado(id: Int) =
        cambiar(id) { it.copy(estado = EstadoReserva.NO_PRESENTADO) }

    /** Aplica [cambio] solo a la reserva con ese [id]. */
    private suspend fun cambiar(id: Int, cambio: (Reserva) -> Reserva) {
        delay(RETARDO_FALSO_MS)
        _reservas.update { lista -> lista.map { if (it.id == id) cambio(it) else it } }
    }

    /** Seguidos = entre el fin de un bloque y el inicio del siguiente solo hay la pausa (0 a 15 minutos). */
    private fun sonSeguidos(ordenados: List<Bloque>): Boolean =
        ordenados.zipWithNext().all { (anterior, siguiente) ->
            Duration.between(anterior.fin, siguiente.inicio).toMinutes() in 0..MAXIMA_PAUSA_MINUTOS
        }

    private fun estaOcupado(codigoSala: String, fecha: LocalDate, bloques: List<Bloque>): Boolean =
        _reservas.value.any { reserva ->
            reserva.codigoSala == codigoSala &&
                reserva.fecha == fecha &&
                bloques.any { bloque -> reserva.ocupa(bloque) }
        }

    private companion object {
        const val MAXIMA_PAUSA_MINUTOS = 15L
    }
}
