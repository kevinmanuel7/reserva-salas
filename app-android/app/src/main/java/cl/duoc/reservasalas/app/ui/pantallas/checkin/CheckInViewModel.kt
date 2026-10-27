package cl.duoc.reservasalas.app.ui.pantallas.checkin

import cl.duoc.reservasalas.app.data.modelo.Reserva
import java.time.LocalTime
import android.content.Context
import androidx.lifecycle.ViewModel
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp

sealed interface EstadoCheckIn {
    data object Esperando : EstadoCheckIn
    data object Procesando : EstadoCheckIn

    data class Error(val mensaje: String) : EstadoCheckIn
    data class Exito(val hora: LocalTime) : EstadoCheckIn
}

data class CheckInUiState(
    val reserva: Reserva? = null,
    val codigoManual: String = "",
    val estado: EstadoCheckIn = EstadoCheckIn.Esperando
)

class CheckInViewModel(
    private val idReserva: Int,
    private val repositorioReservas: RepositorioReservas,
    private val reloj: Reloj,
    private val contexto: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        val reserva = repositorioReservas.buscar(idReserva)
        _uiState.update {estadoActual -> estadoActual.copy(reserva = reserva)}
    }

    fun crearArchivoFoto(): Uri {
        val carpeta = File(contexto.cacheDir, "fotos")
        if (!carpeta.exists()) {
            carpeta.mkdirs()
        }
        val archivo = File(carpeta, "checkin-${System.currentTimeMillis()}.jpg")
        archivo.createNewFile()
        return FileProvider.getUriForFile(contexto, AUTORIDAD_FILE_PROVIDER, archivo)
    }

    private fun validarYRegistrar(textoLeido: String) {
        val reserva = _uiState.value.reserva
        if (reserva == null) {
            mostrarError("No se encontró la reserva.")
            return
        }

        val codigoLeido = textoLeido.trim().removePrefix(PREFIJO_QR).uppercase()
        if (codigoLeido != reserva.codigoSala) {
            mostrarError("Este código es de la sala $codigoLeido. Tu reserva es en ${reserva.codigoSala}.")
            return
        }

        val ahora = reloj.ahora()
        if (!reserva.checkInHabilitado(ahora)) {
            mostrarError("El check-in se puede hacer desde 10 minutos antes hasta 15 minutos después del inicio.")
            return
        }

        viewModelScope.launch {
            repositorioReservas.registrarCheckIn(reserva.id, ahora.toLocalTime())
            _uiState.update { estadoActual ->
                estadoActual.copy(estado = EstadoCheckIn.Exito(ahora.toLocalTime()))
            }
        }
    }

    fun mostrarError(mensaje: String) {
        _uiState.update { estadoActual -> estadoActual.copy(estado = EstadoCheckIn.Error(mensaje)) }
    }

    fun procesarFoto(uri: Uri) {
        _uiState.update { estadoActual -> estadoActual.copy(estado = EstadoCheckIn.Procesando) }

        val imagen = try {
            InputImage.fromFilePath(contexto, uri)
        } catch (e: IOException) {
            mostrarError("No se pudo abrir la foto. Intenta de nuevo.")
            return
        }

        val opciones = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val lector = BarcodeScanning.getClient(opciones)

        lector.process(imagen)
            .addOnSuccessListener { codigos ->
                val texto = codigos.firstOrNull()?.rawValue
                if (texto == null) {
                    mostrarError("No se encontró un código QR. Intenta de nuevo con buena luz.")
                } else {
                    validarYRegistrar(texto)
                }
            }
            .addOnFailureListener {
                mostrarError("No se pudo leer la foto. Intenta de nuevo.")
            }
    }

    fun onCodigoManualCambio(texto: String) {
        _uiState.update { estadoActual ->
            val estadoNuevo = if (estadoActual.estado is EstadoCheckIn.Error) EstadoCheckIn.Esperando else estadoActual.estado
            estadoActual.copy(codigoManual = texto, estado = estadoNuevo)
        }
    }

    fun confirmarCodigoManual() {
        validarYRegistrar(_uiState.value.codigoManual)
    }

    companion object {
        const val AUTORIDAD_FILE_PROVIDER = "cl.duoc.reservasalas.app.fileprovider"
        const val PREFIJO_QR = "SALA:"

        fun factory(idReserva: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReservaSalasApp
                CheckInViewModel(
                    idReserva = idReserva,
                    repositorioReservas = app.container.repositorioReservas,
                    reloj = app.container.reloj,
                    contexto = app
                )
            }
        }
    }
}

