package cl.duoc.reservasalas.app.ui.pantallas.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AgendaViewModel(
    repositorioReservas: RepositorioReservas,
    repositorioSalas: RepositorioSalas,
    sesionDataStore: SesionDataStore
) : ViewModel() {

    val reservas: StateFlow<List<Reserva>> = repositorioReservas.reservas

    val salas: StateFlow<List<Sala>> = repositorioSalas.salas


    val idUsuario: StateFlow<Int?> = sesionDataStore.sesion
        .map { sesion -> sesion?.usuario?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

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