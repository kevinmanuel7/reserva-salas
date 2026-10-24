
package cl.duoc.reservasalas.app.ui.pantallas.iniciorelator

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
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

import java.time.LocalDate

data class InicioRelatorUiState(
    val nombre: String = "Relator",
    val fecha: LocalDate = LocalDate.now(),
    val proximaReserva: Reserva? = null,
    val reservasSemana: List<Reserva> = emptyList(),
    val recordatorio: Reserva? = null,
    val cargando: Boolean = true
)

class InicioRelatorViewModel(
    sesionDataStore: SesionDataStore,
    repositorioReservas: RepositorioReservas,
    val reloj: Reloj
) : ViewModel() {

    val uiState: StateFlow<InicioRelatorUiState> =
        combine(
            sesionDataStore.sesion,
            repositorioReservas.reservas
        ) { sesion, reservas ->

            val ahora = reloj.ahora()
            val usuario = sesion?.usuario

            val misReservas = reservas
                .filter {
                    it.idUsuario == usuario?.id &&
                            it.estado != EstadoReserva.CANCELADA
                }
                .sortedBy { it.inicio }

            val proximas = misReservas.filter {
                it.inicio >= ahora &&
                        it.estado == EstadoReserva.RESERVADA
            }

            InicioRelatorUiState(
                nombre = usuario?.nombre
                    ?.substringBefore(" ")
                    ?: "Relator",

                fecha = reloj.hoy(),

                proximaReserva = proximas.firstOrNull(),

                reservasSemana = proximas
                    .filter {
                        !it.fecha.isAfter(
                            reloj.hoy().plusDays(7)
                        )
                    }
                    .take(3),

                recordatorio = proximas.firstOrNull {
                    it.mostrarRecordatorio(ahora)
                },

                cargando = false
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            InicioRelatorUiState()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor =
                    (this[APPLICATION_KEY] as ReservaSalasApp)
                        .container

                InicioRelatorViewModel(
                    contenedor.sesionDataStore,
                    contenedor.repositorioReservas,
                    contenedor.reloj
                )
            }
        }
    }
}
