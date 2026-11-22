package cl.duoc.reservasalas.app.ui.pantallas.iniciorelator

import androidx.lifecycle.ViewModelStore
import cl.duoc.reservasalas.app.HOY_PRUEBA
import cl.duoc.reservasalas.app.SESION_PRUEBA
import cl.duoc.reservasalas.app.cerrarYEsperar
import cl.duoc.reservasalas.app.crear
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.Feriado
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.repository.RepositorioFeriados
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.relojFijo
import cl.duoc.reservasalas.app.reservaDePrueba
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Pruebas de Inicio del relator: qué reserva aparece como "próxima", cuáles en "esta semana",
 * cuándo sale el recordatorio y el próximo feriado.
 * - La sesión, las reservas y los feriados se simulan con MockK (sin DataStore ni servidor).
 * - El reloj está fijo el lunes 23/11/2026; cada prueba elige la hora.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InicioRelatorViewModelTest {

    private val sesionDataStore = mockk<SesionDataStore>()
    private val repositorioReservas = mockk<RepositorioReservas>()
    private val repositorioFeriados = mockk<RepositorioFeriados>()
    private val almacenViewModels = ViewModelStore()
    private lateinit var viewModel: InicioRelatorViewModel

    private val inmaculada = Feriado(LocalDate.of(2026, 12, 8), "Inmaculada Concepción")

    private fun dia(n: Long) = HOY_PRUEBA.plusDays(n)

    // Reservas de la relatora (id 7), salvo donde se indica.
    private val reservas = listOf(
        reservaDePrueba(id = 1, inicio = LocalTime.of(10, 5)),
        reservaDePrueba(id = 2, fecha = dia(1)),
        reservaDePrueba(id = 3, fecha = dia(3)),
        reservaDePrueba(id = 4, fecha = dia(5)),
        reservaDePrueba(id = 5, fecha = dia(10)),
        reservaDePrueba(id = 6, inicio = LocalTime.of(11, 5), estado = EstadoReserva.CANCELADA),
        reservaDePrueba(id = 7, inicio = LocalTime.of(10, 0), idUsuario = 99),
        // Ya empezó (son las 09:50 en la mayoría de las pruebas).
        reservaDePrueba(id = 8, inicio = LocalTime.of(8, 0))
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { repositorioReservas.refrescar() } just Runs
        coEvery { repositorioFeriados.proximoFeriado(HOY_PRUEBA) } returns inmaculada
    }

    @After
    fun limpiar() {
        almacenViewModels.cerrarYEsperar(viewModel)
        Dispatchers.resetMain()
    }

    private fun crearViewModel(
        hora: LocalTime = LocalTime.of(9, 50),
        sesion: Sesion? = SESION_PRUEBA,
        lista: List<Reserva> = reservas
    ) {
        every { sesionDataStore.sesion } returns MutableStateFlow(sesion)
        every { repositorioReservas.reservas } returns MutableStateFlow(lista)
        viewModel = almacenViewModels.crear {
            InicioRelatorViewModel(sesionDataStore, repositorioReservas, relojFijo(hora), repositorioFeriados)
        }
    }

    /** uiState solo calcula mientras alguien lo observa: first() lo observa hasta que termina de cargar. */
    private suspend fun estado() = viewModel.uiState.first { !it.cargando }

    @Test
    fun alAbrir_pideLasReservasAlServidor() = runTest {
        crearViewModel()

        estado()

        coVerify { repositorioReservas.refrescar() }
    }

    @Test
    fun saludaConElPrimerNombreYMuestraLaFechaDeHoy() = runTest {
        crearViewModel()

        val estado = estado()

        assertEquals("Ana", estado.nombre)
        assertEquals(HOY_PRUEBA, estado.fecha)
    }

    @Test
    fun proximaReserva_ignoraCanceladasPasadasYDeOtrosUsuarios() = runTest {
        crearViewModel()

        assertEquals(1, estado().proximaReserva?.id)
    }

    @Test
    fun semana_muestraHasta3ReservasDeLosProximos7Dias() = runTest {
        crearViewModel()

        assertEquals(listOf(1, 2, 3), estado().reservasSemana.map { it.id })
    }

    @Test
    fun recordatorio_apareceEnLos15MinutosPrevios() = runTest {
        crearViewModel(hora = LocalTime.of(9, 50))

        assertEquals(1, estado().recordatorio?.id)
    }

    @Test
    fun recordatorio_noApareceSiFaltaMasDe15Minutos() = runTest {
        crearViewModel(hora = LocalTime.of(9, 0))

        assertNull(estado().recordatorio)
    }

    @Test
    fun muestraElProximoFeriado() = runTest {
        crearViewModel()

        assertEquals(inmaculada, estado().proximoFeriado)
    }

    @Test
    fun sinSesion_usaUnSaludoGenericoYNoMuestraReservas() = runTest {
        crearViewModel(sesion = null)

        val estado = estado()

        assertEquals("Relator", estado.nombre)
        assertNull(estado.proximaReserva)
        assertEquals(emptyList<Int>(), estado.reservasSemana.map { it.id })
    }
}
