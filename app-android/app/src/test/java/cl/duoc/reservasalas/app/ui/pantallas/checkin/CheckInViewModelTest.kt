package cl.duoc.reservasalas.app.ui.pantallas.checkin

import android.content.Context
import androidx.lifecycle.ViewModelStore
import cl.duoc.reservasalas.app.cerrarYEsperar
import cl.duoc.reservasalas.app.crear
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

/**
 * Pruebas del check-in con el código de la sala (escrito a mano o leído del QR).
 * - RepositorioReservas se simula con MockK: así se decide qué responde el servidor (éxito o error).
 * - El reloj está fijo a las 10:00 y la reserva empieza a las 10:05 (check-in habilitado).
 * - Context es un mock vacío: solo lo usan la cámara y la lectura del QR (ML Kit), que necesitan
 *   un teléfono y no se prueban aquí.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModelTest {

    private val repositorio = mockk<RepositorioReservas>()
    private val reserva = reservaDePrueba(id = 1, codigoSala = "S-101", inicio = LocalTime.of(10, 5))
    private val almacenViewModels = ViewModelStore()
    private lateinit var viewModel: CheckInViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repositorio.buscar(1) } returns reserva
        every { repositorio.buscar(99) } returns null
        coEvery { repositorio.registrarCheckIn(any(), any()) } just Runs
    }

    @After
    fun limpiar() {
        almacenViewModels.cerrarYEsperar(viewModel)
        Dispatchers.resetMain()
    }

    private fun crearViewModel(idReserva: Int = 1, hora: LocalTime = LocalTime.of(10, 0)) {
        viewModel = almacenViewModels.crear {
            CheckInViewModel(idReserva, repositorio, relojFijo(hora), mockk<Context>(relaxed = true))
        }
    }

    private fun ingresarCodigo(texto: String) {
        viewModel.onCodigoManualCambio(texto)
        viewModel.confirmarCodigoManual()
    }

    private val estado get() = viewModel.uiState.value.estado

    @Test
    fun alAbrir_muestraLaReservaYEspera() {
        crearViewModel()

        assertEquals(reserva, viewModel.uiState.value.reserva)
        assertEquals(EstadoCheckIn.Esperando, estado)
    }

    @Test
    fun reservaInexistente_muestraError() {
        crearViewModel(idReserva = 99)

        ingresarCodigo("SALA:S-101")

        assertEquals(EstadoCheckIn.Error("No se encontró la reserva."), estado)
    }

    @Test
    fun qrDeOtraSala_muestraErrorYNoRegistraNada() {
        crearViewModel()

        ingresarCodigo("SALA:LAB-01")

        assertEquals(EstadoCheckIn.Error("Este código es de la sala LAB-01. Tu reserva es en S-101."), estado)
        coVerify(exactly = 0) { repositorio.registrarCheckIn(any(), any()) }
    }

    @Test
    fun qrCorrectoFueraDeHorario_muestraLaVentanaPermitida() {
        crearViewModel(hora = LocalTime.of(9, 30))

        ingresarCodigo("SALA:S-101")

        assertEquals(
            EstadoCheckIn.Error("El check-in se puede hacer desde 10 minutos antes hasta 15 minutos después del inicio."),
            estado
        )
        coVerify(exactly = 0) { repositorio.registrarCheckIn(any(), any()) }
    }

    @Test
    fun qrCorrecto_registraYMuestraLaHoraQueGuardoElServidor() {
        // Después del check-in, el repositorio ya tiene la reserva con la hora del servidor.
        every { repositorio.buscar(1) } returnsMany listOf(
            reserva,
            reserva.copy(estado = EstadoReserva.EN_USO, horaCheckIn = LocalTime.of(10, 1))
        )
        crearViewModel()

        ingresarCodigo("SALA:S-101")

        coVerify { repositorio.registrarCheckIn(1, LocalTime.of(10, 0)) }
        assertEquals(EstadoCheckIn.Exito(LocalTime.of(10, 1)), estado)
    }

    @Test
    fun codigoEscritoEnMinusculasYConEspacios_seAcepta() {
        crearViewModel()

        ingresarCodigo("  s-101 ")

        // El servidor no devolvió hora: se muestra la hora del teléfono.
        assertEquals(EstadoCheckIn.Exito(LocalTime.of(10, 0)), estado)
    }

    @Test
    fun prefijoDelQrEnMinusculas_seAcepta() {
        crearViewModel()

        ingresarCodigo("sala:s-101")

        coVerify { repositorio.registrarCheckIn(1, LocalTime.of(10, 0)) }
        assertEquals(EstadoCheckIn.Exito(LocalTime.of(10, 0)), estado)
    }

    @Test
    fun errorDelServidor_muestraSuMensaje() {
        coEvery { repositorio.registrarCheckIn(any(), any()) } throws IllegalStateException("Sin conexión")
        crearViewModel()

        ingresarCodigo("SALA:S-101")

        assertEquals(EstadoCheckIn.Error("Sin conexión"), estado)
    }

    @Test
    fun errorSinMensaje_muestraUnMensajeGenerico() {
        coEvery { repositorio.registrarCheckIn(any(), any()) } throws RuntimeException()
        crearViewModel()

        ingresarCodigo("SALA:S-101")

        assertEquals(EstadoCheckIn.Error("No se pudo registrar el check-in. Intenta de nuevo."), estado)
    }

    @Test
    fun escribirDespuesDeUnError_vuelveAEsperar() {
        crearViewModel()
        ingresarCodigo("SALA:LAB-01")

        viewModel.onCodigoManualCambio("S-1")

        assertEquals(EstadoCheckIn.Esperando, estado)
        assertEquals("S-1", viewModel.uiState.value.codigoManual)
    }

    @Test
    fun mostrarError_dejaElMensajeEnPantalla() {
        crearViewModel()

        viewModel.mostrarError("Permiso de cámara rechazado")

        assertEquals(EstadoCheckIn.Error("Permiso de cámara rechazado"), estado)
    }
}
