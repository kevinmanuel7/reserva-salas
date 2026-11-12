
package cl.duoc.reservasalas.app.ui.pantallas.supervisar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoSupervisar = Color(0xFFF2F3F5)
private val OscuroSupervisar = Color(0xFF2D2927)
private val AmarilloSupervisar = Color(0xFFFFB91F)
private val TextoSupervisar = Color(0xFF302D2B)
private val GrisSupervisar = Color(0xFF605D5A)
private val BordeSupervisar = Color(0xFFDADADA)
private val VerdeSupervisar = Color(0xFF2E7D32)
private val RojoSupervisar = Color(0xFFD32F2F)

@Composable
fun SupervisarPantalla(
    idReserva: Int,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as ReservaSalasApp
    val repo = app.container.repositorioReservas
    val reservas by repo.reservas.collectAsState()
    val salas by app.container.repositorioSalas.salas.collectAsState()

    val reserva = reservas.find { it.id == idReserva }
    val sala = salas.find { it.codigo == reserva?.codigoSala }

    val scope = rememberCoroutineScope()

    var confirmarAusencia by remember {
        mutableStateOf(false)
    }

    val ahora = app.container.reloj.ahora()

    val sinCheckin = reserva != null &&
            reserva.estado == EstadoReserva.RESERVADA &&
            ahora.toLocalDate() == reserva.fecha &&
            ahora.toLocalTime().isAfter(
                reserva.horaInicio.plusMinutes(15)
            )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoSupervisar)
    ) {
        EncabezadoSupervisar(volver = volver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (reserva == null) {
                Text(
                    text = "No se encontró la reserva",
                    color = TextoSupervisar,
                    fontSize = 17.sp
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BordeSupervisar)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = reserva.codigoSala,
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoSupervisar
                                )

                                val fecha = if (
                                    reserva.fecha == ahora.toLocalDate()
                                ) {
                                    "Hoy"
                                } else {
                                    reserva.fecha.format(
                                        DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy"
                                        )
                                    )
                                }

                                Text(
                                    text = "${sala?.nombre ?: "Sala"} · $fecha ${reserva.horaInicio}–${reserva.horaFin}",
                                    fontSize = 14.sp,
                                    color = GrisSupervisar
                                )
                            }

                            EstadoSupervisar(
                                estado = reserva.estado
                            )
                        }

                        HorizontalDivider(
                            color = BordeSupervisar
                        )

                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Relator",
                                    fontSize = 14.sp,
                                    color = GrisSupervisar
                                )

                                Text(
                                    text = reserva.nombreUsuario,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoSupervisar
                                )

                                Text(
                                    text = reserva.emailUsuario,
                                    fontSize = 14.sp,
                                    color = GrisSupervisar
                                )
                            }

                            Column {
                                Text(
                                    text = "Motivo",
                                    fontSize = 14.sp,
                                    color = GrisSupervisar
                                )

                                Text(
                                    text = reserva.motivo.ifBlank {
                                        "Sin motivo especificado"
                                    },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoSupervisar
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Seguimiento",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoSupervisar
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BordeSupervisar)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        val tieneCheckIn = reserva.horaCheckIn != null

                        val estaVerificada =
                            reserva.estado == EstadoReserva.VERIFICADA

                        PasoSeguimiento(
                            titulo = "Reservada",
                            descripcion = formatearCreacionSupervisar(
                                reserva.creada.toString()
                            ),
                            color = OscuroSupervisar,
                            tipo = 0,
                            mostrarLinea = true
                        )

                        PasoSeguimiento(
                            titulo = "Check-in con QR",
                            descripcion = if (tieneCheckIn) {
                                val fechaCheckIn = if (
                                    reserva.fecha == ahora.toLocalDate()
                                ) {
                                    "Hoy"
                                } else {
                                    reserva.fecha.format(
                                        DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy"
                                        )
                                    )
                                }

                                "$fechaCheckIn, ${reserva.horaCheckIn}"
                            } else {
                                "Pendiente de check-in"
                            },
                            color = if (tieneCheckIn) {
                                VerdeSupervisar
                            } else {
                                Color(0xFF909090)
                            },
                            tipo = if (tieneCheckIn) 0 else 1,
                            mostrarLinea = true
                        )

                        PasoSeguimiento(
                            titulo = "Verificada",
                            descripcion = if (estaVerificada) {
                                "Confirmada por coordinación"
                            } else {
                                "Pendiente: ir a la sala a confirmar"
                            },
                            color = if (estaVerificada) {
                                VerdeSupervisar
                            } else {
                                Color(0xFF909090)
                            },
                            tipo = if (estaVerificada) 0 else 1,
                            mostrarLinea = false
                        )
                    }
                }

                if (sinCheckin) {
                    Text(
                        text = "Esta reserva no tiene check-in después del plazo permitido. Puedes marcar al relator como no presentado. La sala no se libera automáticamente.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = GrisSupervisar
                    )
                }
            }
        }

        if (reserva != null &&
            (
                    reserva.estado == EstadoReserva.EN_USO ||
                            sinCheckin
                    )
        ) {
            HorizontalDivider(
                color = BordeSupervisar
            )

            Surface(
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (reserva.estado == EstadoReserva.EN_USO) {
                        Button(
                            onClick = {
                                scope.launch {
                                    repo.marcarVerificada(idReserva)
                                    volver()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmarilloSupervisar,
                                contentColor = TextoSupervisar
                            )
                        ) {
                            Text(
                                text = "Marcar como verificada",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (sinCheckin) {
                        OutlinedButton(
                            onClick = {
                                confirmarAusencia = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(11.dp),
                            border = BorderStroke(
                                1.dp,
                                RojoSupervisar
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RojoSupervisar
                            )
                        ) {
                            Text(
                                text = "Marcar no presentado",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmarAusencia) {
        AlertDialog(
            onDismissRequest = {
                confirmarAusencia = false
            },
            title = {
                Text("¿Marcar no presentado?")
            },
            text = {
                Text(
                    "Esta acción registrará la ausencia del relator. La sala no se libera."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarAusencia = false

                        scope.launch {
                            repo.marcarNoPresentado(idReserva)
                            volver()
                        }
                    }
                ) {
                    Text(
                        text = "Confirmar",
                        color = RojoSupervisar
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarAusencia = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EncabezadoSupervisar(
    volver: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(OscuroSupervisar)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = volver,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "‹",
                    fontSize = 35.sp,
                    color = Color.White
                )
            }

            Text(
                text = "Supervisar reserva",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(AmarilloSupervisar)
        )
    }
}

@Composable
private fun EstadoSupervisar(
    estado: EstadoReserva
) {
    val fondo = when (estado) {
        EstadoReserva.EN_USO,
        EstadoReserva.VERIFICADA -> VerdeSupervisar

        EstadoReserva.RESERVADA -> Color(0xFFFFF2CC)

        EstadoReserva.CANCELADA,
        EstadoReserva.NO_PRESENTADO -> Color(0xFFFFE5E5)

        else -> FondoSupervisar
    }

    val textoColor = when (estado) {
        EstadoReserva.EN_USO,
        EstadoReserva.VERIFICADA -> Color.White

        EstadoReserva.CANCELADA,
        EstadoReserva.NO_PRESENTADO -> RojoSupervisar

        else -> TextoSupervisar
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = fondo
    ) {
        Text(
            text = estado.texto,
            modifier = Modifier.padding(
                horizontal = 11.dp,
                vertical = 6.dp
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textoColor
        )
    }
}

@Composable
private fun PasoSeguimiento(
    titulo: String,
    descripcion: String,
    color: Color,
    tipo: Int,
    mostrarLinea: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 65.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.width(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier.size(20.dp)
            ) {
                if (tipo == 0) {
                    drawCircle(
                        color = color
                    )
                } else {
                    drawCircle(
                        color = color,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            if (mostrarLinea) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(45.dp)
                        .background(
                            if (color == OscuroSupervisar) {
                                OscuroSupervisar
                            } else {
                                BordeSupervisar
                            }
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 1.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (tipo == 0) {
                    TextoSupervisar
                } else {
                    GrisSupervisar
                }
            )

            Text(
                text = descripcion,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = GrisSupervisar
            )
        }
    }
}

private fun formatearCreacionSupervisar(
    valor: String
): String {
    return try {
        val fecha = LocalDateTime.parse(valor)

        val formato = DateTimeFormatter.ofPattern(
            "EEEE d, HH:mm",
            Locale.forLanguageTag("es-CL")
        )

        fecha.format(formato)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        valor.replace("T", " ")
    }
}
