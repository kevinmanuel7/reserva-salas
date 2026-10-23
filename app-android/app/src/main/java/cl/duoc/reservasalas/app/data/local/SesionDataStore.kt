package cl.duoc.reservasalas.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Archivo de DataStore "sesion", único para toda la app. */
val Context.sesionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

/**
 * Guarda, lee y borra la sesión (token y usuario) en el teléfono.
 * Recibe el DataStore por parámetro para que las pruebas puedan usar uno temporal.
 */
class SesionDataStore(private val dataStore: DataStore<Preferences>) {

    private object Claves {
        val TOKEN = stringPreferencesKey("token")
        val ID = intPreferencesKey("id")
        val NOMBRE = stringPreferencesKey("nombre")
        val EMAIL = stringPreferencesKey("email")
        val ROL = stringPreferencesKey("rol")
    }

    /** La sesión actual, o null si no hay sesión. Emite de nuevo cada vez que cambia. */
    val sesion: Flow<Sesion?> = dataStore.data.map { preferencias ->
        val token = preferencias[Claves.TOKEN]
        val id = preferencias[Claves.ID]
        val nombre = preferencias[Claves.NOMBRE]
        val email = preferencias[Claves.EMAIL]
        val rol = Rol.entries.find { it.name == preferencias[Claves.ROL] }

        if (token == null || id == null || nombre == null || email == null || rol == null) {
            null
        } else {
            Sesion(token, Usuario(id, nombre, email, rol))
        }
    }

    suspend fun guardarSesion(sesion: Sesion) {
        dataStore.edit { preferencias ->
            preferencias[Claves.TOKEN] = sesion.token
            preferencias[Claves.ID] = sesion.usuario.id
            preferencias[Claves.NOMBRE] = sesion.usuario.nombre
            preferencias[Claves.EMAIL] = sesion.usuario.email
            preferencias[Claves.ROL] = sesion.usuario.rol.name
        }
    }

    /** Lee la sesión una sola vez. */
    suspend fun leerSesion(): Sesion? = sesion.first()

    suspend fun borrarSesion() {
        dataStore.edit { preferencias -> preferencias.clear() }
    }
}
