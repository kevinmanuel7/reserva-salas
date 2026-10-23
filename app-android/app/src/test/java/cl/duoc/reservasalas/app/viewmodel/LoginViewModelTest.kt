package cl.duoc.reservasalas.app.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.remote.dto.LoginPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.LoginRespuestaDto
import cl.duoc.reservasalas.app.data.remote.dto.UsuarioDto
import cl.duoc.reservasalas.app.data.repository.AuthRepository
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException

/**
 * Pruebas de LoginViewModel sin teléfono ni backend:
 * - el AuthRepository es el real, pero con una API falsa y un DataStore temporal;
 * - Dispatchers.setMain reemplaza el hilo principal de Android, que no existe en estas pruebas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    /** Reemplaza a Retrofit: cuenta las llamadas, guarda la última petición y responde lo indicado. */
    private class UsuariosApiFalsa : UsuariosApi {
        var respuesta: LoginRespuestaDto? = null
        var error: Exception? = null
        var llamadas = 0
        var ultimaPeticion: LoginPeticionDto? = null

        override suspend fun login(peticion: LoginPeticionDto): LoginRespuestaDto {
            llamadas++
            ultimaPeticion = peticion
            error?.let { throw it }
            return respuesta!!
        }
    }

    @get:Rule
    val carpetaTemporal = TemporaryFolder()

    private val scopeDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var api: UsuariosApiFalsa
    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        api = UsuariosApiFalsa()
        val dataStore = PreferenceDataStoreFactory.create(scope = scopeDataStore) {
            File(carpetaTemporal.root, "sesion.preferences_pb")
        }
        val json = Json { ignoreUnknownKeys = true }
        viewModel = LoginViewModel(AuthRepository(api, SesionDataStore(dataStore), json))
    }

    @After
    fun limpiar() {
        scopeDataStore.cancel()
        Dispatchers.resetMain()
    }

    /** Espera a que termine el login (el DataStore guarda en otro hilo) y devuelve el estado final. */
    private suspend fun estadoFinal(): EstadoLogin =
        viewModel.uiState.first { it.estado !is EstadoLogin.Cargando }.estado

    private fun escribir(email: String, password: String) {
        viewModel.onEmailCambio(email)
        viewModel.onPasswordCambio(password)
    }

    @Test
    fun emailVacio_muestraMensajeYNoLlamaAlBackend() = runTest {
        escribir(email = "   ", password = "clave")

        viewModel.iniciarSesion()

        assertEquals(EstadoLogin.Error(LoginViewModel.MENSAJE_CAMPOS_VACIOS), viewModel.uiState.value.estado)
        assertEquals(0, api.llamadas)
    }

    @Test
    fun passwordVacia_muestraMensajeYNoLlamaAlBackend() = runTest {
        escribir(email = "relator1@duocuc.test", password = "")

        viewModel.iniciarSesion()

        assertEquals(EstadoLogin.Error(LoginViewModel.MENSAJE_CAMPOS_VACIOS), viewModel.uiState.value.estado)
        assertEquals(0, api.llamadas)
    }

    @Test
    fun editarCampoDespuesDeError_vuelveAInicial() = runTest {
        val errorValidacion = EstadoLogin.Error(LoginViewModel.MENSAJE_CAMPOS_VACIOS)

        viewModel.iniciarSesion()
        assertEquals(errorValidacion, viewModel.uiState.value.estado)
        viewModel.onEmailCambio("r")
        assertEquals(EstadoLogin.Inicial, viewModel.uiState.value.estado)

        // La contraseña sigue vacía, así que vuelve a aparecer el error.
        viewModel.iniciarSesion()
        assertEquals(errorValidacion, viewModel.uiState.value.estado)
        viewModel.onPasswordCambio("c")
        assertEquals(EstadoLogin.Inicial, viewModel.uiState.value.estado)
    }

    @Test
    fun sinConexion_muestraMensajeDelRepositorio() = runTest {
        api.error = IOException("Failed to connect to localhost/127.0.0.1:8081")
        escribir(email = "relator1@duocuc.test", password = "clave")

        viewModel.iniciarSesion()

        assertEquals(EstadoLogin.Error("Sin conexión"), estadoFinal())
        assertEquals(1, api.llamadas)
    }

    @Test
    fun credencialesInvalidas_muestraMensajeDelRepositorio() = runTest {
        val cuerpo = """{"codigo":"CREDENCIALES_INVALIDAS","mensaje":"Email o contraseña incorrectos"}"""
            .toResponseBody("application/json".toMediaType())
        api.error = HttpException(Response.error<Any>(401, cuerpo))
        escribir(email = "relator1@duocuc.test", password = "incorrecta")

        viewModel.iniciarSesion()

        assertEquals(EstadoLogin.Error("Email o contraseña incorrectos"), estadoFinal())
    }

    @Test
    fun loginCorrecto_muestraUsuario() = runTest {
        api.respuesta = LoginRespuestaDto(
            token = "token-de-prueba",
            usuario = UsuarioDto(3, "Relator Ficticio Uno", "relator1@duocuc.test", "RELATOR")
        )
        escribir(email = "relator1@duocuc.test", password = "clave")

        viewModel.iniciarSesion()

        val usuarioEsperado = Usuario(3, "Relator Ficticio Uno", "relator1@duocuc.test", Rol.RELATOR)
        assertEquals(EstadoLogin.Exito(usuarioEsperado), estadoFinal())
    }

    @Test
    fun emailSeRecortaYPasswordSeEnviaTalCual() = runTest {
        api.error = IOException("sin red")
        escribir(email = "  relator1@duocuc.test  ", password = " clave ")

        viewModel.iniciarSesion()
        estadoFinal()

        assertEquals(LoginPeticionDto("relator1@duocuc.test", " clave "), api.ultimaPeticion)
    }
}
