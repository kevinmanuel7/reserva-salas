package cl.duoc.reservasalas.app

import android.app.Application

/**
 * Se crea antes que cualquier pantalla y vive mientras la app esté abierta.
 * Por eso guarda el único AppContainer de la app.
 */
class ReservaSalasApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
