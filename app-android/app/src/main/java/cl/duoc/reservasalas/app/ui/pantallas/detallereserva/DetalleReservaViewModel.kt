
package cl.duoc.reservasalas.app.ui.pantallas.detallereserva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleReservaViewModel(
    private val repositorioReservas: RepositorioReservas,
    repositorioSalas: RepositorioSalas,
    private val reloj: Reloj
) : ViewModel() {

    val reservas: StateFlow<List<Reserva>> = repositorioReservas.reservas

    val salas: StateFlow<List<Sala>> = repositorioSalas.salas

    private val _cancelando = MutableStateFlow(false)
    val cancelando: StateFlow<Boolean> = _cancelando.asStateFlow()

    private val _errorCancelacion = MutableStateFlow<String?>(null)
    val errorCancelacion: StateFlow<String?> = _errorCancelacion.asStateFlow()

    fun sePuedeCancelar(reserva: Reserva): Boolean =
        reserva.sePuedeCancelar(reloj.ahora())

    fun checkInHabilitado(reserva: Reserva): Boolean =
        reserva.checkInHabilitado(reloj.ahora())

    fun cancelar(
        idReserva: Int,
        alCancelar: () -> Unit
    ) {
        if (_cancelando.value) return

        viewModelScope.launch {
            _cancelando.value = true
            _errorCancelacion.value = null

            try {
                repositorioReservas.cancelar(idReserva)
                alCancelar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorCancelacion.value =
                    e.message ?: "No se pudo cancelar la reserva"
            } finally {
                _cancelando.value = false
            }
        }
    }

    fun limpiarErrorCancelacion() {
        _errorCancelacion.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor =
                    (this[APPLICATION_KEY] as ReservaSalasApp).container

                DetalleReservaViewModel(
                    repositorioReservas = contenedor.repositorioReservas,
                    repositorioSalas = contenedor.repositorioSalas,
                    reloj = contenedor.reloj
                )
            }
        }
    }
}
