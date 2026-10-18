package cl.duoc.reservasalas.app.data.falso

import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.repository.ResultadoReserva
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Pruebas de las reglas de reserva del repositorio falso y del horario de bloques.
 * runTest salta los delay(), así que las pruebas son instantáneas.
 */
class RepositorioReservasFalsoTest {

    /** Reloj fijo: jueves 15 de octubre de 2026 a las 09:55. */
    private val reloj = object : Reloj {
        override fun ahora(): LocalDateTime = LocalDateTime.of(2026, 10, 15, 9, 55)
    }
    private val relator = DatosFalsos.usuarios.first { it.id == 3 }
    private val bloques = HorarioGeneral().bloques()

    /** Bloque que empieza a [hora]:[minuto] dentro del horario general. */
    private fun bloque(hora: Int, minuto: Int): Bloque = bloques.first { it.inicio == LocalTime.of(hora, minuto) }

    @Test
    fun horarioGeneral_tiene13BloquesConPausa() {
        assertEquals(13, bloques.size)
        assertEquals("08:00–09:00", bloques.first().texto)
        assertEquals("09:05–10:00", bloques[1].texto)
        assertEquals("20:05–21:00", bloques.last().texto)
    }

    @Test
    fun crear_bloquesLibresYSeguidos_creaReserva() = runTest {
        val repositorio = RepositorioReservasFalso(reloj)
        val manana = reloj.hoy().plusDays(1)

        val resultado = repositorio.crear("S-102", relator, manana, listOf(bloque(13, 5), bloque(14, 5)), "Clase")

        assertTrue(resultado is ResultadoReserva.Exito)
        val reserva = (resultado as ResultadoReserva.Exito).reserva
        assertEquals("13:05–15:00", reserva.horario)
        assertEquals(EstadoReserva.RESERVADA, reserva.estado)
        assertEquals(9, repositorio.reservas.value.size)
    }

    @Test
    fun crear_bloqueOcupado_daError() = runTest {
        val repositorio = RepositorioReservasFalso(reloj)
        // LAB-01 ya está reservada hoy de 10:05 a 12:00 (reserva 1).
        val resultado = repositorio.crear("LAB-01", relator, reloj.hoy(), listOf(bloque(11, 5)), "")

        assertEquals(ResultadoReserva.Error("Uno de los bloques ya está ocupado"), resultado)
    }

    @Test
    fun crear_bloquesNoSeguidos_daError() = runTest {
        val repositorio = RepositorioReservasFalso(reloj)
        val resultado = repositorio.crear("S-102", relator, reloj.hoy().plusDays(1), listOf(bloque(13, 5), bloque(15, 5)), "")

        assertEquals(ResultadoReserva.Error("Los bloques deben ser seguidos"), resultado)
    }

    @Test
    fun crear_masDeCuatroBloques_daError() = runTest {
        val repositorio = RepositorioReservasFalso(reloj)
        val cinco = bloques.subList(5, 10)
        val resultado = repositorio.crear("S-102", relator, reloj.hoy().plusDays(1), cinco, "")

        assertTrue(resultado is ResultadoReserva.Error)
    }

    @Test
    fun reservasDeEjemplo_conRelojMaqueta_muestranCadaEstado() {
        val repositorio = RepositorioReservasFalso(reloj)
        val ahora = reloj.ahora()

        assertTrue(repositorio.buscar(1)!!.mostrarRecordatorio(ahora))
        assertTrue(repositorio.buscar(1)!!.checkInHabilitado(ahora))
        assertTrue(repositorio.buscar(3)!!.sinCheckIn(ahora))
        assertFalse(repositorio.buscar(2)!!.sinCheckIn(ahora))
    }

    @Test
    fun cancelar_cambiaElEstado() = runTest {
        val repositorio = RepositorioReservasFalso(reloj)
        repositorio.cancelar(5)

        assertEquals(EstadoReserva.CANCELADA, repositorio.buscar(5)!!.estado)
    }
}
