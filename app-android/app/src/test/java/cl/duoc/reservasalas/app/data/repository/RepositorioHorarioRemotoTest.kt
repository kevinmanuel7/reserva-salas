package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.remote.ReservasApi
import cl.duoc.reservasalas.app.data.remote.dto.HorarioDto
import cl.duoc.reservasalas.app.errorHttp
import cl.duoc.reservasalas.app.jsonError
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Pruebas de RepositorioHorarioRemoto sin servidor: ReservasApi (Retrofit) se simula con MockK.
 * Al crearse, el repositorio pide el horario en otro hilo. En las pruebas de "guardar", esa
 * primera carga falla a propósito (sin conexión) para que no pise el horario recién guardado.
 */
class RepositorioHorarioRemotoTest {

    private val api = mockk<ReservasApi>()
    private val json = Json { ignoreUnknownKeys = true }

    private val horarioServidor = HorarioDto("09:00", "20:00", 10, listOf("MONDAY", "WEDNESDAY"))
    private val horarioEsperado = HorarioGeneral(
        apertura = LocalTime.of(9, 0),
        cierre = LocalTime.of(20, 0),
        pausaMinutos = 10,
        dias = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
    )

    /** Repositorio cuya carga inicial falla: queda con el horario predeterminado. */
    private suspend fun repositorioSinCargaInicial(): RepositorioHorarioRemoto {
        coEvery { api.obtenerHorario() } throws IOException("sin red")
        return RepositorioHorarioRemoto(api, json).also { it.refrescar() }
    }

    @Test
    fun alCrearse_cargaYConvierteElHorarioDelServidor() = runBlocking {
        coEvery { api.obtenerHorario() } returns horarioServidor

        val repositorio = RepositorioHorarioRemoto(api, json)

        assertEquals(horarioEsperado, withTimeout(5_000) { repositorio.horario.first { it.pausaMinutos == 10 } })
    }

    @Test
    fun sinConexion_quedaElHorarioPredeterminado() = runBlocking {
        val repositorio = repositorioSinCargaInicial()

        assertEquals(HorarioGeneral(), repositorio.horario.value)
    }

    @Test
    fun guardar_enviaElHorarioYGuardaLoQueRespondeElServidor() = runBlocking {
        val repositorio = repositorioSinCargaInicial()
        val enviado = slot<HorarioDto>()
        coEvery { api.actualizarHorario(capture(enviado)) } returns horarioServidor

        repositorio.guardar(horarioEsperado)

        assertEquals(horarioServidor, enviado.captured)
        assertEquals(horarioEsperado, repositorio.horario.value)
    }

    private suspend fun mensajeAlGuardarCon(error: Exception): String? {
        val repositorio = repositorioSinCargaInicial()
        coEvery { api.actualizarHorario(any()) } throws error
        val mensaje = runCatching { repositorio.guardar(horarioEsperado) }.exceptionOrNull()?.message
        // Si falla, el horario en memoria no cambia.
        assertEquals(HorarioGeneral(), repositorio.horario.value)
        return mensaje
    }

    @Test
    fun guardar_400_muestraElMensajeDelServidor() = runBlocking {
        val error = errorHttp(400, jsonError("HORARIO_INVALIDO", "El cierre debe ser después de la apertura"))

        assertEquals("El cierre debe ser después de la apertura", mensajeAlGuardarCon(error))
    }

    @Test
    fun guardar_sinConexion_muestraSinConexion() = runBlocking {
        assertEquals("Sin conexión", mensajeAlGuardarCon(IOException("sin red")))
    }

    @Test
    fun guardar_errorSinMensajeUtil_muestraMensajeGenerico() = runBlocking {
        val generico = "No se pudo guardar el horario. Inténtalo nuevamente."

        assertEquals(generico, mensajeAlGuardarCon(errorHttp(403, "")))
        assertEquals(generico, mensajeAlGuardarCon(RuntimeException("detalle técnico")))
    }
}
