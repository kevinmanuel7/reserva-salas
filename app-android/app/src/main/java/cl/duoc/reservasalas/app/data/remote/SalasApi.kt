package cl.duoc.reservasalas.app.data.remote

import cl.duoc.reservasalas.app.data.remote.dto.ActualizarSalaPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.SalaDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints de ms-salas. La URL base (BuildConfig.URL_MS_SALAS) se configura en AppContainer. */
interface SalasApi {

    // GET .../api/v1/salas → salas activas. Es público (no necesita token).
    @GET("salas")
    suspend fun listar(): List<SalaDto>

    // PUT .../api/v1/salas/5 → actualiza la sala 5. Solo COORDINADOR (el token lo agrega TokenInterceptor).
    @PUT("salas/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body peticion: ActualizarSalaPeticionDto
    ): SalaDto
}