package cl.duoc.reservasalas.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.viewmodel.EstadoLogin
import cl.duoc.reservasalas.app.viewmodel.LoginUiState
import cl.duoc.reservasalas.app.viewmodel.LoginViewModel

/**
 * Pantalla 1 del contrato de pantallas (plan, sección 6): Login.
 * Conecta el ViewModel con [LoginContenido], que es el que dibuja.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    // Cada vez que el ViewModel cambia el estado, uiState cambia y la pantalla se vuelve a dibujar.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContenido(
        uiState = uiState,
        onEmailCambio = viewModel::onEmailCambio,
        onPasswordCambio = viewModel::onPasswordCambio,
        onIniciarSesion = viewModel::iniciarSesion,
        modifier = modifier
    )
}

/** Dibuja la pantalla a partir del estado. No conoce al ViewModel, por eso sirve en el Preview. */
@Composable
fun LoginContenido(
    uiState: LoginUiState,
    onEmailCambio: (String) -> Unit,
    onPasswordCambio: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cargando = uiState.estado is EstadoLogin.Cargando

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Reserva de salas", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailCambio,
            label = { Text("Email") },
            singleLine = true,
            enabled = !cargando,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordCambio,
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onIniciarSesion,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        when (val estado = uiState.estado) {
            EstadoLogin.Inicial -> Unit
            EstadoLogin.Cargando -> CircularProgressIndicator()
            is EstadoLogin.Error -> Text(
                text = estado.mensaje,
                color = MaterialTheme.colorScheme.error
            )
            // Temporal: sin navegación todavía, se muestra el usuario aquí mismo.
            is EstadoLogin.Exito -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bienvenido, ${estado.usuario.nombre}")
                Text("Rol: ${estado.usuario.rol.name}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContenidoInicialPreview() {
    ReservaSalasTheme {
        LoginContenido(LoginUiState(), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContenidoErrorPreview() {
    ReservaSalasTheme {
        LoginContenido(
            LoginUiState(
                email = "relator1@duocuc.test",
                estado = EstadoLogin.Error("Email o contraseña incorrectos")
            ),
            {}, {}, {}
        )
    }
}
