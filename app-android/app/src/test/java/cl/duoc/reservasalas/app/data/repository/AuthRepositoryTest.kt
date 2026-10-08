package cl.duoc.reservasalas.app.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.remote.dto.LoginPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.LoginRespuestaDto
import cl.duoc.reservasalas.app.data.remote.dto.UsuarioDto
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException

/**
 * Pruebas de AuthRepository sin teléfono ni backend:
 * - la API es una clase falsa que responde lo que cada prueba le indica;
 * - el DataStore es real, pero guarda en un archivo temporal que se borra al terminar.
 */
class AuthRepositoryTest {

    /** Reemplaza a Retrofit: devuelve [respuesta] o lanza [error]. */
    private class UsuariosApiFalsa : UsuariosApi {
        var respuesta: LoginRespuestaDto? = null
        var error: Exception? = null

        override suspend fun login(peticion: LoginPeticionDto): LoginRespuestaDto {
            error?.let { throw it }
            return respuesta!!
        }
    }

    @get:Rule
    val carpetaTemporal = TemporaryFolder()

    private val json = Json { ignoreUnknownKeys = true }
    private val scopeDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var api: UsuariosApiFalsa
    private lateinit var sesionDataStore: SesionDataStore
    private lateinit var repositorio: AuthRepository

    @Before
    fun preparar() {
        api = UsuariosApiFalsa()
        val dataStore = PreferenceDataStoreFactory.create(scope = scopeDataStore) {
            File(carpetaTemporal.root, "sesion.preferences_pb")
        }
        sesionDataStore = SesionDataStore(dataStore)
        repositorio = AuthRepository(api, sesionDataStore, json)
    }

    @After
    fun limpiar() {
        scopeDataStore.cancel()
    }

    @Test
    fun loginCorrecto_devuelveUsuarioYGuardaSesion() = runBlocking {
        api.respuesta = LoginRespuestaDto(
            token = "token-de-prueba",
            usuario = UsuarioDto(3, "Relator Ficticio Uno", "relator1@duocuc.test", "RELATOR")
        )

        val resultado = repositorio.login("relator1@duocuc.test", "clave")

        val usuarioEsperado = Usuario(3, "Relator Ficticio Uno", "relator1@duocuc.test", Rol.RELATOR)
        assertEquals(ResultadoLogin.Exito(usuarioEsperado), resultado)
        assertEquals(Sesion("token-de-prueba", usuarioEsperado), sesionDataStore.leerSesion())
    }

    @Test
    fun credencialesInvalidas_devuelveMensajeYNoGuardaSesion() = runBlocking {
        val cuerpo = """{"codigo":"CREDENCIALES_INVALIDAS","mensaje":"Email o contraseña incorrectos"}"""
            .toResponseBody("application/json".toMediaType())
        api.error = HttpException(Response.error<Any>(401, cuerpo))

        val resultado = repositorio.login("relator1@duocuc.test", "incorrecta")

        assertEquals(ResultadoLogin.Error("Email o contraseña incorrectos"), resultado)
        assertNull(sesionDataStore.leerSesion())
    }

    @Test
    fun otroErrorDelServidor_devuelveMensajeDelServidor() = runBlocking {
        val cuerpo = """{"codigo":"ERROR_INTERNO","mensaje":"Texto de prueba"}"""
            .toResponseBody("application/json".toMediaType())
        api.error = HttpException(Response.error<Any>(500, cuerpo))

        val resultado = repositorio.login("relator1@duocuc.test", "clave")

        assertEquals(ResultadoLogin.Error("Texto de prueba"), resultado)
        assertNull(sesionDataStore.leerSesion())
    }

    @Test
    fun sinConexion_devuelveMensajeYNoGuardaSesion() = runBlocking {
        api.error = IOException("Failed to connect to localhost/127.0.0.1:8081")

        val resultado = repositorio.login("relator1@duocuc.test", "clave")

        assertEquals(ResultadoLogin.Error("Sin conexión"), resultado)
        assertNull(sesionDataStore.leerSesion())
    }
}
