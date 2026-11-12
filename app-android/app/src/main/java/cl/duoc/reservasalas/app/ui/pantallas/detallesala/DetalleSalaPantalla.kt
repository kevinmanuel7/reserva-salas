
package cl.duoc.reservasalas.app.ui.pantallas.detallesala

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.EstadoEquipo
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.bloques
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FondoDetalle = Color(0xFFF2F3F5)
private val OscuroDetalle = Color(0xFF2D2927)
private val AmarilloDetalle = Color(0xFFFFB91F)
private val TextoDetalle = Color(0xFF302D2B)
private val SecundarioDetalle = Color(0xFF605D5A)
private val BordeDetalle = Color(0xFFDADADA)
private val RojoDetalle = Color(0xFFD32F2F)
private val VerdeDetalle = Color(0xFF388E3C)
private val GrisOcupado = Color(0xFFE0E0E0)

@Composable
fun DetalleSalaPantalla(
    codigo: String,
    navegar: (String) -> Unit,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: DetalleSalaViewModel = viewModel(
        factory = DetalleSalaViewModel.Factory
    )

    val app = LocalContext.current.applicationContext as ReservaSalasApp

    val sesion = app.container.sesionDataStore.sesion
        .collectAsState(initial = null).value

    val coordinador = sesion?.usuario?.rol == Rol.COORDINADOR

    val salas = viewModel.salas.collectAsState().value
    val reservas = viewModel.reservas.collectAsState().value
    val horarioGeneral = viewModel.horario.collectAsState().value

    val sala = salas.find { it.codigo == codigo }
    val hoy = LocalDate.now()
    val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    val bloquesHoy = sala?.bloques(horarioGeneral).orEmpty()

    val reservasSalaHoy = reservas.filter { reserva ->
        reserva.codigoSala == codigo &&
                reserva.fecha == hoy &&
                reserva.estado != EstadoReserva.CANCELADA
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoDetalle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroDetalle)
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
                    text = sala?.codigo ?: codigo,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloDetalle)
            )
        }

        if (sala == null) {
            Text(
                text = "No se encontró la sala",
                color = TextoDetalle,
                fontSize = 18.sp,
                modifier = Modifier.padding(20.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BordeDetalle)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = sala.nombre,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoDetalle
                        )

                        Text(
                            text = "Código ${sala.codigo} · ${sala.ubicacion}",
                            fontSize = 16.sp,
                            color = SecundarioDetalle
                        )

                        Text(
                            text = "♙  ${sala.capacidad} personas",
                            fontSize = 17.sp,
                            color = TextoDetalle
                        )

                        Text(
                            text = if (sala.horarioPropio == null) {
                                "Horario general"
                            } else {
                                "Horario: ${
                                    sala.horarioPropio.apertura.format(formatoHora)
                                } a ${
                                    sala.horarioPropio.cierre.format(formatoHora)
                                }"
                            },
                            fontSize = 14.sp,
                            color = SecundarioDetalle
                        )
                    }
                }

                Text(
                    text = "Equipamiento",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoDetalle
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BordeDetalle)
                ) {
                    Column {
                        if (sala.equipos.isEmpty()) {
                            Text(
                                text = "Esta sala no tiene equipamiento registrado",
                                modifier = Modifier.padding(18.dp),
                                color = SecundarioDetalle
                            )
                        } else {
                            sala.equipos.forEachIndexed { index, equipo ->
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = equipo.nombre,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextoDetalle,
                                            modifier = Modifier.weight(1f)
                                        )

                                        val conFalla =
                                            equipo.estado == EstadoEquipo.CON_FALLA

                                        if (conFalla) {
                                            Surface(
                                                color = RojoDetalle,
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = "Con falla",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 5.dp
                                                    )
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = equipo.estado.texto,
                                                color = VerdeDetalle,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (equipo.estado == EstadoEquipo.CON_FALLA) {
                                        equipo.comentario?.let { comentario ->
                                            Text(
                                                text = "Coordinación: «$comentario»",
                                                fontSize = 14.sp,
                                                color = SecundarioDetalle
                                            )
                                        }
                                    }
                                }

                                if (index < sala.equipos.lastIndex) {
                                    HorizontalDivider(
                                        color = BordeDetalle
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Disponibilidad de hoy",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoDetalle
                    )

                    Text(
                        text = "Gris = ocupado",
                        fontSize = 12.sp,
                        color = SecundarioDetalle
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bloquesHoy.chunked(5).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            fila.forEach { bloque ->
                                val ocupado = reservasSalaHoy.any { reserva ->
                                    bloque.inicio >= reserva.horaInicio &&
                                            bloque.inicio < reserva.horaFin
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(45.dp),
                                    color = if (ocupado) {
                                        GrisOcupado
                                    } else {
                                        Color.White
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        BordeDetalle
                                    )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = bloque.inicio.format(formatoHora),
                                            color = if (ocupado) {
                                                Color.Gray
                                            } else {
                                                TextoDetalle
                                            },
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            repeat(5 - fila.size) {
                                Spacer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

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
                    if (coordinador) {
                        OutlinedButton(
                            onClick = {
                                navegar(Rutas.editarSala(sala.codigo))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                OscuroDetalle
                            )
                        ) {
                            Text(
                                text = "Editar sala",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoDetalle
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                navegar(Rutas.reservar(sala.codigo))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmarilloDetalle,
                                contentColor = TextoDetalle
                            )
                        ) {
                            Text(
                                text = "Reservar",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
