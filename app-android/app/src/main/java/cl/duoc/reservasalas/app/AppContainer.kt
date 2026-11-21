package cl.duoc.reservasalas.app

import android.content.Context
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.falso.RelojMaqueta
import cl.duoc.reservasalas.app.data.falso.RelojSistema
import cl.duoc.reservasalas.app.data.falso.RepositorioHorarioFalso
import cl.duoc.reservasalas.app.data.falso.RepositorioReservasFalso
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.local.sesionDataStore
import cl.duoc.reservasalas.app.data.remote.UsuariosApi
import cl.duoc.reservasalas.app.data.repository.AuthRepository
import cl.duoc.reservasalas.app.data.repository.RepositorioAutenticacion
import cl.duoc.reservasalas.app.data.repository.RepositorioHorario
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import cl.duoc.reservasalas.app.data.remote.TokenInterceptor
import okhttp3.OkHttpClient
import cl.duoc.reservasalas.app.data.remote.SalasApi
import cl.duoc.reservasalas.app.data.repository.RepositorioSalasRemoto

/**
 * Inyección de dependencias manual: aquí se crea cada objeto una sola vez
 * y se le entregan los objetos que necesita. Lo crea ReservaSalasApp.
 *
 * Los ViewModels piden sus repositorios aquí (app.container.repositorioSalas, etc.).
 */
class AppContainer(context: Context) {

    /**
     * Etapa maqueta (hasta el 28 de octubre): true = toda la app usa datos falsos, sin red.
     * Para conectar el backend se cambia a false y se agregan los repositorios reales.
     */
    private val usarDatosFalsos = true

    // ignoreUnknownKeys: si el servidor agrega campos nuevos, la app no falla.
    private val json = Json { ignoreUnknownKeys = true }

    // "by lazy": se crea la primera vez que alguien lo usa. En la maqueta nadie lo usa, así que no hay red.

    // Cliente HTTP compartido: todas las peticiones pasan por TokenInterceptor.
    private val clienteHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(sesionDataStore))
            .build()
    }
    private val usuariosApi: UsuariosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.URL_MS_USUARIOS)
            .client(clienteHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UsuariosApi::class.java)
    }

    // Endpoints de ms-salas (puerto 8082 en local). Usa clienteHttp, así también lleva el token.
    private val salasApi: SalasApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.URL_MS_SALAS)
            .client(clienteHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SalasApi::class.java)
    }

    val sesionDataStore = SesionDataStore(context.sesionDataStore)

    private val authRepository: AuthRepository by lazy {
        AuthRepository(usuariosApi, sesionDataStore, json)
    }

    /** "Ahora" de la app. En la maqueta siempre son las 09:55 de hoy (ver RelojMaqueta). */
    val reloj: Reloj = if (usarDatosFalsos) RelojMaqueta() else RelojSistema()

    val repositorioAutenticacion: RepositorioAutenticacion = authRepository

    val repositorioSalas: RepositorioSalas = RepositorioSalasRemoto(salasApi)
    val repositorioReservas: RepositorioReservas = RepositorioReservasFalso(reloj)
    val repositorioHorario: RepositorioHorario = RepositorioHorarioFalso()
}
