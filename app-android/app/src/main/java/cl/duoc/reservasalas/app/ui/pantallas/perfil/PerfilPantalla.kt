
package cl.duoc.reservasalas.app.ui.pantallas.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme

private val FondoPerfil = Color(0xFFF2F3F5)
private val OscuroPerfil = Color(0xFF2D2927)
private val AmarilloPerfil = Color(0xFFFFB91F)
private val TextoPerfil = Color(0xFF302D2B)
private val SecundarioPerfil = Color(0xFF605D5A)
private val BordePerfil = Color(0xFFDADADA)
private val RojoPerfil = Color(0xFFD32F2F)

@Composable
fun PerfilPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(
        factory = PerfilViewModel.Factory
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var confirmarCierre by rememberSaveable {
        mutableStateOf(false)
    }

    PerfilContenido(
        uiState = uiState,
        alAbrirHorario = {
            navegar(Rutas.HORARIO_GENERAL)
        },
        alCerrarSesion = {
            confirmarCierre = true
        },
        modifier = modifier
    )

    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = {
                confirmarCierre = false
            },
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Seguro que quieres cerrar sesión?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarCierre = false
                        viewModel.cerrarSesion(
                            alTerminar = {
                                navegar(Rutas.LOGIN)
                            }
                        )
                    }
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = RojoPerfil,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarCierre = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextoPerfil
                    )
                }
            }
        )
    }
}

@Composable
fun PerfilContenido(
    uiState: PerfilUiState,
    alAbrirHorario: () -> Unit,
    alCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usuario = uiState.usuario

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoPerfil)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroPerfil)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mi perfil",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloPerfil)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (usuario != null) {
                TarjetaUsuario(usuario)
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        BordePerfil
                    )
                ) {
                    Text(
                        text = "Cargando información del usuario...",
                        modifier = Modifier.padding(20.dp),
                        color = SecundarioPerfil
                    )
                }
            }

            Text(
                text = "Mi actividad",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPerfil
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaEstadisticaPerfil(
                    numero = uiState.reservasEsteMes.toString(),
                    descripcion = "Reservas este mes",
                    modifier = Modifier.weight(1f)
                )

                TarjetaEstadisticaPerfil(
                    numero = uiState.checkInRealizados.toString(),
                    descripcion = "Check-in realizados",
                    modifier = Modifier.weight(1f)
                )
            }

            if (usuario?.rol == Rol.COORDINADOR) {
                Text(
                    text = "Administración",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPerfil
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            alAbrirHorario()
                        },
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        BordePerfil
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    AmarilloPerfil.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▦",
                                color = TextoPerfil,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Horario general de bloques",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoPerfil
                            )

                            Text(
                                text = "Configurar horarios de las salas",
                                fontSize = 13.sp,
                                color = SecundarioPerfil
                            )
                        }

                        Text(
                            text = "›",
                            fontSize = 28.sp,
                            color = SecundarioPerfil
                        )
                    }
                }
            }

            Text(
                text = "Cuenta",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPerfil
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    BordePerfil
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Sesión actual",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPerfil
                    )

                    Text(
                        text = "Puedes cerrar tu sesión cuando termines de utilizar la aplicación.",
                        fontSize = 14.sp,
                        color = SecundarioPerfil,
                        lineHeight = 20.sp
                    )

                    OutlinedButton(
                        onClick = alCerrarSesion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            RojoPerfil
                        )
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            color = RojoPerfil,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Sede Viña del Mar · Datos ficticios con fines académicos",
                fontSize = 12.sp,
                color = SecundarioPerfil,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: Usuario
) {
    val iniciales = usuario.nombre
        .split(" ")
        .filter {
            it.isNotBlank()
        }
        .take(2)
        .joinToString("") {
            it.first().uppercase()
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            BordePerfil
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            AmarilloPerfil,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciales,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextoPerfil
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = usuario.nombre,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPerfil
                    )

                    Text(
                        text = usuario.email,
                        fontSize = 13.sp,
                        color = SecundarioPerfil
                    )
                }
            }

            HorizontalDivider(
                color = BordePerfil
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tipo de usuario",
                    fontSize = 14.sp,
                    color = SecundarioPerfil
                )

                Surface(
                    color = AmarilloPerfil.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = when (usuario.rol) {
                            Rol.COORDINADOR -> "Coordinador"
                            else -> "Relator"
                        },
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 7.dp
                        ),
                        color = TextoPerfil,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaEstadisticaPerfil(
    numero: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            BordePerfil
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 22.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = numero,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextoPerfil
            )

            Text(
                text = descripcion,
                fontSize = 13.sp,
                color = SecundarioPerfil,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun PerfilCoordinadorPreview() {
    ReservaSalasTheme {
        PerfilContenido(
            uiState = PerfilUiState(
                usuario = Usuario(
                    1,
                    "Coordinador Ficticio Uno",
                    "coordinador1@duocuc.test",
                    Rol.COORDINADOR
                ),
                reservasEsteMes = 3,
                checkInRealizados = 2
            ),
            alAbrirHorario = {},
            alCerrarSesion = {}
        )
    }
}
