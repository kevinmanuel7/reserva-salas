package cl.duoc.reservasalas.app.data.remote

import cl.duoc.reservasalas.app.data.remote.dto.FeriadoDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API externa Nager.Date (pública, sin clave). No es de nuestro backend.
 * La URL base (BuildConfig.URL_API_FERIADOS) se configura en AppContainer, con un cliente
 * HTTP propio que NO envía el token de la sesión.
 */
interface FeriadosApi {

    // GET .../PublicHolidays/2026/CL → todos los feriados de Chile en 2026.
    // @Path reemplaza {anio} en la ruta por el número que recibe la función.
    @GET("PublicHolidays/{anio}/CL")
    suspend fun feriados(@Path("anio") anio: Int): List<FeriadoDto>
}
