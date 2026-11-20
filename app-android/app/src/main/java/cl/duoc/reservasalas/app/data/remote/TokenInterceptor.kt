package cl.duoc.reservasalas.app.data.remote

import cl.duoc.reservasalas.app.data.local.SesionDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega "Authorization: Bearer <token>" a cada petición si hay sesión guardada.
 * Sin sesión (por ejemplo, en el login) la petición sale sin el encabezado.
 */
class TokenInterceptor(
    private val sesionDataStore: SesionDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sesionDataStore.leerSesion()?.token }

        val peticion = if (token == null) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(peticion)
    }
}