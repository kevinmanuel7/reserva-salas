package cl.duoc.reservasalas.app.data.modelo

import cl.duoc.reservasalas.app.HOY_PRUEBA
import cl.duoc.reservasalas.app.reservaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Reglas de negocio de una reserva (check-in, cancelar, recordatorio y ocupación de bloques).
 * No se simula nada: son funciones puras que reciben la hora como parámetro, así que cada
 * prueba elige el momento exacto, incluidos los bordes (justo en el límite y un minuto después).
 */
class ReservaTest {

    // Reserva de hoy 23/11/2026 de 10:05 a 12:00 (dos bloques).
    private val reserva = reservaDePrueba(inicio = LocalTime.of(10, 5), fin = LocalTime.of(12, 0))

    private fun a(hora: Int, minuto: Int): LocalDateTime = LocalDateTime.of(HOY_PRUEBA, LocalTime.of(hora, minuto))

    // ---------- Check-in: desde 10 minutos antes hasta 15 minutos después ----------

    @Test
    fun checkIn_justo10MinutosAntes_estaHabilitado() {
        assertTrue(reserva.checkInHabilitado(a(9, 55)))
    }

    @Test
    fun checkIn_11MinutosAntes_noEstaHabilitado() {
        assertFalse(reserva.checkInHabilitado(a(9, 54)))
    }

    @Test
    fun checkIn_justo15MinutosDespues_estaHabilitado() {
        assertTrue(reserva.checkInHabilitado(a(10, 20)))
    }

    @Test
    fun checkIn_16MinutosDespues_noEstaHabilitado() {
        assertFalse(reserva.checkInHabilitado(a(10, 21)))
    }

    @Test
    fun checkIn_reservaQueYaEstaEnUso_noEstaHabilitado() {
        assertFalse(reserva.copy(estado = EstadoReserva.EN_USO).checkInHabilitado(a(10, 5)))
    }

    // ---------- Sin check-in (el coordinador puede marcar "No presentado") ----------

    @Test
    fun sinCheckIn_pasados15MinutosSigueReservada_esVerdadero() {
        assertTrue(reserva.sinCheckIn(a(10, 21)))
    }

    @Test
    fun sinCheckIn_dentroDeLos15Minutos_esFalso() {
        assertFalse(reserva.sinCheckIn(a(10, 20)))
    }

    @Test
    fun sinCheckIn_siYaHizoCheckIn_esFalso() {
        assertFalse(reserva.copy(estado = EstadoReserva.EN_USO).sinCheckIn(a(11, 0)))
    }

    // ---------- Cancelar: solo antes de que empiece ----------

    @Test
    fun cancelar_unMinutoAntesDelInicio_sePuede() {
        assertTrue(reserva.sePuedeCancelar(a(10, 4)))
    }

    @Test
    fun cancelar_desdeQueEmpieza_noSePuede() {
        assertFalse(reserva.sePuedeCancelar(a(10, 5)))
    }

    @Test
    fun cancelar_reservaYaCancelada_noSePuede() {
        assertFalse(reserva.copy(estado = EstadoReserva.CANCELADA).sePuedeCancelar(a(9, 0)))
    }

    // ---------- Recordatorio de 15 minutos ----------

    @Test
    fun momentoRecordatorio_es15MinutosAntesDelInicio() {
        assertEquals(a(9, 50), reserva.momentoRecordatorio)
    }

    @Test
    fun mostrarRecordatorio_dentroDeLos15MinutosPrevios_esVerdadero() {
        assertTrue(reserva.mostrarRecordatorio(a(9, 50)))
    }

    @Test
    fun mostrarRecordatorio_16MinutosAntes_esFalso() {
        assertFalse(reserva.mostrarRecordatorio(a(9, 49)))
    }

    @Test
    fun mostrarRecordatorio_cuandoYaEmpezo_esFalso() {
        assertFalse(reserva.mostrarRecordatorio(a(10, 5)))
    }

    @Test
    fun necesitaRecordatorio_antesDelMomentoDelAviso_esVerdadero() {
        assertTrue(reserva.necesitaRecordatorio(a(9, 49)))
    }

    @Test
    fun necesitaRecordatorio_siElAvisoYaLlego_esFalso() {
        assertFalse(reserva.necesitaRecordatorio(a(9, 50)))
    }

    @Test
    fun necesitaRecordatorio_reservaCancelada_esFalso() {
        assertFalse(reserva.copy(estado = EstadoReserva.CANCELADA).necesitaRecordatorio(a(8, 0)))
    }

    // ---------- Ocupación de bloques ----------

    private fun bloque(desde: String, hasta: String) = Bloque(LocalTime.parse(desde), LocalTime.parse(hasta))

    @Test
    fun ocupa_bloqueDentroDeLaReserva() {
        assertTrue(reserva.ocupa(bloque("11:05", "12:00")))
    }

    @Test
    fun ocupa_bloqueJustoDespues_noLoOcupa() {
        assertFalse(reserva.ocupa(bloque("12:05", "13:00")))
    }

    @Test
    fun ocupa_bloqueJustoAntes_noLoOcupa() {
        assertFalse(reserva.ocupa(bloque("09:05", "10:00")))
    }

    @Test
    fun ocupa_reservaCancelada_liberaElBloque() {
        assertFalse(reserva.copy(estado = EstadoReserva.CANCELADA).ocupa(bloque("10:05", "11:00")))
    }

    @Test
    fun ocupa_noPresentado_noLiberaElBloque() {
        // Regla del mandante: si el relator no llega, la sala NO se libera.
        assertTrue(reserva.copy(estado = EstadoReserva.NO_PRESENTADO).ocupa(bloque("10:05", "11:00")))
    }

    @Test
    fun horario_seMuestraDeCorrido() {
        assertEquals("10:05–12:00", reserva.horario)
        assertEquals("En uso", EstadoReserva.EN_USO.texto)
    }
}
