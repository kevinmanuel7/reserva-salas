
package cl.duoc.reservasalas.app.ui.pantallas.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AgendaViewModel(
    private val repositorioReservas: RepositorioReservas,
    private val repositorioSalas: RepositorioSalas,
    private val sesionDataStore: SesionDataStore
) : ViewModel() {

    val reservas: StateFlow<List<Reserva>> = repositorioReservas.reservas

    val salas: StateFlow<List<Sala>> = repositorioSalas.salas

    private val _idUsuario = MutableStateFlow<Int?>(null)
    val idUsuario: StateFlow<Int?> = _idUsuario

    init {
        viewModelScope.launch {
            val sesion = sesionDataStore.leerSesion()
            _idUsuario.value = sesion?.usuario?.id
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor = (this[APPLICATION_KEY] as ReservaSalasApp).container

                AgendaViewModel(
                    repositorioReservas = contenedor.repositorioReservas,
                    repositorioSalas = contenedor.repositorioSalas,
                    sesionDataStore = contenedor.sesionDataStore
                )
            }
        }
    }
}
