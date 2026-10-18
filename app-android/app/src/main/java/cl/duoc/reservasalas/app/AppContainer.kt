package cl.duoc.reservasalas.app

import android.content.Context
import cl.duoc.reservasalas.app.data.falso.AutenticacionFalsa
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.falso.RelojMaqueta
import cl.duoc.reservasalas.app.data.falso.RelojSistema
import cl.duoc.reservasalas.app.data.falso.RepositorioHorarioFalso
import cl.duoc.reservasalas.app.data.falso.RepositorioReservasFalso
import cl.duoc.reservasalas.app.data.falso.RepositorioSalasFalso
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
    private val usuariosApi: UsuariosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.URL_MS_USUARIOS)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UsuariosApi::class.java)
    }

    val sesionDataStore = SesionDataStore(context.sesionDataStore)

    private val authRepository: AuthRepository by lazy {
        AuthRepository(usuariosApi, sesionDataStore, json)
    }

    /** "Ahora" de la app. En la maqueta siempre son las 09:55 de hoy (ver RelojMaqueta). */
    val reloj: Reloj = if (usarDatosFalsos) RelojMaqueta() else RelojSistema()

    val repositorioAutenticacion: RepositorioAutenticacion =
        if (usarDatosFalsos) AutenticacionFalsa(sesionDataStore) else authRepository

    // Todavía no existen las versiones reales de estos tres (próxima evaluación).
    val repositorioSalas: RepositorioSalas = RepositorioSalasFalso()
    val repositorioReservas: RepositorioReservas = RepositorioReservasFalso(reloj)
    val repositorioHorario: RepositorioHorario = RepositorioHorarioFalso()
}
