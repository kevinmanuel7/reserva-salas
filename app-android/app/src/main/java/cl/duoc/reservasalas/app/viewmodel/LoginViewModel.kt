package cl.duoc.reservasalas.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.repository.AuthRepository
import cl.duoc.reservasalas.app.data.repository.ResultadoLogin
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** En qué momento del login está la pantalla. */
sealed interface EstadoLogin {
    data object Inicial : EstadoLogin
    data object Cargando : EstadoLogin
    data class Error(val mensaje: String) : EstadoLogin
    data class Exito(val usuario: Usuario) : EstadoLogin
}

/** Todo lo que la pantalla de login necesita para dibujarse. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val estado: EstadoLogin = EstadoLogin.Inicial
)

/**
 * Guarda el estado de la pantalla de login y llama a [AuthRepository].
 * La pantalla solo lee [uiState] y avisa acciones con las funciones públicas.
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Solo el ViewModel puede cambiar el estado (_uiState); la pantalla recibe
    // una versión de solo lectura (uiState).
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailCambio(texto: String) {
        _uiState.update { it.copy(email = texto, estado = estadoSinError(it.estado)) }
    }

    fun onPasswordCambio(texto: String) {
        _uiState.update { it.copy(password = texto, estado = estadoSinError(it.estado)) }
    }

    /** Al editar un campo, el mensaje de error anterior ya no corresponde: se borra. */
    private fun estadoSinError(estado: EstadoLogin): EstadoLogin =
        if (estado is EstadoLogin.Error) EstadoLogin.Inicial else estado

    fun iniciarSesion() {
        val actual = _uiState.value
        // Evita enviar dos logins a la vez.
        if (actual.estado is EstadoLogin.Cargando) return

        // El email se envía sin espacios al inicio ni al final; la contraseña, tal cual.
        val email = actual.email.trim()
        val password = actual.password

        if (email.isEmpty() || password.isBlank()) {
            _uiState.update { it.copy(estado = EstadoLogin.Error(MENSAJE_CAMPOS_VACIOS)) }
            return
        }

        _uiState.update { it.copy(estado = EstadoLogin.Cargando) }
        viewModelScope.launch {
            val nuevoEstado = when (val resultado = authRepository.login(email, password)) {
                is ResultadoLogin.Exito -> EstadoLogin.Exito(resultado.usuario)
                is ResultadoLogin.Error -> EstadoLogin.Error(resultado.mensaje)
            }
            _uiState.update { it.copy(estado = nuevoEstado) }
        }
    }

    companion object {
        const val MENSAJE_CAMPOS_VACIOS = "Ingresa tu email y contraseña"

        /**
         * Le dice a Android cómo crear este ViewModel: saca el AuthRepository
         * del AppContainer que vive en ReservaSalasApp (inyección manual, sin Hilt).
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReservaSalasApp
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}
