package cl.duoc.reservasalas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.model.Usuario
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.componentes.LogoDuoc
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.Blanco
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.Fondo
import cl.duoc.reservasalas.app.ui.theme.Gris85
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.RojoError
import cl.duoc.reservasalas.app.ui.theme.Superficie
import cl.duoc.reservasalas.app.viewmodel.EstadoLogin
import cl.duoc.reservasalas.app.viewmodel.LoginUiState
import cl.duoc.reservasalas.app.viewmodel.LoginViewModel

/**
 * Login (ruta login). Dueño: Kevin. Imagen: docs/maqueta/pantallas/kevin/01-Login.png
 * Conecta el ViewModel con [LoginContenido], que es el que dibuja.
 *
 * [alIniciarSesion] se llama una vez cuando el login sale bien: la navegación
 * lo usa para ir al inicio que corresponde al rol del usuario.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    alIniciarSesion: (Usuario) -> Unit = {}
) {
    // Cada vez que el ViewModel cambia el estado, uiState cambia y la pantalla se vuelve a dibujar.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // LaunchedEffect corre su bloque cuando cambia "estado". Si es Exito, avisa una sola vez.
    val estado = uiState.estado
    LaunchedEffect(estado) {
        if (estado is EstadoLogin.Exito) alIniciarSesion(estado.usuario)
    }

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
    // Estado propio de la pantalla (no del ViewModel): si la contraseña se ve o no.
    var verPassword by rememberSaveable { mutableStateOf(false) }

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NegroDuoc,
        focusedLabelColor = NegroDuoc,
        cursorColor = NegroDuoc,
        unfocusedBorderColor = Borde
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera negra con el logo.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NegroDuoc)
                .statusBarsPadding()
                .padding(top = 48.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LogoDuoc(grande = true)
            Text("Reserva de Salas", color = Blanco, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Sede Viña del Mar", color = AmarilloDuoc, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(AmarilloDuoc)
        )

        // Tarjeta blanca con el formulario.
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .background(Superficie, RoundedCornerShape(16.dp))
                .border(1.dp, Borde, RoundedCornerShape(16.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Iniciar sesión", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailCambio,
                label = { Text("Correo institucional") },
                singleLine = true,
                enabled = !cargando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = coloresCampo,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordCambio,
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !cargando,
                visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { verPassword = !verPassword }) {
                        Text(if (verPassword) "Ocultar" else "Mostrar", color = Gris85)
                    }
                },
                colors = coloresCampo,
                modifier = Modifier.fillMaxWidth()
            )

            val estado = uiState.estado
            if (estado is EstadoLogin.Error) {
                Text(text = estado.mensaje, color = RojoError, fontSize = 14.sp)
            }

            BotonPrincipal(
                texto = "Ingresar",
                alPresionar = onIniciarSesion,
                cargando = cargando,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(
            text = "Datos ficticios con fines académicos",
            color = Gris85,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun LoginContenidoInicialPreview() {
    ReservaSalasTheme {
        LoginContenido(LoginUiState(), {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 760)
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
