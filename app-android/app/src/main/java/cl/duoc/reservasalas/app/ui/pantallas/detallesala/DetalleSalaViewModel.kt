
package cl.duoc.reservasalas.app.ui.pantallas.detallesala

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.repository.RepositorioHorario
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.coroutines.flow.StateFlow

class DetalleSalaViewModel(
    private val repositorioSalas: RepositorioSalas,
    private val repositorioReservas: RepositorioReservas,
    private val repositorioHorario: RepositorioHorario
) : ViewModel() {

    val salas: StateFlow<List<Sala>> = repositorioSalas.salas

    val reservas: StateFlow<List<Reserva>> =
        repositorioReservas.reservas

    val horario: StateFlow<HorarioGeneral> =
        repositorioHorario.horario

    fun buscarSala(codigo: String): Sala? {
        return salas.value.find { it.codigo == codigo }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor =
                    (this[APPLICATION_KEY] as ReservaSalasApp).container

                DetalleSalaViewModel(
                    repositorioSalas = contenedor.repositorioSalas,
                    repositorioReservas = contenedor.repositorioReservas,
                    repositorioHorario = contenedor.repositorioHorario
                )
            }
        }
    }
}
