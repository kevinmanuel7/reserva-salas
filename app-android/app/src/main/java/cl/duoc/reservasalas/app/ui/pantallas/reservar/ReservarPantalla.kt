
package cl.duoc.reservasalas.app.ui.pantallas.reservar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.repository.ResultadoReserva
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    val formatoFecha = remember {
        DateTimeFormatter.ofPattern(
            "EEE d MMM",
            Locale("es", "CL")
        )
    }

    val fechas = remember(hoy) {
        (0..14).map { dias ->
            hoy.plusDays(dias.toLong())
        }
    }

    val bloques = if (sala != null) {
        viewModel.obtenerBloques(sala, horarioGeneral)
    } else {
        emptyList()
    }

    val reservasDelDia = reservas.filter { reserva ->
        reserva.codigoSala == codigo &&
                reserva.fecha == fechaSeleccionada
    }

    fun bloqueDisponible(bloque: Bloque): Boolean {
        val ocupado = reservasDelDia.any { reserva ->
            reserva.ocupa(bloque)
        }

        val pasado = fechaSeleccionada == hoy &&
                !bloque.inicio.isAfter(viewModel.reloj.ahora().toLocalTime())

        return !ocupado && !pasado
    }

    fun seleccionarBloque(bloque: Bloque) {
        mensajeError = null

        if (bloque in bloquesSeleccionados) {
            bloquesSeleccionados = bloquesSeleccionados - bloque
            return
        }

        if (bloquesSeleccionados.size >= 4) {
            mensajeError = "Puedes seleccionar como máximo 4 bloques"
            return
        }

        val nuevaSeleccion = bloquesSeleccionados + bloque

        val indices = nuevaSeleccion
            .map { bloques.indexOf(it) }
            .sorted()

        val consecutivos = indices.zipWithNext().all { (anterior, siguiente) ->
            siguiente == anterior + 1
        }

        if (!consecutivos) {
            mensajeError = "Debes seleccionar bloques consecutivos"
            return
        }

        bloquesSeleccionados = nuevaSeleccion.sortedBy { it.inicio }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = volver
        ) {
            Text("← Volver")
        }

        Text(
            text = "Reservar sala",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (sala == null) {
            Text("No se encontró la sala")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = sala.codigo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(sala.nombre)

                    Text("Capacidad: ${sala.capacidad} personas")
                }
            }

            HorizontalDivider()

            Text(
                text = "Selecciona una fecha",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            fechas.chunked(3).forEach { grupo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grupo.forEach { fecha ->
                        val seleccionada = fecha == fechaSeleccionada
                        val habilitada = fecha.dayOfWeek in horarioGeneral.dias

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = habilitada) {
                                    fechaSeleccionada = fecha
                                    bloquesSeleccionados = emptyList()
                                    mensajeError = null
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (seleccionada) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            border = if (seleccionada) {
                                BorderStroke(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary
                                )
                            } else {
                                null
                            }
                        ) {
                            Text(
                                text = fecha.format(formatoFecha),
                                modifier = Modifier.padding(12.dp),
                                color = if (habilitada) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.4f
                                    )
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Selecciona los bloques horarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Puedes seleccionar hasta 4 bloques consecutivos",
                style = MaterialTheme.typography.bodyMedium
            )

            if (fechaSeleccionada.dayOfWeek !in horarioGeneral.dias) {
                Text("No hay atención en esta fecha")
            } else if (bloques.isEmpty()) {
                Text("No hay bloques disponibles")
            } else {
                bloques.forEachIndexed { indice, bloque ->
                    val disponible = bloqueDisponible(bloque)
                    val seleccionado = bloque in bloquesSeleccionados

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = disponible) {
                                seleccionarBloque(bloque)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionado) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        border = if (seleccionado) {
                            BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary
                            )
                        } else {
                            null
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Bloque ${indice + 1}",
                                fontWeight = FontWeight.Bold
                            )

                            Text(bloque.texto)

                            Text(
                                text = when {
                                    seleccionado -> "Seleccionado"
                                    disponible -> "Disponible"
                                    else -> "No disponible"
                                },
                                color = if (disponible) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Motivo de la reserva",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = motivo,
                onValueChange = {
                    motivo = it
                    mensajeError = null
                },
                label = {
                    Text("Escribe el motivo")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text(
                text = "Bloques seleccionados: ${bloquesSeleccionados.size} de 4"
            )

            if (mensajeError != null) {
                Text(
                    text = mensajeError ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            BotonPrincipal(
                texto = "Confirmar reserva",
                alPresionar = {
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
                modifier = Modifier.fillMaxWidth(),
                habilitado = bloquesSeleccionados.isNotEmpty() &&
                        motivo.isNotBlank() &&
                        !guardando &&
                        bloquesSeleccionados.all { bloqueDisponible(it) },
                cargando = guardando
            )
        }
    }
}
