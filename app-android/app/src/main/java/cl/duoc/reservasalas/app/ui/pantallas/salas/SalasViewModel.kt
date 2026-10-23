package cl.duoc.reservasalas.app.ui.pantallas.salas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.coroutines.flow.StateFlow

class SalasViewModel(
    repositorioSalas: RepositorioSalas
) : ViewModel() {

    val salas: StateFlow<List<Sala>> = repositorioSalas.salas

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor =
                    (this[APPLICATION_KEY] as ReservaSalasApp).container

                SalasViewModel(
                    repositorioSalas = contenedor.repositorioSalas
                )
            }
        }
    }
}
