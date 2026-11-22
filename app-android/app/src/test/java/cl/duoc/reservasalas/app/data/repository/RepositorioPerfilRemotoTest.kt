package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.remote.dto.ActualizarPerfilPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.CambiarPasswordPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.UsuarioDto
import cl.duoc.reservasalas.app.errorHttp
import cl.duoc.reservasalas.app.jsonError
import cl.duoc.reservasalas.app.model.PerfilUsuario
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate

/**
 * Pruebas de RepositorioPerfilRemoto sin servidor: UsuariosApi (Retrofit) se simula con MockK.
 * Se revisa que los datos viajen en el formato del contrato (fecha "AAAA-MM-DD") y que cada error
 * del servidor llegue a la pantalla como un mensaje en español, nunca como el error técnico.
 */
class RepositorioPerfilRemotoTest {

    private val api = mockk<UsuariosApi>()
    private val repositorio = RepositorioPerfilRemoto(api, Json { ignoreUnknownKeys = true })

    private val usuarioDto = UsuarioDto(3, "Ana", "ana@duocuc.test", "RELATOR", "1990-05-01", "+56912345678", "Docencia")
    private val perfilEsperado = PerfilUsuario(
        Usuario(3, "Ana", "ana@duocuc.test", Rol.RELATOR),
        LocalDate.of(1990, 5, 1),
        "+56912345678",
        "Docencia"
    )

    /** Ejecuta [accion] y devuelve el mensaje de la excepción que lanzó (o null si no lanzó). */
    private suspend fun mensajeDeError(accion: suspend () -> Unit): String? {
        val error = runCatching { accion() }.exceptionOrNull()
        if (error != null) assertTrue(error is IllegalStateException)
        return error?.message
    }

    @Test
    fun obtenerPerfil_convierteElUsuarioDelServidor() = runTest {
        coEvery { api.obtenerPerfil() } returns usuarioDto

        assertEquals(perfilEsperado, repositorio.obtenerPerfil())
    }

    @Test
    fun actualizarPerfil_enviaLaFechaComoTextoDelContrato() = runTest {
        val peticion = slot<ActualizarPerfilPeticionDto>()
        coEvery { api.actualizarPerfil(capture(peticion)) } returns usuarioDto

        val perfil = repositorio.actualizarPerfil(LocalDate.of(1990, 5, 1), null, "Docencia")

        assertEquals(ActualizarPerfilPeticionDto("1990-05-01", null, "Docencia"), peticion.captured)
        assertEquals(perfilEsperado, perfil)
    }

    @Test
    fun actualizarPerfil_sinFecha_laEnviaNullParaConservarLaGuardada() = runTest {
        val peticion = slot<ActualizarPerfilPeticionDto>()
        coEvery { api.actualizarPerfil(capture(peticion)) } returns usuarioDto

        repositorio.actualizarPerfil(null, "+56987654321", null)

        assertNull(peticion.captured.fechaNacimiento)
        assertEquals("+56987654321", peticion.captured.telefono)
    }

    @Test
    fun cambiarPassword_respuesta204_terminaSinError() = runTest {
        coEvery { api.cambiarPassword(any()) } returns Response.success(204, Unit)

        assertNull(mensajeDeError { repositorio.cambiarPassword("actual123", "nueva1234") })
        coVerify { api.cambiarPassword(CambiarPasswordPeticionDto("actual123", "nueva1234")) }
    }

    @Test
    fun cambiarPassword_contrasenaIncorrecta_muestraMensajeClaro() = runTest {
        val cuerpo = jsonError("CONTRASENA_INCORRECTA", "Password incorrecta")
            .toResponseBody("application/json".toMediaType())
        coEvery { api.cambiarPassword(any()) } returns Response.error(400, cuerpo)

        assertEquals(
            RepositorioPerfilRemoto.MENSAJE_CONTRASENA_INCORRECTA,
            mensajeDeError { repositorio.cambiarPassword("mala", "nueva1234") }
        )
    }

    @Test
    fun sinConexion_muestraSinConexion() = runTest {
        coEvery { api.cambiarPassword(any()) } throws IOException("sin red")

        assertEquals(
            RepositorioPerfilRemoto.MENSAJE_SIN_CONEXION,
            mensajeDeError { repositorio.cambiarPassword("actual123", "nueva1234") }
        )
    }

    @Test
    fun obtenerUsuario_404_muestraElMensajeDelServidor() = runTest {
        coEvery { api.obtenerUsuario(99) } throws errorHttp(404, jsonError("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"))

        assertEquals("Usuario no encontrado", mensajeDeError { repositorio.obtenerUsuario(99) })
    }

    @Test
    fun obtenerUsuario_existente_devuelveSusDatos() = runTest {
        coEvery { api.obtenerUsuario(3) } returns usuarioDto

        assertEquals(perfilEsperado, repositorio.obtenerUsuario(3))
    }

    @Test
    fun errorSinMensajeUtil_muestraElMensajeGenericoDeCadaAccion() = runTest {
        coEvery { api.obtenerPerfil() } throws errorHttp(500, "no es json")
        coEvery { api.actualizarPerfil(any()) } throws errorHttp(400, jsonError("OTRO", ""))
        coEvery { api.obtenerUsuario(any()) } throws RuntimeException("detalle técnico")

        assertEquals("No se pudo cargar tu perfil", mensajeDeError { repositorio.obtenerPerfil() })
        assertEquals("No se pudieron guardar tus datos", mensajeDeError { repositorio.actualizarPerfil(null, null, null) })
        assertEquals("No se pudieron cargar los datos del relator", mensajeDeError { repositorio.obtenerUsuario(1) })
    }
}
