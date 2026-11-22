package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.EstadoEquipo
import cl.duoc.reservasalas.app.data.modelo.HorarioSala
import cl.duoc.reservasalas.app.data.remote.SalasApi
import cl.duoc.reservasalas.app.data.remote.dto.ActualizarSalaPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.EquipoDto
import cl.duoc.reservasalas.app.data.remote.dto.SalaDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.LocalTime

/**
 * Pruebas de RepositorioSalasRemoto sin servidor: SalasApi (Retrofit) se simula con MockK.
 * El repositorio carga las salas solo al crearse (en otro hilo), por eso varias pruebas esperan
 * a que la lista deje de estar vacía antes de seguir.
 */
class RepositorioSalasRemotoTest {

    private val api = mockk<SalasApi>()

    private val salaDto = SalaDto(
        id = 5, codigo = "S-101", nombre = "Sala 101", tipo = "SALA", capacidad = 30,
        tieneProyector = true, tieneComputadores = false, tieneClimatizacion = true, activa = true,
        ubicacion = "Piso 1", horaApertura = "10:00", horaCierre = "18:00",
        equipos = listOf(EquipoDto("Proyector", "CON_FALLA", "Parpadea"))
    )

    /** Crea el repositorio y espera (máx. 5 s) a que termine la carga inicial. */
    private suspend fun repositorioCargado(): RepositorioSalasRemoto {
        coEvery { api.listar() } returns listOf(salaDto)
        val repositorio = RepositorioSalasRemoto(api)
        withTimeout(5_000) { repositorio.salas.first { it.isNotEmpty() } }
        return repositorio
    }

    @Test
    fun alCrearse_cargaYConvierteLasSalasDelServidor() = runBlocking {
        val repositorio = repositorioCargado()

        val sala = repositorio.buscar("S-101")!!
        assertEquals("Sala 101", sala.nombre)
        assertEquals(5L, sala.id)
        assertEquals(HorarioSala(LocalTime.of(10, 0), LocalTime.of(18, 0)), sala.horarioPropio)
        assertEquals(EstadoEquipo.CON_FALLA, sala.equipos.first().estado)
        assertNull(repositorio.buscar("NO-EXISTE"))
    }

    @Test
    fun sinConexion_conservaLaUltimaLista() = runBlocking {
        val repositorio = repositorioCargado()
        coEvery { api.listar() } throws IOException("sin red")

        repositorio.refrescar()

        assertEquals(1, repositorio.salas.value.size)
    }

    @Test
    fun sinConexionDesdeElInicio_quedaVaciaSinCerrarLaApp() = runBlocking {
        coEvery { api.listar() } throws IOException("sin red")
        val repositorio = RepositorioSalasRemoto(api)

        repositorio.refrescar()

        assertTrue(repositorio.salas.value.isEmpty())
        coVerify(atLeast = 1) { api.listar() }
    }

    @Test
    fun guardar_enviaLaPeticionArmadaDesdeElOriginalYActualizaLaLista() = runBlocking {
        val repositorio = repositorioCargado()
        val peticion = slot<ActualizarSalaPeticionDto>()
        coEvery { api.actualizar(5, capture(peticion)) } returns
            salaDto.copy(nombre = "Sala renovada", horaApertura = null, horaCierre = null)

        val editada = repositorio.buscar("S-101")!!.copy(nombre = "Sala renovada", horarioPropio = null)
        repositorio.guardar(editada)

        // Lo que la app no edita (tipo, activa, booleanos) se copia de la sala original.
        assertEquals("SALA", peticion.captured.tipo)
        assertEquals(true, peticion.captured.tieneProyector)
        assertEquals(true, peticion.captured.usaHorarioGeneral)
        val guardada = repositorio.buscar("S-101")!!
        assertEquals("Sala renovada", guardada.nombre)
        assertNull(guardada.horarioPropio)
    }

    @Test
    fun guardar_salaQueNoEstaEnElServidor_lanzaError() = runBlocking {
        val repositorio = repositorioCargado()
        val desconocida = repositorio.buscar("S-101")!!.copy(codigo = "X-1", id = 99)

        val error = runCatching { repositorio.guardar(desconocida) }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertEquals("La sala X-1 no existe en el servidor", error?.message)
    }
}
