package cl.duoc.reservasalas.app.data.remote

import cl.duoc.reservasalas.app.data.remote.dto.CrearReservaPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.HorarioDto
import cl.duoc.reservasalas.app.data.remote.dto.ReservaDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de ms-reservas. La URL base (BuildConfig.URL_MS_RESERVAS) se configura en AppContainer.
 * Todos piden token (salvo GET horario): lo agrega TokenInterceptor.
 */
interface ReservasApi {

    // GET .../api/v1/reservas?fecha=2026-11-16 → reservas ACTIVA de ese día.
    @GET("reservas")
    suspend fun listar(@Query("fecha") fecha: String): List<ReservaDto>

    // POST .../api/v1/reservas → 201 con la reserva creada (o error 400/404/409).
    @POST("reservas")
    suspend fun crear(@Body peticion: CrearReservaPeticionDto): ReservaDto

    // PUT .../api/v1/reservas/5/cancelar → la reserva 5 en estado CANCELADA.
    @PUT("reservas/{id}/cancelar")
    suspend fun cancelar(@Path("id") id: Long): ReservaDto

    // Solo el dueño. El servidor guarda la hora de Chile.
    @PUT("reservas/{id}/checkin")
    suspend fun registrarCheckIn(@Path("id") id: Long): ReservaDto

    // Solo coordinador.
    @PUT("reservas/{id}/verificar")
    suspend fun verificar(@Path("id") id: Long): ReservaDto

    // Solo coordinador.
    @PUT("reservas/{id}/no-presentado")
    suspend fun marcarNoPresentado(@Path("id") id: Long): ReservaDto

    // GET .../api/v1/horario → el horario general. Público: funciona sin sesión.
    @GET("horario")
    suspend fun obtenerHorario(): HorarioDto

    // PUT .../api/v1/horario → el horario guardado (o error 400/403). Solo coordinador.
    @PUT("horario")
    suspend fun actualizarHorario(@Body horario: HorarioDto): HorarioDto
}
