package cl.duoc.reservasalas.app.ui.pantallas.reservar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.modelo.bloques
import cl.duoc.reservasalas.app.data.repository.RepositorioHorario
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import cl.duoc.reservasalas.app.data.repository.ResultadoReserva
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReservarViewModel(
    private val repositorioSalas: RepositorioSalas,
    private val repositorioHorario: RepositorioHorario,
    private val repositorioReservas: RepositorioReservas,
    private val sesionDataStore: SesionDataStore,
    val reloj: Reloj
) : ViewModel() {

    val salas: StateFlow<List<Sala>> = repositorioSalas.salas

    val horario: StateFlow<HorarioGeneral> = repositorioHorario.horario

    val reservas: StateFlow<List<Reserva>> = repositorioReservas.reservas

    fun obtenerBloques(sala: Sala, horarioGeneral: HorarioGeneral): List<Bloque> {
        return sala.bloques(horarioGeneral)
    }

    fun crearReserva(
        codigoSala: String,
        fecha: LocalDate,
        bloques: List<Bloque>,
        motivo: String,
        alTerminar: (ResultadoReserva) -> Unit
    ) {
        viewModelScope.launch {
            val sesion = sesionDataStore.leerSesion()

            if (sesion == null) {
                alTerminar(
                    ResultadoReserva.Error("Debes iniciar sesión para reservar")
                )
                return@launch
            }

            val resultado = repositorioReservas.crear(
                codigoSala = codigoSala,
                usuario = sesion.usuario,
                fecha = fecha,
                bloques = bloques,
                motivo = motivo
            )

            alTerminar(resultado)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor =
                    (this[APPLICATION_KEY] as ReservaSalasApp).container

                ReservarViewModel(
                    repositorioSalas = contenedor.repositorioSalas,
                    repositorioHorario = contenedor.repositorioHorario,
                    repositorioReservas = contenedor.repositorioReservas,
                    sesionDataStore = contenedor.sesionDataStore,
                    reloj = contenedor.reloj
                )
            }
        }
    }
}
