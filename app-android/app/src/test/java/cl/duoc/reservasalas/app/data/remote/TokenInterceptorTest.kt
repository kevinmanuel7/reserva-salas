package cl.duoc.reservasalas.app.data.remote

import cl.duoc.reservasalas.app.SESION_PRUEBA
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas de TokenInterceptor: el token de la sesión se agrega a cada petición, y sin sesión la
 * petición sale tal cual. La sesión y la "cadena" de OkHttp (lo que envía la petición) se
 * simulan con MockK: así se revisa la petición final sin conectarse a ningún servidor.
 */
class TokenInterceptorTest {

    private val sesionDataStore = mockk<SesionDataStore>()
    private val cadena = mockk<Interceptor.Chain>()
    private val enviada = slot<Request>()

    /** Ejecuta el interceptor y devuelve la petición que se envió. */
    private fun peticionEnviada(): Request {
        every { cadena.request() } returns Request.Builder().url("http://localhost:8083/api/v1/reservas").build()
        every { cadena.proceed(capture(enviada)) } returns mockk<Response>()
        TokenInterceptor(sesionDataStore).intercept(cadena)
        return enviada.captured
    }

    @Test
    fun conSesion_agregaElTokenBearer() {
        coEvery { sesionDataStore.leerSesion() } returns SESION_PRUEBA

        assertEquals("Bearer token-de-prueba", peticionEnviada().header("Authorization"))
    }

    @Test
    fun sinSesion_laPeticionSaleSinEncabezado() {
        coEvery { sesionDataStore.leerSesion() } returns null

        assertNull(peticionEnviada().header("Authorization"))
    }
}
