package cl.duoc.reservasalas.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Archivo de DataStore "perfil", separado de "sesion": cerrar sesión borra "sesion" completo,
 * y la foto de cada usuario debe seguir ahí cuando vuelva a entrar.
 */
val Context.perfilDataStore: DataStore<Preferences> by preferencesDataStore(name = "perfil")

/**
 * Guarda en el teléfono la ruta de la foto de perfil de cada usuario (la foto no sube al servidor).
 * Recibe el DataStore por parámetro para que las pruebas puedan usar uno temporal.
 */
class PerfilDataStore(private val dataStore: DataStore<Preferences>) {

    // Una clave por usuario, ej. "foto_3": así dos usuarios del mismo teléfono no se mezclan.
    private fun claveFoto(idUsuario: Int) = stringPreferencesKey("foto_$idUsuario")

    /** Ruta del archivo de la foto, o null si el usuario no tiene foto. Emite cada vez que cambia. */
    fun foto(idUsuario: Int): Flow<String?> =
        dataStore.data
            .map { preferencias -> preferencias[claveFoto(idUsuario)] }
            .distinctUntilChanged()

    suspend fun guardarFoto(idUsuario: Int, ruta: String) {
        dataStore.edit { preferencias -> preferencias[claveFoto(idUsuario)] = ruta }
    }

    suspend fun borrarFoto(idUsuario: Int) {
        dataStore.edit { preferencias -> preferencias.remove(claveFoto(idUsuario)) }
    }
}
