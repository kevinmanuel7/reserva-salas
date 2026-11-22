package cl.duoc.reservasalas.app.notificaciones

import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Pruebas de qué recordatorios se programan y cuáles se borran.
 * Sin Android: el programador es falso (solo anota) y el reloj está fijo a las 09:00.
 */
class SincronizadorRecordatoriosTest {

    /** Anota cada llamada en vez de usar AlarmManager. */
    private class ProgramadorFalso : ProgramadorRecordatorios {
        val programadas = mutableListOf<Int>()
        val canceladas = mutableListOf<Int>()
        var vecesCancelarTodos = 0

        override fun programar(reserva: Reserva) { programadas.add(reserva.id) }
        override fun cancelar(idReserva: Int) { canceladas.add(idReserva) }
        override fun cancelarTodos() { vecesCancelarTodos++ }
    }

    private val hoy = LocalDate.of(2026, 11, 23)
    private val reloj = object : Reloj {
        override fun ahora(): LocalDateTime = LocalDateTime.of(hoy, LocalTime.of(9, 0))
    }

    private val relator = Usuario(ID_RELATOR, "Ana Relatora", "ana@duoc.cl", Rol.RELATOR)
    private val sesion = Sesion("token", relator)

    private lateinit var programador: ProgramadorFalso
    private lateinit var sincronizador: SincronizadorRecordatorios

    @Before
    fun preparar() {
        programador = ProgramadorFalso()
        sincronizador = SincronizadorRecordatorios(
            MutableStateFlow(emptyList()),
            MutableStateFlow(null),
            programador,
            reloj
        )
    }

    private fun reserva(
        id: Int,
        inicio: LocalTime = LocalTime.of(10, 5),
        idUsuario: Int = ID_RELATOR,
        estado: EstadoReserva = EstadoReserva.RESERVADA
    ) = Reserva(
        id = id,
        codigoSala = "S-101",
        idUsuario = idUsuario,
        nombreUsuario = "Ana Relatora",
        emailUsuario = "ana@duoc.cl",
        fecha = hoy,
        horaInicio = inicio,
        horaFin = inicio.plusMinutes(55),
        cantidadBloques = 1,
        motivo = "Excel intermedio",
        estado = estado,
        creada = LocalDateTime.of(hoy.minusDays(1), LocalTime.NOON)
    )

    @Test
    fun programaUnaReservaPropiaFutura() {
        sincronizador.sincronizar(listOf(reserva(1)), sesion)

        assertEquals(listOf(1), programador.programadas)
    }

    @Test
    fun noProgramaDeOtroUsuarioCanceladaConCheckInNiConAvisoPasado() {
        val lista = listOf(
            reserva(2, idUsuario = 99),
            reserva(3, estado = EstadoReserva.CANCELADA),
            reserva(4, estado = EstadoReserva.EN_USO),
            // Empieza 09:10: el aviso era a las 08:55 y ya pasó.
            reserva(5, inicio = LocalTime.of(9, 10))
        )

        sincronizador.sincronizar(lista, sesion)

        assertEquals(emptyList<Int>(), programador.programadas)
    }

    @Test
    fun noReprogramaSiLaReservaNoCambio() {
        sincronizador.sincronizar(listOf(reserva(1)), sesion)
        sincronizador.sincronizar(listOf(reserva(1)), sesion)

        assertEquals(listOf(1), programador.programadas)
    }

    @Test
    fun cancelaLaQuePasaACancelada() {
        sincronizador.sincronizar(listOf(reserva(1)), sesion)
        sincronizador.sincronizar(listOf(reserva(1, estado = EstadoReserva.CANCELADA)), sesion)

        assertEquals(listOf(1), programador.canceladas)
    }

    @Test
    fun noCancelaUnaReservaQueSoloNoApareceEnLaLista() {
        sincronizador.sincronizar(listOf(reserva(1)), sesion)
        sincronizador.sincronizar(emptyList(), sesion)

        assertEquals(emptyList<Int>(), programador.canceladas)
    }

    @Test
    fun alCerrarSesionCancelaTodos() {
        sincronizador.sincronizar(listOf(reserva(1), reserva(2, inicio = LocalTime.of(11, 5))), sesion)
        sincronizador.sincronizar(listOf(reserva(1), reserva(2, inicio = LocalTime.of(11, 5))), null)

        assertEquals(1, programador.vecesCancelarTodos)
    }

    @Test
    fun reprogramarTodoVuelveAProgramarLasQueYaEstaban() {
        sincronizador.sincronizar(listOf(reserva(1), reserva(2, idUsuario = 99)), sesion)

        sincronizador.reprogramarTodo()

        // La 1 se programa dos veces; la de otro usuario, nunca.
        assertEquals(listOf(1, 1), programador.programadas)
    }

    @Test
    fun reprogramarTodoSinReservasRecibidasNoHaceNada() {
        sincronizador.reprogramarTodo()

        assertEquals(emptyList<Int>(), programador.programadas)
        assertEquals(0, programador.vecesCancelarTodos)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun iniciarReaccionaALosCambiosDeReservasYSesion() {
        val reservas = MutableStateFlow(listOf(reserva(1)))
        val sesionActual = MutableStateFlow<Sesion?>(sesion)
        val scope = CoroutineScope(UnconfinedTestDispatcher())
        val observado = SincronizadorRecordatorios(reservas, sesionActual, programador, reloj)

        observado.iniciar(scope)
        reservas.value = listOf(reserva(1, estado = EstadoReserva.EN_USO))
        sesionActual.value = null

        assertEquals(listOf(1), programador.programadas)
        assertEquals(listOf(1), programador.canceladas)
        assertEquals(1, programador.vecesCancelarTodos)
        scope.cancel()
    }

    @Test
    fun textoConSalaHorarioYActividad() {
        assertEquals(
            "Sala 101 · 10:05–11:00 · Excel intermedio",
            textoRecordatorio(reserva(1), "Sala 101")
        )
    }

    private companion object {
        const val ID_RELATOR = 7
    }
}
