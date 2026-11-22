package cl.duoc.reservasalas.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas de lo que la app guarda en el teléfono: la foto de perfil (DataStore "perfil") y los
 * ids de los recordatorios programados (DataStore "recordatorios").
 *
 * Se simula el DataStore con uno en memoria: lo que se prueba es la lógica de cada clase (claves,
 * agregar, quitar, borrar todo), no la escritura del archivo. Con un archivo real, en Windows varias
 * escrituras seguidas pueden fallar al renombrar el archivo temporal (el antivirus lo tiene abierto).
 */
class DataStoresTest {

    /** DataStore que guarda las preferencias en una variable, sin archivos. */
    private class DataStoreEnMemoria : DataStore<Preferences> {
        private val actual = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = actual

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val nuevo = transform(actual.value)
            actual.value = nuevo
            return nuevo
        }
    }

    @Test
    fun recordatorios_guardaYQuitaIdsYLuegoLosEntregaTodosUnaSolaVez() = runTest {
        val recordatorios = RecordatoriosDataStore(DataStoreEnMemoria())

        recordatorios.agregar(3)
        recordatorios.agregar(5)
        recordatorios.agregar(8)
        recordatorios.quitar(3)

        assertEquals(setOf(5, 8), recordatorios.leerYBorrarTodos())
        // Ya se borraron: la segunda vez no queda nada.
        assertEquals(emptySet<Int>(), recordatorios.leerYBorrarTodos())
    }

    @Test
    fun fotoDePerfil_seGuardaPorUsuarioYSePuedeBorrar() = runTest {
        val perfil = PerfilDataStore(DataStoreEnMemoria())

        perfil.guardarFoto(3, "/datos/foto_perfil_3.jpg")

        assertEquals("/datos/foto_perfil_3.jpg", perfil.foto(3).first())
        // Otro usuario del mismo teléfono no ve la foto.
        assertNull(perfil.foto(4).first())

        perfil.borrarFoto(3)
        assertNull(perfil.foto(3).first())
    }
}
