package cl.duoc.reservasalas.app.ui.pantallas.detallesala

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.coroutines.flow.StateFlow

class DetalleSalaViewModel(
    private val repositorioSalas: RepositorioSalas
) : ViewModel() {

    val salas: StateFlow<List<Sala>> = repositorioSalas.salas

    fun buscarSala(codigo: String): Sala? {
        return salas.value.find { it.codigo == codigo }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor =
                    (this[APPLICATION_KEY] as ReservaSalasApp).container

                DetalleSalaViewModel(
                    repositorioSalas = contenedor.repositorioSalas
                )
            }
        }
    }
}
