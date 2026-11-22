package cl.duoc.reservasalas.app

import android.app.Application
import cl.duoc.reservasalas.app.notificaciones.crearCanalRecordatorios

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

        // Recordatorios: el canal de notificaciones y, una sola vez, el sincronizador que
        // los programa o borra cada vez que cambian las reservas o la sesión.
        crearCanalRecordatorios(this)
        container.sincronizadorRecordatorios.iniciar(container.scopeApp)
    }
}
