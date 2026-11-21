package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.Feriado
import cl.duoc.reservasalas.app.data.remote.FeriadosApi
import cl.duoc.reservasalas.app.data.remote.dto.FeriadoDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/**
 * Pruebas de RepositorioFeriadosRemoto sin internet: la API es una clase falsa que responde
 * los feriados que cada prueba le indica, con la misma forma que el JSON real de Nager.Date.
 */
class RepositorioFeriadosTest {

    /** Reemplaza a Retrofit: responde [porAnio] o lanza [error], y cuenta las llamadas. */
    private class FeriadosApiFalsa : FeriadosApi {
        val porAnio = mutableMapOf<Int, List<FeriadoDto>>()
        var error: Exception? = null
        val aniosPedidos = mutableListOf<Int>()

        override suspend fun feriados(anio: Int): List<FeriadoDto> {
            aniosPedidos += anio
            error?.let { throw it }
            return porAnio[anio].orEmpty()
        }
    }

    private val api = FeriadosApiFalsa().apply {
        porAnio[2026] = listOf(
            FeriadoDto("2026-01-01", "Año Nuevo", global = true),
            // Regional de otra región (Arica): no aplica a Viña del Mar.
            FeriadoDto("2026-06-07", "Asalto y Toma del Morro de Arica", global = false, counties = listOf("CL-AP")),
            // Regional que sí incluye la Región de Valparaíso.
            FeriadoDto("2026-06-29", "Feriado regional de prueba", global = false, counties = listOf("CL-VS", "CL-RM")),
            FeriadoDto("2026-12-25", "Navidad", global = true)
        )
        porAnio[2027] = listOf(
            FeriadoDto("2027-01-01", "Año Nuevo", global = true)
        )
    }
    private val repositorio = RepositorioFeriadosRemoto(api)

    // ---------- Filtro de región ----------

    @Test
    fun feriadoNacional_seConserva() = runBlocking {
        assertEquals(
            Feriado(LocalDate.of(2026, 12, 25), "Navidad"),
            repositorio.feriadoEn(LocalDate.of(2026, 12, 25))
        )
    }

    @Test
    fun feriadoRegionalDeOtraRegion_seDescarta() = runBlocking {
        assertNull(repositorio.feriadoEn(LocalDate.of(2026, 6, 7)))
    }

    @Test
    fun feriadoRegionalDeValparaiso_seConserva() = runBlocking {
        assertEquals(
            "Feriado regional de prueba",
            repositorio.feriadoEn(LocalDate.of(2026, 6, 29))?.nombre
        )
    }

    // ---------- feriadoEn ----------

    @Test
    fun diaNormal_noEsFeriado() = runBlocking {
        assertNull(repositorio.feriadoEn(LocalDate.of(2026, 12, 23)))
    }

    @Test
    fun mismoAnio_sePideUnaSolaVez() = runBlocking {
        repositorio.feriadoEn(LocalDate.of(2026, 12, 25))
        repositorio.feriadoEn(LocalDate.of(2026, 1, 1))
        repositorio.proximoFeriado(LocalDate.of(2026, 3, 1))

        assertEquals(listOf(2026), api.aniosPedidos)
    }

    // ---------- proximoFeriado ----------

    @Test
    fun proximoFeriado_devuelveElPrimeroDesdeLaFecha() = runBlocking {
        assertEquals(
            Feriado(LocalDate.of(2026, 12, 25), "Navidad"),
            repositorio.proximoFeriado(LocalDate.of(2026, 7, 1))
        )
    }

    @Test
    fun proximoFeriado_incluyeElMismoDia() = runBlocking {
        assertEquals("Navidad", repositorio.proximoFeriado(LocalDate.of(2026, 12, 25))?.nombre)
    }

    @Test
    fun proximoFeriadoDesde30DeDiciembre_cruzaDeAnio() = runBlocking {
        assertEquals(
            Feriado(LocalDate.of(2027, 1, 1), "Año Nuevo"),
            repositorio.proximoFeriado(LocalDate.of(2026, 12, 30))
        )
        assertEquals(listOf(2026, 2027), api.aniosPedidos)
    }

    // ---------- API que falla ----------

    @Test
    fun apiQueFalla_devuelveNullSinLanzarExcepcion() = runBlocking {
        api.error = IOException("Sin conexión")

        assertNull(repositorio.feriadoEn(LocalDate.of(2026, 12, 25)))
        assertNull(repositorio.proximoFeriado(LocalDate.of(2026, 12, 1)))
    }

    @Test
    fun apiQueFalla_noGuardaNadaYReintentaDespues() = runBlocking {
        api.error = IOException("Sin conexión")
        assertNull(repositorio.feriadoEn(LocalDate.of(2026, 12, 25)))

        // Vuelve la conexión: la siguiente consulta pide de nuevo y ahora sí encuentra el feriado.
        api.error = null
        assertEquals("Navidad", repositorio.feriadoEn(LocalDate.of(2026, 12, 25))?.nombre)
    }
}
