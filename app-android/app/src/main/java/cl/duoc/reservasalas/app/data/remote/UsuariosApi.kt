package cl.duoc.reservasalas.app.data.remote

import cl.duoc.reservasalas.app.data.remote.dto.ActualizarPerfilPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.CambiarPasswordPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.LoginPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.LoginRespuestaDto
import cl.duoc.reservasalas.app.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Endpoints de ms-usuarios. Retrofit genera el código que hace la petición.
 * La URL base (BuildConfig.URL_MS_USUARIOS) se configura en AppContainer.
 */
interface UsuariosApi {

    // Sin "/" al inicio: la URL base ya termina en "/".
    // Si el servidor responde un error (por ejemplo 401), Retrofit lanza HttpException.
    @POST("auth/login")
    suspend fun login(@Body peticion: LoginPeticionDto): LoginRespuestaDto

    // GET .../api/v1/usuarios/me → el usuario del token, con fecha, teléfono y área.
    @GET("usuarios/me")
    suspend fun obtenerPerfil(): UsuarioDto

    // PUT .../api/v1/usuarios/me → el usuario actualizado (o 400 DATOS_INVALIDOS).
    @PUT("usuarios/me")
    suspend fun actualizarPerfil(@Body peticion: ActualizarPerfilPeticionDto): UsuarioDto

    // Responde 204 sin cuerpo. Response<Unit> evita que Retrofit intente leer un JSON que no
    // existe; por eso aquí hay que revisar a mano si la respuesta fue exitosa.
    @PUT("usuarios/me/password")
    suspend fun cambiarPassword(@Body peticion: CambiarPasswordPeticionDto): Response<Unit>

    // Solo coordinador (403 si lo pide un relator, 404 si no existe).
    @GET("usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Int): UsuarioDto
}
