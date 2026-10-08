package cl.duoc.reservasalas.app.data.remote

import cl.duoc.reservasalas.app.data.remote.dto.LoginPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.LoginRespuestaDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Endpoints de ms-usuarios. Retrofit genera el código que hace la petición.
 * La URL base (BuildConfig.URL_MS_USUARIOS) se configura en AppContainer.
 */
interface UsuariosApi {

    // Sin "/" al inicio: la URL base ya termina en "/".
    // Si el servidor responde un error (por ejemplo 401), Retrofit lanza HttpException.
    @POST("auth/login")
    suspend fun login(@Body peticion: LoginPeticionDto): LoginRespuestaDto
}
