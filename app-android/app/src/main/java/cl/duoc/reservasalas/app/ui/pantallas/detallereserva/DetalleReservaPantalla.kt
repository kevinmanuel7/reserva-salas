
package cl.duoc.reservasalas.app.ui.pantallas.detallereserva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoDetalleReserva = Color(0xFFF2F3F5)
private val OscuroDetalleReserva = Color(0xFF2D2927)
private val AmarilloDetalleReserva = Color(0xFFFFB91F)
private val AmarilloSuaveDetalle = Color(0xFFFFF2C9)
private val TextoDetalleReserva = Color(0xFF302D2B)
private val SecundarioDetalleReserva = Color(0xFF605D5A)
private val BordeDetalleReserva = Color(0xFFDADADA)
private val RojoDetalleReserva = Color(0xFFD32F2F)

@Composable
fun DetalleReservaPantalla(
    idReserva: Int,
    navegar: (String) -> Unit,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: DetalleReservaViewModel = viewModel(
        factory = DetalleReservaViewModel.Factory
    )

    val reservas = viewModel.reservas.collectAsState().value
    val salas = viewModel.salas.collectAsState().value

    val reserva = reservas.find { it.id == idReserva }
    val sala = salas.find { it.codigo == reserva?.codigoSala }

    var mostrarDialogoCancelar by remember {
        mutableStateOf(false)
    }

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM",
        Locale.forLanguageTag("es-CL")
    )

    val formatoFechaCorta = DateTimeFormatter.ofPattern(
        "EEEE d",
        Locale.forLanguageTag("es-CL")
    )

    val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    val ahora = LocalDateTime.now()

    val inicioReserva = reserva?.let {
        LocalDateTime.of(it.fecha, it.horaInicio)
    }

    val finReserva = reserva?.let {
        LocalDateTime.of(it.fecha, it.horaFin)
    }

    val inicioCheckIn = inicioReserva?.minusMinutes(15)
    val finCheckIn = inicioReserva?.plusMinutes(15)

    val esReservada = reserva?.estado == EstadoReserva.RESERVADA

    val checkInDisponible = reserva != null &&
            esReservada &&
            viewModel.checkInHabilitado(reserva)

    val reservaFinalizada = finReserva != null &&
            ahora.isAfter(finReserva)

    val mostrarAviso = reserva != null &&
            esReservada &&
            !reservaFinalizada &&
            inicioCheckIn != null &&
            finCheckIn != null

    val textoAviso = if (mostrarAviso) {
        when {
            ahora.isBefore(inicioCheckIn) -> {
                "Check-in disponible desde las ${
                    inicioCheckIn.format(formatoHora)
                }."
            }

            !ahora.isAfter(finCheckIn) -> {
                "Check-in disponible ahora. Se cierra a las ${
                    finCheckIn.format(formatoHora)
                }."
            }

            else -> {
                "El plazo para realizar el check-in ha finalizado."
            }
        }
    } else {
        ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoDetalleReserva)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroDetalleReserva)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "‹",
                    color = Color.White,
                    fontSize = 38.sp,
                    modifier = Modifier.clickable {
                        volver()
                    }
                )

                Text(
                    text = if (reserva != null) {
                        "Reserva N.º ${reserva.id}"
                    } else {
                        "Detalle de reserva"
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloDetalleReserva)
            )
        }

        if (reserva == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No se encontró la reserva",
                    color = RojoDetalleReserva,
                    fontSize = 17.sp
                )

                Button(
                    onClick = {
                        navegar(Rutas.AGENDA)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloDetalleReserva,
                        contentColor = TextoDetalleReserva
                    )
                ) {
                    Text("Ir a Mi agenda")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 16.dp,
                        vertical = 20.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        BordeDetalleReserva
                    )
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = reserva.codigoSala,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextoDetalleReserva
                                )

                                Text(
                                    text = sala?.nombre
                                        ?: reserva.codigoSala,
                                    fontSize = 17.sp,
                                    color = SecundarioDetalleReserva
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = AmarilloSuaveDetalle
                            ) {
                                Text(
                                    text = reserva.estado.texto,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoDetalleReserva
                                )
                            }
                        }

                        HorizontalDivider(
                            color = BordeDetalleReserva
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    DatoDetalleReserva(
                                        etiqueta = "Fecha",
                                        valor = if (
                                            reserva.fecha == ahora.toLocalDate()
                                        ) {
                                            "Hoy, ${
                                                reserva.fecha
                                                    .format(formatoFechaCorta)
                                            }"
                                        } else {
                                            reserva.fecha
                                                .format(formatoFechaCorta)
                                                .replaceFirstChar {
                                                    it.uppercase()
                                                }
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    DatoDetalleReserva(
                                        etiqueta = "Horario",
                                        valor = "${
                                            reserva.horaInicio.format(formatoHora)
                                        }–${
                                            reserva.horaFin.format(formatoHora)
                                        }"
                                    )
                                }
                            }

                            DatoDetalleReserva(
                                etiqueta = "Motivo",
                                valor = reserva.motivo.ifBlank {
                                    "Sin motivo especificado"
                                }
                            )

                            DatoDetalleReserva(
                                etiqueta = "Creada",
                                valor = reserva.creada
                                    .format(formatoFecha)
                                    .replaceFirstChar {
                                        it.uppercase()
                                    } + ", " +
                                        reserva.creada.format(formatoHora)
                            )
                        }
                    }
                }

                if (mostrarAviso) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = AmarilloSuaveDetalle,
                        border = BorderStroke(
                            1.dp,
                            AmarilloDetalleReserva
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "◷",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoDetalleReserva
                            )

                            Text(
                                text = textoAviso,
                                fontSize = 16.sp,
                                lineHeight = 23.sp,
                                color = TextoDetalleReserva
                            )
                        }
                    }
                }
            }

            if (
                checkInDisponible ||
                viewModel.sePuedeCancelar(reserva)
            ) {
                Surface(
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (checkInDisponible) {
                            Button(
                                onClick = {
                                    navegar(
                                        Rutas.checkin(reserva.id)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmarilloDetalleReserva,
                                    contentColor = TextoDetalleReserva
                                )
                            ) {
                                Text(
                                    text = "▦  Hacer check-in",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (viewModel.sePuedeCancelar(reserva)) {
                            OutlinedButton(
                                onClick = {
                                    mostrarDialogoCancelar = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    RojoDetalleReserva
                                )
                            ) {
                                Text(
                                    text = "Cancelar reserva",
                                    color = RojoDetalleReserva,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (mostrarDialogoCancelar) {
                AlertDialog(
                    onDismissRequest = {
                        mostrarDialogoCancelar = false
                    },
                    title = {
                        Text("¿Cancelar reserva?")
                    },
                    text = {
                        Text(
                            "La sala ${reserva.codigoSala} quedará libre de ${reserva.horario}. Esta acción no se puede deshacer."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.cancelar(reserva.id)
                                mostrarDialogoCancelar = false
                            }
                        ) {
                            Text(
                                text = "Sí, cancelar",
                                color = RojoDetalleReserva
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogoCancelar = false
                            }
                        ) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DatoDetalleReserva(
    etiqueta: String,
    valor: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = etiqueta,
            color = SecundarioDetalleReserva,
            fontSize = 14.sp
        )

        Text(
            text = valor,
            color = TextoDetalleReserva,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
