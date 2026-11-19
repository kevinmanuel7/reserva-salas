
package cl.duoc.reservasalas.app.ui.pantallas.reservasdia

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoReservas = Color(0xFFF2F3F5)
private val NegroReservas = Color(0xFF2D2927)
private val AmarilloReservas = Color(0xFFFFB91F)
private val TextoReservas = Color(0xFF302D2B)
private val GrisReservas = Color(0xFF605D5A)
private val BordeReservas = Color(0xFFDADADA)
private val RojoReservas = Color(0xFFD32F2F)
private val VerdeReservas = Color(0xFF2E7D32)
private val CremaReservas = Color(0xFFFFF2CC)

@Composable
fun ReservasDiaPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as ReservaSalasApp
    val reservas by app.container.repositorioReservas.reservas.collectAsState()

    var dia by remember {
        mutableStateOf(app.container.reloj.hoy())
    }

    var filtro by remember {
        mutableStateOf("Todas")
    }

    val ahora = app.container.reloj.ahora()
    val hoy = ahora.toLocalDate()
    val horaActual = ahora.toLocalTime()

    val formatoFecha = remember {
        DateTimeFormatter.ofPattern(
            "EEEE d 'de' MMMM",
            Locale.forLanguageTag("es-CL")
        )
    }

    val fechaTexto = dia.format(formatoFecha)
        .replaceFirstChar { it.uppercase() }

    val reservasDelDia = reservas
        .filter { it.fecha == dia }
        .sortedBy { it.horaInicio }

    val visibles = reservasDelDia.filter { reserva ->
        val sinCheckin = reserva.estado == EstadoReserva.RESERVADA &&
                (
                        dia.isBefore(hoy) ||
                                (
                                        dia == hoy &&
                                                horaActual.isAfter(
                                                    reserva.horaInicio.plusMinutes(15)
                                                )
                                        )
                        )

        when (filtro) {
            "Reservada" -> reserva.estado == EstadoReserva.RESERVADA
            "En uso" -> reserva.estado == EstadoReserva.EN_USO
            "Sin check-in" -> sinCheckin
            "Verificada" -> reserva.estado == EstadoReserva.VERIFICADA
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoReservas)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(NegroReservas)
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
                text = "Reservas",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(AmarilloReservas)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "‹",
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                color = TextoReservas,
                modifier = Modifier
                    .clickable {
                        dia = dia.minusDays(1)
                    }
                    .padding(horizontal = 10.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (dia == hoy) "Hoy" else {
                        dia.format(
                            DateTimeFormatter.ofPattern(
                                "EEEE",
                                Locale.forLanguageTag("es-CL")
                            )
                        ).replaceFirstChar { it.uppercase() }
                    },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoReservas
                )

                Text(
                    text = fechaTexto,
                    fontSize = 15.sp,
                    color = GrisReservas
                )
            }

            Text(
                text = "›",
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                color = TextoReservas,
                modifier = Modifier
                    .clickable {
                        dia = dia.plusDays(1)
                    }
                    .padding(horizontal = 10.dp)
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = BordeReservas
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        FiltroReserva(
                            texto = "Todas",
                            seleccionado = filtro == "Todas",
                            alPresionar = {
                                filtro = "Todas"
                            }
                        )

                        FiltroReserva(
                            texto = "Reservada",
                            seleccionado = filtro == "Reservada",
                            alPresionar = {
                                filtro = "Reservada"
                            }
                        )

                        FiltroReserva(
                            texto = "En uso",
                            seleccionado = filtro == "En uso",
                            alPresionar = {
                                filtro = "En uso"
                            }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        FiltroReserva(
                            texto = "Sin check-in",
                            seleccionado = filtro == "Sin check-in",
                            alPresionar = {
                                filtro = "Sin check-in"
                            }
                        )

                        FiltroReserva(
                            texto = "Verificada",
                            seleccionado = filtro == "Verificada",
                            alPresionar = {
                                filtro = "Verificada"
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = if (visibles.size == 1) {
                        "1 reserva"
                    } else {
                        "${visibles.size} reservas"
                    },
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 2.dp
                    ),
                    fontSize = 15.sp,
                    color = GrisReservas
                )
            }

            if (visibles.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(13.dp),
                        color = Color.White,
                        border = BorderStroke(
                            1.dp,
                            BordeReservas
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No hay reservas",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoReservas
                            )

                            Text(
                                text = "No se encontraron reservas para esta fecha y filtro.",
                                fontSize = 14.sp,
                                color = GrisReservas
                            )
                        }
                    }
                }
            }

            items(
                items = visibles,
                key = { it.id }
            ) { reserva ->
                val sinCheckin = reserva.estado == EstadoReserva.RESERVADA &&
                        (
                                dia.isBefore(hoy) ||
                                        (
                                                dia == hoy &&
                                                        horaActual.isAfter(
                                                            reserva.horaInicio.plusMinutes(15)
                                                        )
                                                )
                                )

                val minutosSinCheckin = if (
                    sinCheckin && dia == hoy
                ) {
                    Duration.between(
                        reserva.horaInicio,
                        horaActual
                    ).toMinutes().coerceAtLeast(0)
                } else {
                    0L
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navegar(
                                Rutas.supervisar(reserva.id)
                            )
                        },
                    shape = RoundedCornerShape(13.dp),
                    color = Color.White,
                    border = BorderStroke(
                        1.dp,
                        BordeReservas
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (sinCheckin) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(84.dp)
                                    .background(RojoReservas)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${reserva.horaInicio}–${reserva.horaFin} · ${reserva.codigoSala}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoReservas,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = if (sinCheckin) {
                                        if (dia == hoy) {
                                            "${reserva.nombreUsuario} · sin check-in hace $minutosSinCheckin min"
                                        } else {
                                            "${reserva.nombreUsuario} · sin check-in"
                                        }
                                    } else {
                                        reserva.nombreUsuario
                                    },
                                    fontSize = 13.sp,
                                    color = GrisReservas,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            EstadoReservaEtiqueta(
                                estado = reserva.estado
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroReserva(
    texto: String,
    seleccionado: Boolean,
    alPresionar: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable {
            alPresionar()
        },
        shape = RoundedCornerShape(50),
        color = if (seleccionado) {
            NegroReservas
        } else {
            Color.White
        },
        border = BorderStroke(
            1.dp,
            if (seleccionado) {
                NegroReservas
            } else {
                BordeReservas
            }
        )
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 10.dp
            ),
            fontSize = 13.sp,
            fontWeight = if (seleccionado) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            color = if (seleccionado) {
                Color.White
            } else {
                TextoReservas
            }
        )
    }
}

