
package cl.duoc.reservasalas.app.ui.pantallas.iniciorelator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Fondo = Color(0xFFF3F4F6)
private val Oscuro = Color(0xFF2D2927)
private val Amarillo = Color(0xFFFFB91F)
private val AmarilloClaro = Color(0xFFFFF2C9)
private val TextoPrincipal = Color(0xFF302D2B)
private val TextoSecundario = Color(0xFF605D5A)
private val Borde = Color(0xFFDADADA)
private val FormaTarjeta = RoundedCornerShape(14.dp)

@Composable
fun InicioRelatorPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: InicioRelatorViewModel = viewModel(
        factory = InicioRelatorViewModel.Factory
    )

    val estado = viewModel.uiState.collectAsState().value

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM",
        Locale("es", "CL")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Oscuro)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFF99918D)
                    )
                ) {
                    Text(
                        text = "Logo Duoc UC",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 14.dp
                        )
                    )
                }

                Text(
                    text = "Inicio",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Amarillo)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Hola, ${estado.nombre}",
                    color = TextoPrincipal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 29.sp,
                    lineHeight = 34.sp
                )

                Text(
                    text = estado.fecha
                        .format(formatoFecha)
                        .replaceFirstChar { it.uppercase() },
                    color = TextoSecundario,
                    fontSize = 17.sp
                )
            }

            estado.recordatorio?.let { reserva ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navegar(Rutas.reserva(reserva.id))
                        },
                    shape = FormaTarjeta,
                    color = AmarilloClaro,
                    border = BorderStroke(1.dp, Amarillo)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "♧",
                            color = TextoPrincipal,
                            fontSize = 30.sp
                        )

                        Column {
                            Text(
                                text = "Recordatorio:",
                                color = TextoPrincipal,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Tu reserva en ${reserva.codigoSala} comienza a las ${
                                    reserva.horario.substringBefore("–")
                                }.",
                                color = TextoPrincipal,
                                fontSize = 16.sp,
                                lineHeight = 23.sp
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = FormaTarjeta,
                color = Color.White,
                border = BorderStroke(1.dp, Borde)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Oscuro)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Tu próxima reserva",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (estado.cargando) {
                            CircularProgressIndicator(
                                color = Amarillo
                            )
                            Text("Cargando reservas...")
                        } else {
                            val reserva = estado.proximaReserva

                            if (reserva == null) {
                                Text(
                                    text = "No tienes reservas próximas",
                                    color = TextoSecundario,
                                    fontSize = 16.sp
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reserva.codigoSala,
                                        color = TextoPrincipal,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        color = AmarilloClaro,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = reserva.estado.texto,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextoPrincipal,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = reserva.fecha
                                        .format(formatoFecha)
                                        .replaceFirstChar { it.uppercase() },
                                    color = TextoSecundario,
                                    fontSize = 15.sp
                                )

                                Text(
                                    text = reserva.horario,
                                    color = TextoPrincipal,
                                    fontSize = 18.sp
                                )

                                if (
                                    reserva.checkInHabilitado(
                                        viewModel.reloj.ahora()
                                    )
                                ) {
                                    Button(
                                        onClick = {
                                            navegar(Rutas.checkin(reserva.id))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        shape = RoundedCornerShape(11.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Amarillo,
                                            contentColor = TextoPrincipal
                                        )
                                    ) {
                                        Text(
                                            text = "Hacer check-in",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            navegar(Rutas.reserva(reserva.id))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(11.dp),
                                        border = BorderStroke(1.dp, Amarillo)
                                    ) {
                                        Text(
                                            text = "Ver detalle de reserva",
                                            color = TextoPrincipal,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { navegar(Rutas.SALAS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amarillo,
                    contentColor = TextoPrincipal
                )
            ) {
                Text(
                    text = "+  Reservar sala",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Esta semana",
                    color = TextoPrincipal,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Ver todas",
                    color = TextoPrincipal,
                    fontSize = 15.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navegar(Rutas.AGENDA)
                    }
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = FormaTarjeta,
                border = BorderStroke(1.dp, Borde)
            ) {
                Column {
                    if (estado.reservasSemana.isEmpty()) {
                        Text(
                            text = "No tienes reservas próximas esta semana",
                            color = TextoSecundario,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(18.dp)
                        )
                    } else {
                        estado.reservasSemana.forEachIndexed { indice, reserva ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navegar(Rutas.reserva(reserva.id))
                                    }
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 17.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = reserva.codigoSala,
                                    color = TextoPrincipal,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "· ${
                                        etiquetaFecha(
                                            reserva.fecha,
                                            estado.fecha
                                        )
                                    }",
                                    color = TextoSecundario,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = reserva.horario,
                                    color = TextoPrincipal,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }

                            if (indice < estado.reservasSemana.lastIndex) {
                                HorizontalDivider(
                                    color = Borde,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun etiquetaFecha(
    fecha: LocalDate,
    hoy: LocalDate
): String {
    return when (fecha) {
        hoy -> "Hoy"
        hoy.plusDays(1) -> "Mañana"
        else -> fecha.format(
            DateTimeFormatter.ofPattern(
                "EEEE d",
                Locale("es", "CL")
            )
        ).replaceFirstChar { it.uppercase() }
    }
}
