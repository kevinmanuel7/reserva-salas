package cl.duoc.reservasalas.app

import android.content.Context
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.local.sesionDataStore
import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.repository.AuthRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Inyección de dependencias manual: aquí se crea cada objeto una sola vez
 * y se le entregan los objetos que necesita. Lo crea ReservaSalasApp.
 */
class AppContainer(context: Context) {

    // ignoreUnknownKeys: si el servidor agrega campos nuevos, la app no falla.
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofitUsuarios: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.URL_MS_USUARIOS)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val usuariosApi: UsuariosApi = retrofitUsuarios.create(UsuariosApi::class.java)

    val sesionDataStore = SesionDataStore(context.sesionDataStore)

    val authRepository = AuthRepository(usuariosApi, sesionDataStore, json)
}
