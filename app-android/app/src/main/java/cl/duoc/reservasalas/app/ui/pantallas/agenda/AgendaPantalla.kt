
package cl.duoc.reservasalas.app.ui.pantallas.agenda

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoAgenda = Color(0xFFF2F3F5)
private val OscuroAgenda = Color(0xFF2D2927)
private val AmarilloAgenda = Color(0xFFFFB91F)
private val TextoAgenda = Color(0xFF302D2B)
private val SecundarioAgenda = Color(0xFF605D5A)
private val BordeAgenda = Color(0xFFDADADA)
private val VerdeAgenda = Color(0xFF388E3C)
private val RojoAgenda = Color(0xFFD32F2F)

@Composable
fun AgendaPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: AgendaViewModel = viewModel(
        factory = AgendaViewModel.Factory
    )

    val reservas by vm.reservas.collectAsState()
    val salas by vm.salas.collectAsState()
    val idUsuario by vm.idUsuario.collectAsState()

    var historial by remember {
        mutableStateOf(false)
    }

    val app = LocalContext.current.applicationContext as ReservaSalasApp
    val hoy = app.container.reloj.hoy()

    val propias = reservas.filter {
        it.idUsuario == idUsuario
    }

    val filtradas = propias.filter { reserva ->
        if (historial) {
            reserva.fecha.isBefore(hoy) ||
                    reserva.estado in listOf(
                EstadoReserva.CANCELADA,
                EstadoReserva.VERIFICADA,
                EstadoReserva.NO_PRESENTADO
            )
        } else {
            !reserva.fecha.isBefore(hoy) &&
                    reserva.estado in listOf(
                EstadoReserva.RESERVADA,
                EstadoReserva.EN_USO
            )
        }
    }.sortedWith(
        compareBy({ it.fecha }, { it.horaInicio })
    )

    val reservasOrdenadas = if (historial) {
        filtradas.reversed()
    } else {
        filtradas
    }

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM",
        Locale.forLanguageTag("es-CL")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoAgenda)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroAgenda)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mi agenda",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloAgenda)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PestañaAgenda(
                texto = "Próximas",
                seleccionada = !historial,
                modifier = Modifier.weight(1f),
                alPresionar = {
                    historial = false
                }
            )

            PestañaAgenda(
                texto = "Historial",
                seleccionada = historial,
                modifier = Modifier.weight(1f),
                alPresionar = {
                    historial = true
                }
            )
        }

        if (idUsuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cargando reservas...",
                    color = SecundarioAgenda,
                    fontSize = 16.sp
                )
            }
        } else if (reservasOrdenadas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "▤",
                        fontSize = 55.sp,
                        color = SecundarioAgenda
                    )

                    Text(
                        text = if (historial) {
                            "No tienes reservas en el historial"
                        } else {
                            "No tienes próximas reservas"
                        },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoAgenda
                    )

                    Text(
                        text = if (historial) {
                            "Aquí aparecerán tus reservas anteriores"
                        } else {
                            "Tus nuevas reservas aparecerán aquí"
                        },
                        fontSize = 14.sp,
                        color = SecundarioAgenda
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = reservasOrdenadas,
                    key = { it.id }
                ) { reserva ->
                    val nombreSala = salas.find {
                        it.codigo == reserva.codigoSala
                    }?.nombre ?: "Sala"

                    val etiquetaFecha = when (reserva.fecha) {
                        hoy -> "HOY"
                        hoy.plusDays(1) -> "MAÑANA"
                        else -> reserva.fecha
                            .format(formatoFecha)
                            .uppercase()
                    }

                    val colorEstado = when (reserva.estado) {
                        EstadoReserva.RESERVADA -> AmarilloAgenda
                        EstadoReserva.EN_USO -> VerdeAgenda
                        EstadoReserva.VERIFICADA -> VerdeAgenda
                        EstadoReserva.CANCELADA -> RojoAgenda
                        EstadoReserva.NO_PRESENTADO -> RojoAgenda
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = etiquetaFecha,
                            color = SecundarioAgenda,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navegar(
                                        Rutas.reserva(reserva.id)
                                    )
                                },
                            color = Color.White,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                BordeAgenda
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reserva.codigoSala,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoAgenda
                                    )

                                    Surface(
                                        color = colorEstado.copy(
                                            alpha = 0.16f
                                        ),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = reserva.estado.texto,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (reserva.estado) {
                                                EstadoReserva.RESERVADA ->
                                                    TextoAgenda
                                                else -> colorEstado
                                            }
                                        )
                                    }
                                }

                                Text(
                                    text = nombreSala,
                                    fontSize = 16.sp,
                                    color = SecundarioAgenda
                                )

                                HorizontalDivider(
                                    color = BordeAgenda
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "◷  ${reserva.horario}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextoAgenda
                                    )

                                    Text(
                                        text = "Ver detalle ›",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecundarioAgenda
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PestañaAgenda(
    texto: String,
    seleccionada: Boolean,
    modifier: Modifier = Modifier,
    alPresionar: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(46.dp)
            .clickable {
                alPresionar()
            },
        color = if (seleccionada) {
            AmarilloAgenda
        } else {
            FondoAgenda
        },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (seleccionada) {
                AmarilloAgenda
            } else {
                BordeAgenda
            }
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                fontSize = 16.sp,
                fontWeight = if (seleccionada) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = TextoAgenda
            )
        }
    }
}
