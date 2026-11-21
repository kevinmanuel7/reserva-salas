package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.remote.dto.ActualizarPerfilPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.CambiarPasswordPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.ErrorDto
import cl.duoc.reservasalas.app.data.remote.dto.aPerfil
import cl.duoc.reservasalas.app.model.PerfilUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

/**
 * Perfil real desde ms-usuarios. No guarda copia en memoria: cada pantalla lo pide al abrirse.
 * Si algo falla, lanza IllegalStateException con el mensaje que ve el usuario.
 */
class RepositorioPerfilRemoto(
    private val api: UsuariosApi,
    private val json: Json
) : RepositorioPerfil {

    override suspend fun obtenerPerfil(): PerfilUsuario =
        llamar("No se pudo cargar tu perfil") { api.obtenerPerfil().aPerfil() }

    override suspend fun actualizarPerfil(
        fechaNacimiento: LocalDate?,
        telefono: String?,
        area: String?
    ): PerfilUsuario = llamar("No se pudieron guardar tus datos") {
        // LocalDate.toString() ya entrega "yyyy-MM-dd", el formato del contrato.
        api.actualizarPerfil(
            ActualizarPerfilPeticionDto(fechaNacimiento?.toString(), telefono, area)
        ).aPerfil()
    }

    override suspend fun cambiarPassword(passwordActual: String, passwordNueva: String) =
        llamar("No se pudo cambiar la contraseña") {
            val respuesta = api.cambiarPassword(CambiarPasswordPeticionDto(passwordActual, passwordNueva))
            // Con Response<Unit>, Retrofit no lanza HttpException solo: se lanza aquí.
            if (!respuesta.isSuccessful) throw HttpException(respuesta)
        }

    override suspend fun obtenerUsuario(id: Int): PerfilUsuario =
        llamar("No se pudieron cargar los datos del relator") { api.obtenerUsuario(id).aPerfil() }

    /**
     * Ejecuta la [llamada] y convierte cualquier error en un mensaje para el usuario.
     * <T> significa que sirve para cualquier tipo de resultado (PerfilUsuario, Unit...).
     */
    private suspend fun <T> llamar(mensajeGenerico: String, llamada: suspend () -> T): T =
        try {
            llamada()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(mensajeDeError(e, mensajeGenerico))
        }

    /** Traduce la excepción al mensaje que ve el usuario (nunca el error técnico). */
    private fun mensajeDeError(e: Exception, mensajeGenerico: String): String = when (e) {
        is IOException -> MENSAJE_SIN_CONEXION
        is HttpException -> {
            val error = try {
                e.response()?.errorBody()?.string()?.let { json.decodeFromString<ErrorDto>(it) }
            } catch (_: Exception) {
                null
            }
            when {
                error == null -> mensajeGenerico
                error.codigo == "CONTRASENA_INCORRECTA" -> MENSAJE_CONTRASENA_INCORRECTA
                error.mensaje.isNotBlank() -> error.mensaje
                else -> mensajeGenerico
            }
        }
        else -> mensajeGenerico
    }

    companion object {
        const val MENSAJE_SIN_CONEXION = "Sin conexión"
        const val MENSAJE_CONTRASENA_INCORRECTA = "La contraseña actual no es correcta"
    }
}
