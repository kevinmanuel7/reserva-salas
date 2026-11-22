package cl.duoc.reservasalas.app.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import cl.duoc.reservasalas.app.SESION_PRUEBA
import cl.duoc.reservasalas.app.cerrarYEsperar
import cl.duoc.reservasalas.app.crear
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioHorario
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import cl.duoc.reservasalas.app.relojFijo
import cl.duoc.reservasalas.app.reservaDePrueba
import cl.duoc.reservasalas.app.ui.pantallas.agenda.AgendaViewModel
import cl.duoc.reservasalas.app.ui.pantallas.detallesala.DetalleSalaViewModel
import cl.duoc.reservasalas.app.ui.pantallas.reservaconfirmada.ReservaConfirmadaViewModel
import cl.duoc.reservasalas.app.ui.pantallas.salas.SalasViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Pruebas de los ViewModel que casi no tienen lógica propia (Agenda, Salas, Detalle de sala y
 * Reserva confirmada): revisan que pidan los datos al servidor al abrirse y que entreguen a la
 * pantalla lo que tienen los repositorios. Los repositorios y la sesión se simulan con MockK.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsSimplesTest {

    private val repositorioReservas = mockk<RepositorioReservas>()
    private val repositorioSalas = mockk<RepositorioSalas>()
    private val repositorioHorario = mockk<RepositorioHorario>()
    private val sesionDataStore = mockk<SesionDataStore>()

    private val reserva = reservaDePrueba()
    private val sala = Sala("S-101", "Sala 101", 30, "Piso 1", emptyList())
    private val almacenViewModels = ViewModelStore()
    private val creados = mutableListOf<ViewModel>()

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repositorioReservas.reservas } returns MutableStateFlow(listOf(reserva))
        every { repositorioSalas.salas } returns MutableStateFlow(listOf(sala))
        every { repositorioHorario.horario } returns MutableStateFlow(HorarioGeneral())
        every { sesionDataStore.sesion } returns MutableStateFlow(SESION_PRUEBA)
        coEvery { repositorioReservas.refrescar() } just Runs
        coEvery { repositorioSalas.refrescar() } just Runs
        coEvery { repositorioHorario.refrescar() } just Runs
    }

    @After
    fun limpiar() {
        almacenViewModels.cerrarYEsperar(*creados.toTypedArray())
        Dispatchers.resetMain()
    }

    @Test
    fun agenda_pideLasReservasYSabeQuienEstaConectado() {
        val viewModel = almacenViewModels.crear {
            AgendaViewModel(repositorioReservas, repositorioSalas, sesionDataStore)
        }.also { creados += it }

        coVerify { repositorioReservas.refrescar() }
        assertEquals(7, viewModel.idUsuario.value)
        assertEquals(listOf(reserva), viewModel.reservas.value)
        assertEquals(listOf(sala), viewModel.salas.value)
    }

    @Test
    fun salas_pideLaListaAlAbrir() {
        val viewModel = almacenViewModels.crear { SalasViewModel(repositorioSalas) }.also { creados += it }

        coVerify { repositorioSalas.refrescar() }
        assertEquals(listOf(sala), viewModel.salas.value)
    }

    @Test
    fun detalleSala_pideElHorarioYBuscaLaSalaPorCodigo() {
        val reloj = relojFijo(LocalTime.of(9, 0))
        val viewModel = almacenViewModels.crear {
            DetalleSalaViewModel(repositorioSalas, repositorioReservas, repositorioHorario, reloj)
        }.also { creados += it }

        coVerify { repositorioHorario.refrescar() }
        assertEquals(sala, viewModel.buscarSala("S-101"))
        assertNull(viewModel.buscarSala("NO-EXISTE"))
        assertEquals(HorarioGeneral(), viewModel.horario.value)
        assertEquals(listOf(reserva), viewModel.reservas.value)
        assertEquals(reloj, viewModel.reloj)
    }

    @Test
    fun reservaConfirmada_entregaReservasYSalas() {
        val viewModel = almacenViewModels.crear {
            ReservaConfirmadaViewModel(repositorioReservas, repositorioSalas)
        }.also { creados += it }

        assertEquals(listOf(reserva), viewModel.reservas.value)
        assertEquals(listOf(sala), viewModel.salas.value)
    }
}
