
package cl.duoc.reservasalas.app.ui.pantallas.reservaconfirmada

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoConfirmacion = Color(0xFFF2F3F5)
private val OscuroConfirmacion = Color(0xFF2D2927)
private val AmarilloConfirmacion = Color(0xFFFFB91F)
private val TextoConfirmacion = Color(0xFF302D2B)
private val SecundarioConfirmacion = Color(0xFF605D5A)
private val BordeConfirmacion = Color(0xFFDADADA)
private val VerdeConfirmacion = Color(0xFF2E7D32)
private val AmarilloSuave = Color(0xFFFFF2C9)

@Composable
fun ReservaConfirmadaPantalla(
    idReserva: Int,
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ReservaConfirmadaViewModel = viewModel(
        factory = ReservaConfirmadaViewModel.Factory
    )

    val reservas = viewModel.reservas.collectAsState().value
    val reserva = reservas.find { it.id == idReserva }

    LaunchedEffect(idReserva) {
        delay(5000)
        navegar(Rutas.SALAS)
    }

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d",
        Locale.forLanguageTag("es-CL")
    )

    val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoConfirmacion)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroConfirmacion)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Reserva confirmada",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloConfirmacion)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 26.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.size(112.dp),
                shape = CircleShape,
                color = VerdeConfirmacion
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 70.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Reserva confirmada!",
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextoConfirmacion,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (reserva == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(
                        1.dp,
                        BordeConfirmacion
                    )
                ) {
                    Text(
                        text = "No se encontró la información de la reserva",
                        modifier = Modifier.padding(20.dp),
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OscuroConfirmacion)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reserva N.º ${reserva.id}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AmarilloSuave
                        ) {
                            Text(
                                text = reserva.estado.texto,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 5.dp
                                ),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoConfirmacion
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                DatoConfirmacion(
                                    etiqueta = "Sala",
                                    valor = reserva.codigoSala
                                )
                            }

                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                DatoConfirmacion(
                                    etiqueta = "Fecha",
                                    valor = reserva.fecha
                                        .format(formatoFecha)
                                        .replaceFirstChar {
                                            it.uppercase()
                                        }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                DatoConfirmacion(
                                    etiqueta = "Horario",
                                    valor = "${reserva.horaInicio.format(formatoHora)}–${reserva.horaFin.format(formatoHora)}"
                                )
                            }

                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                DatoConfirmacion(
                                    etiqueta = "Bloques",
                                    valor = reserva.cantidadBloques.toString()
                                )
                            }
                        }

                        if (reserva.motivo.isNotBlank()) {
                            DatoConfirmacion(
                                etiqueta = "Motivo",
                                valor = reserva.motivo
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = AmarilloSuave,
                    border = BorderStroke(
                        1.dp,
                        AmarilloConfirmacion
                    )
                ) {
                    Text(
                        text = "Te recordaremos 15 minutos antes. Al llegar, haz check-in escaneando el código QR de la puerta de la sala.",
                        modifier = Modifier.padding(18.dp),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = TextoConfirmacion
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        navegar(Rutas.AGENDA)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloConfirmacion,
                        contentColor = TextoConfirmacion
                    )
                ) {
                    Text(
                        text = "Ver mi agenda",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        navegar(Rutas.INICIO_RELATOR)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        OscuroConfirmacion
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "Volver al inicio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoConfirmacion
                    )
                }
            }
        }
    }
}

@Composable
private fun DatoConfirmacion(
    etiqueta: String,
    valor: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = etiqueta,
            fontSize = 14.sp,
            color = SecundarioConfirmacion
        )

        Text(
            text = valor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextoConfirmacion
        )
    }
}
