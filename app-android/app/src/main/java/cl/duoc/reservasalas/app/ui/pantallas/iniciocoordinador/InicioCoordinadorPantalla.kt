
package cl.duoc.reservasalas.app.ui.pantallas.iniciocoordinador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoCoordinador = Color(0xFFF2F3F5)
private val NegroCoordinador = Color(0xFF2D2927)
private val PanelCoordinador = Color(0xFF383330)
private val AmarilloCoordinador = Color(0xFFFFB91F)
private val TextoCoordinador = Color(0xFF302D2B)
private val GrisCoordinador = Color(0xFF605D5A)
private val BordeCoordinador = Color(0xFFDADADA)
private val RojoCoordinador = Color(0xFFD32F2F)
private val VerdeCoordinador = Color(0xFF2E7D32)

@Composable
fun InicioCoordinadorPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as ReservaSalasApp

    val reservas by app.container.repositorioReservas.reservas.collectAsState()
    val salas by app.container.repositorioSalas.salas.collectAsState()

    val ahora = app.container.reloj.ahora()
    val hoy = ahora.toLocalDate()
    val horaActual = ahora.toLocalTime()

    val reservasHoy = reservas.filter {
        it.fecha == hoy && it.estado != EstadoReserva.CANCELADA
    }

    val enUso = reservasHoy.count {
        it.estado == EstadoReserva.EN_USO
    }

    val sinCheckin = reservasHoy.count {
        it.estado == EstadoReserva.RESERVADA &&
                horaActual.isAfter(it.horaInicio.plusMinutes(15))
    }

    val salasConFallas = salas.count {
        it.tieneFallas
    }

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM",
        Locale.forLanguageTag("es-CL")
    )

    val fechaTexto = hoy.format(formatoFecha)
        .replaceFirstChar { it.uppercase() }

    val avisos = reservasHoy
        .filter {
            it.estado == EstadoReserva.EN_USO ||
                    it.estado == EstadoReserva.VERIFICADA ||
                    (
                            it.estado == EstadoReserva.RESERVADA &&
                                    horaActual.isAfter(it.horaInicio.plusMinutes(15))
                            )
        }
        .sortedByDescending { it.creada }
        .take(4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCoordinador)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(NegroCoordinador)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = Color.Transparent,
                border = BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Logo Duoc UC",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "Inicio",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PanelCoordinador)
                        .padding(
                            horizontal = 16.dp,
                            vertical = 20.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PANEL DEL COORDINADOR",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )

                    Text(
                        text = "Sede Viña del Mar · $fechaTexto",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(AmarilloCoordinador)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 18.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricaCoordinador(
                            numero = reservasHoy.size,
                            titulo = "reservas para hoy",
                            colorNumero = TextoCoordinador,
                            modifier = Modifier.weight(1f),
                            alPresionar = {
                                navegar(Rutas.RESERVAS_DIA)
                            }
                        )

                        MetricaCoordinador(
                            numero = enUso,
                            titulo = "en uso ahora",
                            colorNumero = VerdeCoordinador,
                            modifier = Modifier.weight(1f),
                            alPresionar = {
                                navegar(Rutas.RESERVAS_DIA)
                            }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricaCoordinador(
                            numero = sinCheckin,
                            titulo = "sin check-in",
                            colorNumero = RojoCoordinador,
                            modifier = Modifier.weight(1f),
                            alPresionar = {
                                navegar(Rutas.RESERVAS_DIA)
                            }
                        )

                        MetricaCoordinador(
                            numero = salasConFallas,
                            titulo = if (salasConFallas == 1) {
                                "sala con fallas"
                            } else {
                                "salas con fallas"
                            },
                            colorNumero = TextoCoordinador,
                            modifier = Modifier.weight(1f),
                            alPresionar = {
                                navegar(Rutas.SALAS)
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Avisos recientes",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 20.dp,
                        bottom = 14.dp
                    ),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoCoordinador
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(
                        1.dp,
                        BordeCoordinador
                    )
                ) {
                    Column {
                        if (avisos.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Sin avisos recientes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoCoordinador
                                )

                                Text(
                                    text = "No hay incidencias ni check-ins recientes para hoy.",
                                    fontSize = 14.sp,
                                    color = GrisCoordinador
                                )
                            }
                        } else {
                            avisos.forEachIndexed { index, reserva ->
                                val sinRegistro =
                                    reserva.estado == EstadoReserva.RESERVADA &&
                                            horaActual.isAfter(
                                                reserva.horaInicio.plusMinutes(15)
                                            )

                                val verificada =
                                    reserva.estado == EstadoReserva.VERIFICADA

                                val minutosSinCheckin = if (sinRegistro) {
                                    Duration.between(
                                        reserva.horaInicio,
                                        horaActual
                                    ).toMinutes().coerceAtLeast(0)
                                } else {
                                    0L
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navegar(
                                                Rutas.supervisar(reserva.id)
                                            )
                                        }
                                        .padding(
                                            horizontal = 16.dp,
                                            vertical = 15.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = if (sinRegistro) {
                                            "!"
                                        } else {
                                            "▦"
                                        },
                                        color = if (sinRegistro) {
                                            RojoCoordinador
                                        } else {
                                            TextoCoordinador
                                        },
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(28.dp)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (sinRegistro) {
                                            Text(
                                                text = "${reserva.codigoSala} sin check-in hace $minutosSinCheckin min",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextoCoordinador,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = "${reserva.nombreUsuario} · ${reserva.horaInicio}–${reserva.horaFin}",
                                                fontSize = 13.sp,
                                                color = GrisCoordinador,
                                                maxLines = 2
                                            )
                                        } else {
                                            Text(
                                                text = "${reserva.nombreUsuario} hizo check-in en ${reserva.codigoSala}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextoCoordinador,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = if (verificada) {
                                                    "${reserva.horaInicio} · verificada"
                                                } else {
                                                    "${reserva.horaInicio} · sin revisar"
                                                },
                                                fontSize = 13.sp,
                                                color = GrisCoordinador
                                            )
                                        }
                                    }

                                    if (
                                        reserva.estado == EstadoReserva.EN_USO
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    AmarilloCoordinador,
                                                    RoundedCornerShape(50)
                                                )
                                        )
                                    }
                                }

                                if (index < avisos.lastIndex) {
                                    HorizontalDivider(
                                        color = BordeCoordinador,
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
}

@Composable
private fun MetricaCoordinador(
    numero: Int,
    titulo: String,
    colorNumero: Color,
    modifier: Modifier = Modifier,
    alPresionar: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(94.dp)
            .clickable {
                alPresionar()
            },
        shape = RoundedCornerShape(13.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            BordeCoordinador
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = numero.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorNumero
            )

            Text(
                text = titulo,
                fontSize = 13.sp,
                color = GrisCoordinador,
                maxLines = 2
            )
        }
    }
}
