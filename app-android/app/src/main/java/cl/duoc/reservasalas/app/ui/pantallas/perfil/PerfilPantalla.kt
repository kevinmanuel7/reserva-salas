package cl.duoc.reservasalas.app.ui.pantallas.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.R
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import cl.duoc.reservasalas.app.ui.componentes.BarraSuperior
import cl.duoc.reservasalas.app.ui.componentes.BotonPeligro
import cl.duoc.reservasalas.app.ui.componentes.ChipRol
import cl.duoc.reservasalas.app.ui.componentes.FormaTarjeta
import cl.duoc.reservasalas.app.ui.componentes.TarjetaMetrica
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.Fondo
import cl.duoc.reservasalas.app.ui.theme.Gris85
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.RojoError
import cl.duoc.reservasalas.app.ui.theme.Superficie

/**
 * Perfil (ruta perfil). Dueño: Kevin. Imagen: docs/maqueta/pantallas/kevin/02-Perfil.png
 * Conecta el ViewModel con [PerfilContenido] y maneja el diálogo de cerrar sesión.
 */
@Composable
fun PerfilPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // rememberSaveable: el diálogo sigue abierto aunque se gire la pantalla.
    var confirmarCierre by rememberSaveable { mutableStateOf(false) }

    PerfilContenido(
        uiState = uiState,
        alAbrirHorario = { navegar(Rutas.HORARIO_GENERAL) },
        alCerrarSesion = { confirmarCierre = true },
        modifier = modifier
    )

    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCierre = false
                    viewModel.cerrarSesion(alTerminar = { navegar(Rutas.LOGIN) })
                }) {
                    Text("Cerrar sesión", color = RojoError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierre = false }) {
                    Text("Cancelar", color = NegroDuoc)
                }
            }
        )
    }
}

/** Dibuja el perfil a partir del estado. No conoce al ViewModel, por eso sirve en el Preview. */
@Composable
fun PerfilContenido(
    uiState: PerfilUiState,
    alAbrirHorario: () -> Unit,
    alCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usuario = uiState.usuario

    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior(titulo = "Perfil")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (usuario != null) {
                TarjetaUsuario(usuario)
            }

            Text("Mi actividad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TarjetaMetrica(
                    numero = uiState.reservasEsteMes.toString(),
                    descripcion = "reservas este mes",
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    numero = uiState.checkInRealizados.toString(),
                    descripcion = "check-in realizados",
                    modifier = Modifier.weight(1f)
                )
            }

            // Solo el coordinador edita el horario general.
            if (usuario?.rol == Rol.COORDINADOR) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie, FormaTarjeta)
                        .border(1.dp, Borde, FormaTarjeta)
                        .clickable(onClick = alAbrirHorario)
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_agenda),
                        contentDescription = null,
                        tint = NegroDuoc,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Horario general de bloques",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NegroDuoc,
                        modifier = Modifier.weight(1f)
                    )
                    Text("›", fontSize = 22.sp, color = Gris85)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Sede Viña del Mar · Datos ficticios con fines académicos",
                fontSize = 12.sp,
                color = Gris85,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            BotonPeligro(
                texto = "Cerrar sesión",
                alPresionar = alCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Círculo con iniciales, nombre, correo y rol. */
@Composable
private fun TarjetaUsuario(usuario: Usuario) {
    // Ej. "Relator Ficticio Uno" → "RF".
    val iniciales = usuario.nombre
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Superficie, FormaTarjeta)
            .border(1.dp, Borde, FormaTarjeta)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(AmarilloDuoc, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(iniciales, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = NegroDuoc)
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(usuario.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)
            Text(usuario.email, fontSize = 13.sp, color = Gris85)
            ChipRol(usuario.rol)
        }
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun PerfilCoordinadorPreview() {
    ReservaSalasTheme {
        PerfilContenido(
            uiState = PerfilUiState(
                usuario = Usuario(1, "Coordinador Ficticio Uno", "coordinador1@duocuc.test", Rol.COORDINADOR),
                reservasEsteMes = 3,
                checkInRealizados = 2
            ),
            alAbrirHorario = {},
            alCerrarSesion = {}
        )
    }
}