@Composable
private fun EstadoReservaEtiqueta(
    estado: EstadoReserva
) {
    val texto: String
    val fondo: Color
    val colorTexto: Color
    val borde: Color?

    when (estado) {
        EstadoReserva.VERIFICADA -> {
            texto = "Verificada"
            fondo = Color.White
            colorTexto = VerdeReservas
            borde = VerdeReservas
        }

        EstadoReserva.EN_USO -> {
            texto = "En uso"
            fondo = VerdeReservas
            colorTexto = Color.White
            borde = null
        }

        EstadoReserva.RESERVADA -> {
            texto = "Reservada"
            fondo = CremaReservas
            colorTexto = TextoReservas
            borde = null
        }

        EstadoReserva.CANCELADA -> {
            texto = "Cancelada"
            fondo = Color(0xFFFFE5E5)
            colorTexto = RojoReservas
            borde = null
        }

        EstadoReserva.NO_PRESENTADO -> {
            texto = "No presentado"
            fondo = Color(0xFFFFE5E5)
            colorTexto = RojoReservas
            borde = null
        }

        else -> {
            texto = estado.texto
            fondo = FondoReservas
            colorTexto = TextoReservas
            borde = null
        }
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = fondo,
        border = borde?.let {
            BorderStroke(1.dp, it)
        }
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorTexto,
            maxLines = 1
        )
    }
}
