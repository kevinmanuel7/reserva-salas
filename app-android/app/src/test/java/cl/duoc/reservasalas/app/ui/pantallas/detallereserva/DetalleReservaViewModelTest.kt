package cl.duoc.reservasalas.app.ui.pantallas.detallereserva

import androidx.lifecycle.ViewModelStore
import cl.duoc.reservasalas.app.cerrarYEsperar
import cl.duoc.reservasalas.app.crear
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import cl.duoc.reservasalas.app.relojFijo
import cl.duoc.reservasalas.app.reservaDePrueba
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Pruebas de Detalle de reserva: botones habilitados según la hora y el flujo de cancelar.
 * - Los repositorios se simulan con MockK; cancelar() responde bien, falla o queda "en espera"
 *   según la prueba.
 * - El reloj está fijo a las 09:58 y la reserva empieza a las 10:05.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleReservaViewModelTest {

    private val repositorioReservas = mockk<RepositorioReservas>()
    private val repositorioSalas = mockk<RepositorioSalas>()
    private val reserva = reservaDePrueba(id = 1, inicio = LocalTime.of(10, 5))
    private val sala = Sala("S-101", "Sala 101", 30, "Piso 1", emptyList())
    private val almacenViewModels = ViewModelStore()
    private lateinit var viewModel: DetalleReservaViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repositorioReservas.reservas } returns MutableStateFlow(listOf(reserva))
        every { repositorioSalas.salas } returns MutableStateFlow(listOf(sala))
        coEvery { repositorioReservas.refrescar() } just Runs
        viewModel = almacenViewModels.crear {
            DetalleReservaViewModel(repositorioReservas, repositorioSalas, relojFijo(LocalTime.of(9, 58)))
        }
    }

    @After
    fun limpiar() {
        almacenViewModels.cerrarYEsperar(viewModel)
        Dispatchers.resetMain()
    }

    @Test
    fun alAbrir_pideLasReservasYExponeReservasYSalas() {
        coVerify { repositorioReservas.refrescar() }
        assertEquals(listOf(reserva), viewModel.reservas.value)
        assertEquals(listOf(sala), viewModel.salas.value)
    }

    @Test
    fun aLas0958_sePuedeCancelarYHacerCheckIn() {
        assertTrue(viewModel.sePuedeCancelar(reserva))
        assertTrue(viewModel.checkInHabilitado(reserva))
    }

    @Test
    fun cancelarConExito_avisaALaPantallaSinError() {
        coEvery { repositorioReservas.cancelar(1) } just Runs
        var avisada = false

        viewModel.cancelar(1) { avisada = true }

        assertTrue(avisada)
        assertFalse(viewModel.cancelando.value)
        assertNull(viewModel.errorCancelacion.value)
    }

    @Test
    fun cancelarConError_muestraElMensajeYNoAvisa() {
        coEvery { repositorioReservas.cancelar(1) } throws IllegalStateException("Sin conexión")
        var avisada = false

        viewModel.cancelar(1) { avisada = true }

        assertFalse(avisada)
        assertEquals("Sin conexión", viewModel.errorCancelacion.value)
        assertFalse(viewModel.cancelando.value)

        viewModel.limpiarErrorCancelacion()
        assertNull(viewModel.errorCancelacion.value)
    }

    @Test
    fun cancelarConErrorSinMensaje_muestraMensajeGenerico() {
        coEvery { repositorioReservas.cancelar(1) } throws RuntimeException()

        viewModel.cancelar(1) {}

        assertEquals("No se pudo cancelar la reserva", viewModel.errorCancelacion.value)
    }

    @Test
    fun dobleToqueMientrasCancela_soloEnviaUnaCancelacion() {
        // El servidor "se demora": la cancelación queda esperando hasta que se completa respuesta.
        val respuesta = CompletableDeferred<Unit>()
        coEvery { repositorioReservas.cancelar(1) } coAnswers { respuesta.await() }

        viewModel.cancelar(1) {}
        assertTrue(viewModel.cancelando.value)
        viewModel.cancelar(1) {}

        respuesta.complete(Unit)
        coVerify(exactly = 1) { repositorioReservas.cancelar(1) }
        assertFalse(viewModel.cancelando.value)
    }
}
