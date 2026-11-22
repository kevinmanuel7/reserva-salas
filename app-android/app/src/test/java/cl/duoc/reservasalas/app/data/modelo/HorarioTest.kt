package cl.duoc.reservasalas.app.data.modelo

import cl.duoc.reservasalas.app.HOY_PRUEBA
import cl.duoc.reservasalas.app.reservaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Cómo se arman los bloques de 1 hora (horario general y horario propio de una sala),
 * el estado de cada bloque (libre, ocupado, pasado) y los formatos de fecha y hora.
 * Funciones puras: no se simula nada.
 */
class HorarioTest {

    private fun hora(texto: String) = LocalTime.parse(texto)

    // ---------- Bloques ----------

    @Test
    fun horarioPredeterminado_tiene13BloquesDe8a21ConPausaDe5Minutos() {
        val bloques = HorarioGeneral().bloques()

        assertEquals(13, bloques.size)
        assertEquals(Bloque(hora("08:00"), hora("09:00")), bloques.first())
        assertEquals(Bloque(hora("09:05"), hora("10:00")), bloques[1])
        assertEquals(Bloque(hora("20:05"), hora("21:00")), bloques.last())
    }

    @Test
    fun horarioConfigurado_usaSuAperturaCierreYPausa() {
        val horario = HorarioGeneral(apertura = hora("09:00"), cierre = hora("12:00"), pausaMinutos = 10)

        assertEquals(
            listOf("09:00–10:00", "10:10–11:00", "11:10–12:00"),
            horario.bloques().map { it.texto }
        )
    }

    private val sala = Sala("S-101", "Sala 101", 30, "Piso 1", equipos = emptyList())

    @Test
    fun salaSinHorarioPropio_usaElHorarioGeneral() {
        assertEquals(HorarioGeneral().bloques(), sala.bloques(HorarioGeneral()))
    }

    @Test
    fun salaConHorarioPropio_usaSusHorasYLaPausaGeneral() {
        val conHorario = sala.copy(horarioPropio = HorarioSala(hora("10:00"), hora("13:00")))

        assertEquals(
            listOf("10:00–11:00", "11:05–12:00", "12:05–13:00"),
            conHorario.bloques(HorarioGeneral()).map { it.texto }
        )
    }

    // ---------- Estado de un bloque ----------

    private val bloque = Bloque(hora("10:05"), hora("11:00"))
    private val manana: LocalDate = HOY_PRUEBA.plusDays(1)
    private val ahora: LocalDateTime = LocalDateTime.of(HOY_PRUEBA, hora("09:00"))

    @Test
    fun estadoBloque_conReservaDeEsaSalaYDia_estaOcupado() {
        val reservas = listOf(reservaDePrueba(fecha = manana))

        assertEquals(EstadoBloque.OCUPADO, estadoBloque(bloque, "S-101", manana, reservas, ahora))
    }

    @Test
    fun estadoBloque_reservaDeOtraSala_quedaLibre() {
        val reservas = listOf(reservaDePrueba(codigoSala = "LAB-01", fecha = manana))

        assertEquals(EstadoBloque.LIBRE, estadoBloque(bloque, "S-101", manana, reservas, ahora))
    }

    @Test
    fun estadoBloque_hoyYaEmpezado_estaPasado() {
        val tarde = LocalDateTime.of(HOY_PRUEBA, hora("10:05"))

        assertEquals(EstadoBloque.PASADO, estadoBloque(bloque, "S-101", HOY_PRUEBA, emptyList(), tarde))
    }

    @Test
    fun estadoBloque_ocupadoYPasado_seMuestraOcupado() {
        val tarde = LocalDateTime.of(HOY_PRUEBA, hora("12:00"))
        val reservas = listOf(reservaDePrueba())

        assertEquals(EstadoBloque.OCUPADO, estadoBloque(bloque, "S-101", HOY_PRUEBA, reservas, tarde))
    }

    // ---------- Sala ----------

    @Test
    fun tieneFallas_soloSiAlgunEquipoEstaConFalla() {
        val operativa = sala.copy(equipos = listOf(Equipo("Proyector")))
        val conFalla = sala.copy(equipos = listOf(Equipo("Proyector"), Equipo("Pizarra", EstadoEquipo.CON_FALLA, "Rayada")))

        assertFalse(operativa.tieneFallas)
        assertTrue(conFalla.tieneFallas)
    }

    // ---------- Formatos ----------

    @Test
    fun formatos_horaConDosDigitosYFechaEnEspanol() {
        val jueves = LocalDate.of(2026, 10, 15)

        assertEquals("09:05", formatearHora(hora("09:05")))
        assertEquals("Jueves 15 de octubre", formatearFechaLarga(jueves))
        assertEquals("Jueves 15", formatearFechaCorta(jueves))
    }
}
