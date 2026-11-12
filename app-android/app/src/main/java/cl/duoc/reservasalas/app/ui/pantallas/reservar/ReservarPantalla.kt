
package cl.duoc.reservasalas.app.ui.pantallas.reservar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.repository.ResultadoReserva
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FondoReserva = Color(0xFFF2F3F5)
private val OscuroReserva = Color(0xFF2D2927)
private val AmarilloReserva = Color(0xFFFFB91F)
private val TextoReserva = Color(0xFF302D2B)
private val SecundarioReserva = Color(0xFF605D5A)
private val BordeReserva = Color(0xFFDADADA)
private val GrisReserva = Color(0xFFE0E0E0)

@Composable
fun ReservarPantalla(
    codigo: String,
    navegar: (String) -> Unit,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ReservarViewModel = viewModel(
        factory = ReservarViewModel.Factory
    )

    val salas = viewModel.salas.collectAsState().value
    val horarioGeneral = viewModel.horario.collectAsState().value
    val reservas = viewModel.reservas.collectAsState().value
    val sala = salas.find { it.codigo == codigo }

    val hoy = remember { viewModel.reloj.hoy() }

    var fechaSeleccionada by remember {
        mutableStateOf(hoy)
    }

    var bloquesSeleccionados by remember {
        mutableStateOf<List<Bloque>>(emptyList())
    }

    var motivo by remember {
        mutableStateOf("")
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    val fechas = remember(hoy) {
        (0..14).map { hoy.plusDays(it.toLong()) }
    }

    val formatoDia = remember {
        DateTimeFormatter.ofPattern(
            "EEE",
            Locale("es", "CL")
        )
    }

    val formatoFechaResumen = remember {
        DateTimeFormatter.ofPattern(
            "EEEE d",
            Locale("es", "CL")
        )
    }

    val formatoHora = remember {
        DateTimeFormatter.ofPattern("HH:mm")
    }

    val bloques = if (sala != null) {
        viewModel.obtenerBloques(sala, horarioGeneral)
    } else {
        emptyList()
    }

    val reservasDelDia = reservas.filter { reserva ->
        reserva.codigoSala == codigo &&
                reserva.fecha == fechaSeleccionada &&
                reserva.estado != EstadoReserva.CANCELADA
    }

    fun bloqueDisponible(bloque: Bloque): Boolean {
        val ocupado = reservasDelDia.any { reserva ->
            reserva.ocupa(bloque)
        }

        val pasado = fechaSeleccionada == hoy &&
                !bloque.inicio.isAfter(
                    viewModel.reloj.ahora().toLocalTime()
                )

        return !ocupado && !pasado
    }

    fun seleccionarBloque(bloque: Bloque) {
        mensajeError = null

        if (bloque in bloquesSeleccionados) {
            bloquesSeleccionados =
                bloquesSeleccionados - bloque
            return
        }

        if (bloquesSeleccionados.size >= 4) {
            mensajeError =
                "Puedes seleccionar como máximo 4 bloques"
            return
        }

        val nuevaSeleccion = bloquesSeleccionados + bloque

        val indices = nuevaSeleccion
            .map { bloques.indexOf(it) }
            .sorted()

        val consecutivos = indices.zipWithNext()
            .all { (anterior, siguiente) ->
                siguiente == anterior + 1
            }

        if (!consecutivos) {
            mensajeError =
                "Debes seleccionar bloques consecutivos"
            return
        }

        bloquesSeleccionados =
            nuevaSeleccion.sortedBy { it.inicio }
    }

    val seleccionOrdenada =
        bloquesSeleccionados.sortedBy { it.inicio }

    val resumenHorario = if (seleccionOrdenada.isEmpty()) {
        "Selecciona entre 1 y 4 bloques"
    } else {
        val primero = seleccionOrdenada.first()
        val ultimo = seleccionOrdenada.last()

        "${primero.inicio.format(formatoHora)}–${
            ultimo.fin.format(formatoHora)
        } (${seleccionOrdenada.size} ${
            if (seleccionOrdenada.size == 1) {
                "bloque"
            } else {
                "bloques"
            }
        })"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoReserva)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroReserva)
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
                    text = "Reservar $codigo",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloReserva)
            )
        }

        if (sala == null) {
            Text(
                text = "No se encontró la sala",
                color = TextoReserva,
                modifier = Modifier.padding(20.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 18.dp,
                        bottom = 24.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Elige el día",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoReserva
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    fechas.forEach { fecha ->
                        val seleccionada =
                            fecha == fechaSeleccionada

                        val habilitada =
                            fecha.dayOfWeek in horarioGeneral.dias

                        Surface(
                            modifier = Modifier
                                .width(58.dp)
                                .height(79.dp)
                                .clickable(enabled = habilitada) {
                                    fechaSeleccionada = fecha
                                    bloquesSeleccionados = emptyList()
                                    mensajeError = null
                                },
                            color = if (seleccionada) {
                                AmarilloReserva
                            } else {
                                Color.White
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (seleccionada) {
                                    AmarilloReserva
                                } else {
                                    BordeReserva
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(7.dp),
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.Center
                            ) {
                                Text(
                                    text = fecha.format(formatoDia)
                                        .replaceFirstChar {
                                            it.uppercase()
                                        },
                                    color = if (habilitada) {
                                        TextoReserva
                                    } else {
                                        SecundarioReserva.copy(
                                            alpha = 0.45f
                                        )
                                    },
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = fecha.dayOfMonth.toString(),
                                    color = if (habilitada) {
                                        TextoReserva
                                    } else {
                                        SecundarioReserva.copy(
                                            alpha = 0.45f
                                        )
                                    },
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Elige los bloques",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoReserva
                    )

                    Text(
                        text = "De 1 a 4 seguidos",
                        fontSize = 12.sp,
                        color = SecundarioReserva
                    )
                }

                if (
                    fechaSeleccionada.dayOfWeek
                    !in horarioGeneral.dias
                ) {
                    Text(
                        text = "No hay atención en esta fecha",
                        color = SecundarioReserva
                    )
                } else if (bloques.isEmpty()) {
                    Text(
                        text = "No hay bloques disponibles",
                        color = SecundarioReserva
                    )
                } else {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        bloques.chunked(3).forEach { fila ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                fila.forEach { bloque ->
                                    val disponible =
                                        bloqueDisponible(bloque)

                                    val seleccionado =
                                        bloque in bloquesSeleccionados

                                    val fondo = when {
                                        seleccionado -> AmarilloReserva
                                        !disponible -> GrisReserva
                                        else -> Color.White
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clickable(
                                                enabled = disponible &&
                                                        !guardando
                                            ) {
                                                seleccionarBloque(bloque)
                                            },
                                        color = fondo,
                                        shape = RoundedCornerShape(9.dp),
                                        border = BorderStroke(
                                            if (seleccionado) {
                                                2.dp
                                            } else {
                                                1.dp
                                            },
                                            if (seleccionado) {
                                                OscuroReserva
                                            } else {
                                                BordeReserva
                                            }
                                        )
                                    ) {
                                        Box(
                                            contentAlignment =
                                                Alignment.Center
                                        ) {
                                            Text(
                                                text = bloque.inicio
                                                    .format(formatoHora),
                                                color = if (disponible) {
                                                    TextoReserva
                                                } else {
                                                    SecundarioReserva
                                                },
                                                fontSize = 16.sp,
                                                fontWeight =
                                                    if (seleccionado) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    },
                                                textDecoration =
                                                    if (!disponible) {
                                                        TextDecoration.LineThrough
                                                    } else {
                                                        TextDecoration.None
                                                    },
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                repeat(3 - fila.size) {
                                    Spacer(
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeyendaReserva(
                        color = Color.White,
                        texto = "Libre"
                    )

                    LeyendaReserva(
                        color = GrisReserva,
                        texto = "Ocupado"
                    )

                    LeyendaReserva(
                        color = AmarilloReserva,
                        texto = "Tu selección"
                    )
                }

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Motivo (opcional)",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoReserva
                    )

                    OutlinedTextField(
                        value = motivo,
                        onValueChange = {
                            motivo = it
                            mensajeError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Escribe el motivo de la reserva")
                        },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = AmarilloReserva,
                            unfocusedBorderColor = BordeReserva
                        )
                    )
                }

                mensajeError?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )
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
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "$codigo · ${
                            fechaSeleccionada
                                .format(formatoFechaResumen)
                                .replaceFirstChar {
                                    it.uppercase()
                                }
                        } · $resumenHorario",
                        fontSize = 15.sp,
                        color = TextoReserva
                    )

                    Button(
                        onClick = {
                            guardando = true
                            mensajeError = null

                            viewModel.crearReserva(
                                codigoSala = codigo,
                                fecha = fechaSeleccionada,
                                bloques = bloquesSeleccionados,
                                motivo = motivo.trim()
                            ) { resultado ->
                                guardando = false

                                when (resultado) {
                                    is ResultadoReserva.Exito -> {
                                        navegar(
                                            Rutas.reservaConfirmada(
                                                resultado.reserva.id
                                            )
                                        )
                                    }

                                    is ResultadoReserva.Error -> {
                                        mensajeError = resultado.mensaje
                                    }
                                }
                            }
                        },
                        enabled = bloquesSeleccionados.isNotEmpty() &&
                                !guardando &&
                                bloquesSeleccionados.all {
                                    bloqueDisponible(it)
                                },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloReserva,
                            contentColor = TextoReserva,
                            disabledContainerColor =
                                Color(0xFFE7D7A9),
                            disabledContentColor =
                                SecundarioReserva
                        )
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = TextoReserva
                            )
                        } else {
                            Text(
                                text = "Confirmar reserva",
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

@Composable
private fun LeyendaReserva(
    color: Color,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(15.dp),
            color = color,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, BordeReserva)
        ) {}

        Text(
            text = texto,
            color = SecundarioReserva,
            fontSize = 12.sp
        )
    }
}
