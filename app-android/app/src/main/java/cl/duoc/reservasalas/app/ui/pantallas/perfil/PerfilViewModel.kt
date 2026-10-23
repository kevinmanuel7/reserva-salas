package cl.duoc.reservasalas.app.ui.pantallas.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Todo lo que el Perfil necesita para dibujarse. */
data class PerfilUiState(
    val usuario: Usuario? = null,
    val reservasEsteMes: Int = 0,
    val checkInRealizados: Int = 0
)

class PerfilViewModel(
    private val sesionDataStore: SesionDataStore,
    repositorioReservas: RepositorioReservas,
    reloj: Reloj
) : ViewModel() {

    /**
     * combine junta dos Flow: cada vez que cambia la sesión o las reservas,
     * se vuelve a calcular el estado de la pantalla.
     */
    val uiState: StateFlow<PerfilUiState> =
        combine(sesionDataStore.sesion, repositorioReservas.reservas) { sesion, reservas ->
            val usuario = sesion?.usuario
            val hoy = reloj.hoy()
            val mias = reservas.filter { it.idUsuario == usuario?.id }
            PerfilUiState(
                usuario = usuario,
                reservasEsteMes = mias.count {
                    it.fecha.year == hoy.year &&
                        it.fecha.month == hoy.month &&
                        it.estado != EstadoReserva.CANCELADA
                },
                checkInRealizados = mias.count { it.horaCheckIn != null }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilUiState())

    /** Borra la sesión del teléfono y, cuando termina, avisa con [alTerminar]. */
    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            sesionDataStore.borrarSesion()
            alTerminar()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor = (this[APPLICATION_KEY] as ReservaSalasApp).container
                PerfilViewModel(
                    contenedor.sesionDataStore,
                    contenedor.repositorioReservas,
                    contenedor.reloj
                )
            }
        }
    }
}
