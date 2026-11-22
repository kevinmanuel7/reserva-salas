package cl.duoc.reservasalas.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Archivo de DataStore "recordatorios", separado de "sesion" y "perfil": solo guarda los ids
 * de las reservas que tienen una alarma programada.
 */
val Context.recordatoriosDataStore: DataStore<Preferences> by preferencesDataStore(name = "recordatorios")

/**
 * Recuerda qué alarmas de recordatorio se programaron. AlarmManager no permite pedir
 * "todas las alarmas de la app", así que para borrarlas todas (al cerrar sesión) hace falta
 * saber sus ids. Recibe el DataStore por parámetro para que las pruebas puedan usar uno temporal.
 */
class RecordatoriosDataStore(private val dataStore: DataStore<Preferences>) {

    // DataStore guarda conjuntos de texto, no de números: cada id se guarda como "42".
    private val claveIds = stringSetPreferencesKey("ids")

    suspend fun agregar(idReserva: Int) {
        dataStore.edit { preferencias ->
            preferencias[claveIds] = preferencias[claveIds].orEmpty() + idReserva.toString()
        }
    }

    suspend fun quitar(idReserva: Int) {
        dataStore.edit { preferencias ->
            preferencias[claveIds] = preferencias[claveIds].orEmpty() - idReserva.toString()
        }
    }

    /** Devuelve todos los ids guardados y deja la lista vacía, en un solo paso. */
    suspend fun leerYBorrarTodos(): Set<Int> {
        var ids: Set<Int> = emptySet()
        dataStore.edit { preferencias ->
            ids = preferencias[claveIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
            preferencias.remove(claveIds)
        }
        return ids
    }
}
