package cl.duoc.reservasalas.app.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.remote.dto.LoginPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.LoginRespuestaDto
import cl.duoc.reservasalas.app.data.repository.AuthRepository
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

/**
 * Prueba de interfaz: corre en el teléfono, dibuja el LoginScreen real con un
 * LoginViewModel real y simula toques y escritura, como lo haría un usuario.
 * La API es falsa y el DataStore usa un archivo temporal: no hay red ni se toca la sesión real.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    /** Reemplaza a Retrofit: cuenta las llamadas y responde "sin conexión". */
    private class UsuariosApiFalsa : UsuariosApi {
        var llamadas = 0

        override suspend fun login(peticion: LoginPeticionDto): LoginRespuestaDto {
            llamadas++
            throw IOException("sin red en la prueba")
        }
    }

    // Permite dibujar Composables en la prueba y buscarlos en la pantalla.
    @get:Rule
    val composeRule = createComposeRule()

    private val scopeDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var archivoDataStore: File
    private lateinit var api: UsuariosApiFalsa

    @Before
    fun preparar() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        archivoDataStore = File(contexto.cacheDir, "sesion-prueba-${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scopeDataStore) { archivoDataStore }
        api = UsuariosApiFalsa()
        val viewModel = LoginViewModel(
            AuthRepository(api, SesionDataStore(dataStore), Json { ignoreUnknownKeys = true })
        )

        composeRule.setContent {
            ReservaSalasTheme {
                LoginScreen(viewModel = viewModel)
            }
        }
    }

    @After
    fun limpiar() {
        scopeDataStore.cancel()
        archivoDataStore.delete()
    }

    private fun tocarIniciarSesion() {
        composeRule.onNodeWithText("Iniciar sesión").performClick()
    }

    @Test
    fun camposVacios_muestraMensajeDeValidacion() {
        tocarIniciarSesion()

        composeRule.onNodeWithText(LoginViewModel.MENSAJE_CAMPOS_VACIOS).assertIsDisplayed()
        assertEquals(0, api.llamadas)
    }

    @Test
    fun errorDesapareceAlEditarEmail() {
        tocarIniciarSesion()
        composeRule.onNodeWithText(LoginViewModel.MENSAJE_CAMPOS_VACIOS).assertIsDisplayed()

        composeRule.onNodeWithText("Email").performTextInput("r")

        composeRule.onNodeWithText(LoginViewModel.MENSAJE_CAMPOS_VACIOS).assertDoesNotExist()
    }

    @Test
    fun errorDesapareceAlEditarPassword() {
        tocarIniciarSesion()
        composeRule.onNodeWithText(LoginViewModel.MENSAJE_CAMPOS_VACIOS).assertIsDisplayed()

        composeRule.onNodeWithText("Contraseña").performTextInput("c")

        composeRule.onNodeWithText(LoginViewModel.MENSAJE_CAMPOS_VACIOS).assertDoesNotExist()
    }
}
