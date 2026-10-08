package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.remote.dto.ErrorDto
import cl.duoc.reservasalas.app.data.remote.dto.LoginPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.aModelo
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/** Resultado del login: o salió bien, o hay un mensaje listo para mostrar. */
sealed interface ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin
    data class Error(val mensaje: String) : ResultadoLogin
}

/**
 * Hace el login contra ms-usuarios y guarda la sesión si es correcto.
 * Nunca lanza excepciones hacia afuera: las convierte en un mensaje para el usuario.
 */
class AuthRepository(
    private val api: UsuariosApi,
    private val sesionDataStore: SesionDataStore,
    private val json: Json
) {

    suspend fun login(email: String, password: String): ResultadoLogin {
        return try {
            val respuesta = api.login(LoginPeticionDto(email, password))
            val usuario = respuesta.usuario.aModelo()
            sesionDataStore.guardarSesion(Sesion(respuesta.token, usuario))
            ResultadoLogin.Exito(usuario)
        } catch (e: CancellationException) {
            // Si la pantalla se cerró, la corrutina se cancela: no es un error de login.
            throw e
        } catch (e: HttpException) {
            ResultadoLogin.Error(mensajeDeErrorHttp(e))
        } catch (e: IOException) {
            // Sin red, servidor apagado o tiempo de espera agotado.
            ResultadoLogin.Error(MENSAJE_SIN_CONEXION)
        } catch (e: Exception) {
            // JSON inesperado, rol desconocido, etc. Nunca se muestra el error técnico.
            ResultadoLogin.Error(MENSAJE_GENERICO)
        }
    }

    /**
     * Lee el JSON de error del servidor y decide el mensaje según su "codigo".
     * Para los demás códigos muestra el "mensaje" del servidor (AGENTS.md de la app, sección 4).
     */
    private fun mensajeDeErrorHttp(e: HttpException): String {
        val error = try {
            val cuerpo = e.response()?.errorBody()?.string() ?: return MENSAJE_GENERICO
            json.decodeFromString<ErrorDto>(cuerpo)
        } catch (_: Exception) {
            // El cuerpo no es el JSON de error esperado (o le falta "mensaje").
            return MENSAJE_GENERICO
        }
        return when {
            error.codigo == "CREDENCIALES_INVALIDAS" -> MENSAJE_CREDENCIALES_INVALIDAS
            error.mensaje.isNotBlank() -> error.mensaje
            else -> MENSAJE_GENERICO
        }
    }

    companion object {
        const val MENSAJE_CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos"
        const val MENSAJE_SIN_CONEXION = "Sin conexión"
        const val MENSAJE_GENERICO = "No se pudo iniciar sesión. Intenta nuevamente."
    }
}
