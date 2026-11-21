package cl.duoc.reservasalas.app.ui.pantallas.reservar

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.falso.RepositorioHorarioFalso
import cl.duoc.reservasalas.app.data.falso.RepositorioSalasFalso
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.Feriado
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.remote.FeriadosApi
import cl.duoc.reservasalas.app.data.remote.dto.FeriadoDto
import cl.duoc.reservasalas.app.data.repository.RepositorioFeriadosRemoto
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.ResultadoReserva
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Pruebas de la regla de feriados en ReservarViewModel. El repositorio de feriados es el
 * real, con una FeriadosApi falsa (sin internet). El de reservas es falso y solo anota si
 * se le pidió crear una reserva.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReservarViewModelTest {

    private class FeriadosApiFalsa : FeriadosApi {
        override suspend fun feriados(anio: Int): List<FeriadoDto> = listOf(
            FeriadoDto("2026-12-25", "Navidad", global = true)
        )
    }

    /** Solo importa crear(): anota la llamada y responde un error cualquiera. */
    private class RepositorioReservasQueAnota : RepositorioReservas {
        var llamadasCrear = 0
        override val reservas: StateFlow<List<Reserva>> = MutableStateFlow(emptyList())
        override fun buscar(id: Int): Reserva? = null
        override suspend fun crear(
            codigoSala: String,
            usuario: Usuario,
            fecha: LocalDate,
            bloques: List<Bloque>,
            motivo: String
        ): ResultadoReserva {
            llamadasCrear++
            return ResultadoReserva.Error(RESPUESTA_REPOSITORIO)
        }
        override suspend fun cancelar(id: Int) {}
        override suspend fun registrarCheckIn(id: Int, hora: LocalTime) {}
        override suspend fun marcarVerificada(id: Int) {}
        override suspend fun marcarNoPresentado(id: Int) {}
        override suspend fun refrescar() {}
    }

    // Miércoles 23/12/2026: Navidad cae el viernes 25 y el lunes 28 es un día normal.
    private val reloj = object : Reloj {
        override fun ahora(): LocalDateTime = LocalDateTime.of(2026, 12, 23, 9, 0)
    }
    private val navidad = LocalDate.of(2026, 12, 25)
    private val diaNormal = LocalDate.of(2026, 12, 28)
    private val bloque = Bloque(LocalTime.of(10, 5), LocalTime.of(11, 0))

    @get:Rule
    val carpetaTemporal = TemporaryFolder()

    private val scopeDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val almacenViewModels = ViewModelStore()
    private lateinit var reservas: RepositorioReservasQueAnota
    private lateinit var viewModel: ReservarViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        reservas = RepositorioReservasQueAnota()
        val sesionDataStore = SesionDataStore(
            PreferenceDataStoreFactory.create(scope = scopeDataStore) {
                File(carpetaTemporal.root, "sesion.preferences_pb")
            }
        )
        // crearReserva exige sesión iniciada.
        runBlocking {
            sesionDataStore.guardarSesion(
                Sesion("token-de-prueba", Usuario(3, "Relator Ficticio Uno", "relator1@duocuc.test", Rol.RELATOR))
            )
        }
        val fabrica = viewModelFactory {
            initializer {
                ReservarViewModel(
                    repositorioSalas = RepositorioSalasFalso(),
                    repositorioHorario = RepositorioHorarioFalso(),
                    repositorioReservas = reservas,
                    sesionDataStore = sesionDataStore,
                    reloj = reloj,
                    repositorioFeriados = RepositorioFeriadosRemoto(FeriadosApiFalsa())
                )
            }
        }
        viewModel = ViewModelProvider.create(almacenViewModels, fabrica)[ReservarViewModel::class]
    }

    @After
    fun limpiar() {
        almacenViewModels.clear()
        scopeDataStore.cancel()
        Dispatchers.resetMain()
    }

    /** crearReserva avisa con un callback; esto espera esa respuesta (máximo 5 segundos). */
    private fun crearEn(fecha: LocalDate): ResultadoReserva = runBlocking {
        val respuesta = CompletableDeferred<ResultadoReserva>()
        viewModel.crearReserva("LAB-01", fecha, listOf(bloque), "Taller") { respuesta.complete(it) }
        withTimeout(5_000) { respuesta.await() }
    }

    @Test
    fun fechaFeriado_muestraAvisoYNoPermiteReservar() {
        viewModel.seleccionarFecha(navidad)

        assertEquals(Feriado(navidad, "Navidad"), viewModel.feriado.value)

        val resultado = crearEn(navidad)
        assertEquals(
            ResultadoReserva.Error("Feriado: Navidad. La sede no funciona este día."),
            resultado
        )
        assertEquals(0, reservas.llamadasCrear)
    }

    @Test
    fun fechaNormal_noMuestraAvisoYPermiteReservar() {
        viewModel.seleccionarFecha(diaNormal)

        assertNull(viewModel.feriado.value)

        val resultado = crearEn(diaNormal)
        // Llegó al repositorio de reservas: el feriado no la bloqueó.
        assertEquals(ResultadoReserva.Error(RESPUESTA_REPOSITORIO), resultado)
        assertEquals(1, reservas.llamadasCrear)
    }

    private companion object {
        const val RESPUESTA_REPOSITORIO = "respuesta del repositorio de reservas"
    }
}
