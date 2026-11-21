package cl.duoc.reservasalas.app.ui.pantallas.perfil

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.falso.RepositorioReservasFalso
import cl.duoc.reservasalas.app.data.local.AlmacenFotoPerfil
import cl.duoc.reservasalas.app.data.local.PerfilDataStore
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.repository.RepositorioPerfil
import cl.duoc.reservasalas.app.model.PerfilUsuario
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Pruebas de las validaciones de PerfilViewModel, sin teléfono ni backend:
 * - el repositorio del perfil es falso y anota qué recibió;
 * - los DataStore son reales, pero guardan en una carpeta temporal;
 * - el reloj está fijo en el 21/11/2026 para que las fechas no dependan del día de la prueba.
 * Con UnconfinedTestDispatcher, cada viewModelScope.launch corre al instante.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {

    /** Reemplaza al servidor: devuelve el perfil guardado y anota cada llamada. */
    private class RepositorioPerfilFalso : RepositorioPerfil {
        var perfil = PerfilUsuario(
            usuario = Usuario(3, "Relator Ficticio Uno", "relator1@duocuc.test", Rol.RELATOR),
            fechaNacimiento = null,
            telefono = null,
            area = null
        )
        var llamadasActualizar = 0
        var ultimaFecha: LocalDate? = null
        var ultimoTelefono: String? = null
        var llamadasPassword = 0
        var ultimaPassword: Pair<String, String>? = null

        override suspend fun obtenerPerfil(): PerfilUsuario = perfil

        override suspend fun actualizarPerfil(
            fechaNacimiento: LocalDate?,
            telefono: String?,
            area: String?
        ): PerfilUsuario {
            llamadasActualizar++
            ultimaFecha = fechaNacimiento
            ultimoTelefono = telefono
            perfil = perfil.copy(
                fechaNacimiento = fechaNacimiento ?: perfil.fechaNacimiento,
                telefono = telefono ?: perfil.telefono,
                area = area ?: perfil.area
            )
            return perfil
        }

        override suspend fun cambiarPassword(passwordActual: String, passwordNueva: String) {
            llamadasPassword++
            ultimaPassword = passwordActual to passwordNueva
        }

        override suspend fun obtenerUsuario(id: Int): PerfilUsuario = perfil
    }

    /** La foto no se prueba aquí: este almacén no toca archivos. */
    private class AlmacenFotoFalso : AlmacenFotoPerfil {
        override fun crearArchivoCamara(): Uri = TODO("No se usa en estas pruebas")
        override suspend fun guardar(origen: Uri, idUsuario: Int): String = TODO("No se usa en estas pruebas")
        override suspend fun cargar(ruta: String): ImageBitmap? = null
        override suspend fun borrar(ruta: String) {}
    }

    private val reloj = object : Reloj {
        override fun ahora(): LocalDateTime = LocalDateTime.of(2026, 11, 21, 10, 0)
    }
    private val hoy: LocalDate = reloj.hoy()

    @get:Rule
    val carpetaTemporal = TemporaryFolder()

    private val scopeDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val almacenViewModels = ViewModelStore()

    private lateinit var repositorio: RepositorioPerfilFalso
    private lateinit var viewModel: PerfilViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repositorio = RepositorioPerfilFalso()
        val sesion = PreferenceDataStoreFactory.create(scope = scopeDataStore) {
            File(carpetaTemporal.root, "sesion.preferences_pb")
        }
        val perfil = PreferenceDataStoreFactory.create(scope = scopeDataStore) {
            File(carpetaTemporal.root, "perfil.preferences_pb")
        }
        // El ViewModel se crea dentro de un ViewModelStore (como lo hace Android) para poder
        // cerrarlo en limpiar(): así sus corrutinas se detienen antes de terminar la prueba.
        val fabrica = viewModelFactory {
            initializer {
                PerfilViewModel(
                    sesionDataStore = SesionDataStore(sesion),
                    repositorioReservas = RepositorioReservasFalso(reloj),
                    reloj = reloj,
                    repositorioPerfil = repositorio,
                    perfilDataStore = PerfilDataStore(perfil),
                    almacenFoto = AlmacenFotoFalso()
                )
            }
        }
        viewModel = ViewModelProvider.create(almacenViewModels, fabrica)[PerfilViewModel::class]
    }

    @After
    fun limpiar() {
        almacenViewModels.clear()
        scopeDataStore.cancel()
        Dispatchers.resetMain()
    }

    private val formulario get() = viewModel.formulario.value

    // ---------- Fecha de nacimiento ----------

    @Test
    fun fechaFutura_muestraErrorYNoLlamaAlServidor() {
        viewModel.onFechaNacimientoCambio(hoy.plusDays(1))

        viewModel.guardarDatos()

        assertEquals(PerfilViewModel.MENSAJE_FECHA_FUTURA, formulario.mensajeError)
        assertEquals(0, repositorio.llamadasActualizar)
    }

    @Test
    fun menorDe18_muestraErrorYNoLlamaAlServidor() {
        // Cumple 18 mañana.
        viewModel.onFechaNacimientoCambio(hoy.minusYears(18).plusDays(1))

        viewModel.guardarDatos()

        assertEquals(PerfilViewModel.MENSAJE_MENOR_DE_EDAD, formulario.mensajeError)
        assertEquals(0, repositorio.llamadasActualizar)
    }

    @Test
    fun cumple18Hoy_seEnvia() {
        viewModel.onFechaNacimientoCambio(hoy.minusYears(18))

        viewModel.guardarDatos()

        assertNull(formulario.mensajeError)
        assertEquals(1, repositorio.llamadasActualizar)
        assertEquals(hoy.minusYears(18), repositorio.ultimaFecha)
        assertEquals(PerfilViewModel.MENSAJE_DATOS_GUARDADOS, formulario.mensajeExito)
    }

    // ---------- Teléfono ----------

    @Test
    fun telefonoConMenosDe8Digitos_muestraErrorYNoLlamaAlServidor() {
        viewModel.onTelefonoCambio("1234567")

        viewModel.guardarDatos()

        assertEquals(PerfilViewModel.MENSAJE_TELEFONO_INVALIDO, formulario.mensajeError)
        assertEquals(0, repositorio.llamadasActualizar)
    }

    @Test
    fun telefonoValido_seEnviaConPrefijo() {
        // Los espacios se descartan: en el campo solo quedan los dígitos.
        viewModel.onTelefonoCambio("1234 5678")

        viewModel.guardarDatos()

        assertEquals("12345678", formulario.telefono)
        assertEquals(1, repositorio.llamadasActualizar)
        assertEquals("+56912345678", repositorio.ultimoTelefono)
        // La fecha no cambió: va null y el servidor la conserva.
        assertNull(repositorio.ultimaFecha)
    }

    // ---------- Contraseña ----------

    private fun escribirPasswords(actual: String, nueva: String, confirmacion: String) {
        viewModel.abrirDialogoPassword()
        viewModel.onPasswordActualCambio(actual)
        viewModel.onPasswordNuevaCambio(nueva)
        viewModel.onPasswordConfirmacionCambio(confirmacion)
    }

    @Test
    fun passwordCorta_muestraErrorYNoLlamaAlServidor() {
        escribirPasswords(actual = "actual123", nueva = "corta", confirmacion = "corta")

        viewModel.cambiarPassword()

        assertEquals(PerfilViewModel.MENSAJE_PASSWORD_CORTA, formulario.errorPassword)
        assertEquals(0, repositorio.llamadasPassword)
    }

    @Test
    fun passwordIgualALaActual_muestraErrorYNoLlamaAlServidor() {
        escribirPasswords(actual = "actual123", nueva = "actual123", confirmacion = "actual123")

        viewModel.cambiarPassword()

        assertEquals(PerfilViewModel.MENSAJE_PASSWORD_IGUAL, formulario.errorPassword)
        assertEquals(0, repositorio.llamadasPassword)
    }

    @Test
    fun confirmacionDistinta_muestraErrorYNoLlamaAlServidor() {
        escribirPasswords(actual = "actual123", nueva = "nuevaSegura1", confirmacion = "nuevaSegura2")

        viewModel.cambiarPassword()

        assertEquals(PerfilViewModel.MENSAJE_CONFIRMACION_DISTINTA, formulario.errorPassword)
        assertEquals(0, repositorio.llamadasPassword)
    }

    @Test
    fun cambioValido_llamaAlServidorYCierraElDialogo() {
        escribirPasswords(actual = "actual123", nueva = "nuevaSegura1", confirmacion = "nuevaSegura1")

        viewModel.cambiarPassword()

        assertEquals(1, repositorio.llamadasPassword)
        assertEquals("actual123" to "nuevaSegura1", repositorio.ultimaPassword)
        assertFalse(formulario.dialogoPassword)
        assertEquals("", formulario.passwordNueva)
        assertEquals(PerfilViewModel.MENSAJE_PASSWORD_CAMBIADA, formulario.mensajeExito)
    }
}
