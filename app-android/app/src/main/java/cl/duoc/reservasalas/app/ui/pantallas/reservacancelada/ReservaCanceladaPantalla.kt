
package cl.duoc.reservasalas.app.ui.pantallas.reservacancelada

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import cl.duoc.reservasalas.app.ui.pantallas.reservaconfirmada.ReservaConfirmadaViewModel
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoCancelacion = Color(0xFFF2F3F5)
private val OscuroCancelacion = Color(0xFF2D2927)
private val AmarilloCancelacion = Color(0xFFFFB91F)
private val TextoCancelacion = Color(0xFF302D2B)
private val SecundarioCancelacion = Color(0xFF605D5A)
private val BordeCancelacion = Color(0xFFDADADA)
private val VerdeCancelacion = Color(0xFF2E7D32)

@Composable
fun ReservaCanceladaPantalla(
    idReserva: Int,
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ReservaConfirmadaViewModel = viewModel(
        factory = ReservaConfirmadaViewModel.Factory
    )

    val reservas by viewModel.reservas.collectAsState()
    val reserva = reservas.find { it.id == idReserva }

    var segundosRestantes by remember(idReserva) {
        mutableIntStateOf(5)
    }

    LaunchedEffect(idReserva) {
        for (segundo in 4 downTo 0) {
            delay(1000)
            segundosRestantes = segundo
        }
        navegar(Rutas.SALAS)
    }

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM 'de' yyyy",
        Locale.forLanguageTag("es-CL")
    )

    val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCancelacion)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroCancelacion)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reserva cancelada",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloCancelacion)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(VerdeCancelacion, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "¡Reserva cancelada!",
                color = TextoCancelacion,
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Tu reserva se canceló correctamente y la sala ha quedado disponible.",
                color = SecundarioCancelacion,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BordeCancelacion)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OscuroCancelacion)
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Reserva N.º $idReserva",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (reserva != null) {
                            DatoCancelacion(
                                etiqueta = "Sala",
                                valor = reserva.codigoSala
                            )

                            DatoCancelacion(
                                etiqueta = "Fecha",
                                valor = reserva.fecha.format(formatoFecha)
                                    .replaceFirstChar { it.uppercase() }
                            )

                            DatoCancelacion(
                                etiqueta = "Horario",
                                valor = "${reserva.horaInicio.format(formatoHora)} – ${reserva.horaFin.format(formatoHora)}"
                            )

                            DatoCancelacion(
                                etiqueta = "Motivo",
                                valor = reserva.motivo.ifBlank {
                                    "Sin motivo especificado"
                                }
                            )
                        }

                        HorizontalDivider(color = BordeCancelacion)

                        DatoCancelacion(
                            etiqueta = "Estado",
                            valor = "Cancelada"
                        )
                    }
                }
            }

            Text(
                text = "Volviendo a Salas en $segundosRestantes segundos...",
                color = SecundarioCancelacion,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Surface(
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        navegar(Rutas.SALAS)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloCancelacion,
                        contentColor = TextoCancelacion
                    )
                ) {
                    Text(
                        text = "Volver a salas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DatoCancelacion(
    etiqueta: String,
    valor: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            color = SecundarioCancelacion,
            fontSize = 14.sp
        )

        Text(
            text = valor,
            color = TextoCancelacion,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
