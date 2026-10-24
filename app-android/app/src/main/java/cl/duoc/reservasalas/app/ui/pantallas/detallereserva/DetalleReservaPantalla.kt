
package cl.duoc.reservasalas.app.ui.pantallas.detallereserva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM 'de' yyyy",
        Locale.forLanguageTag("es-CL")
    )

    val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = {
                volver()
            }
        ) {
            Text("← Volver")
        }

        Text(
            text = "Detalle de reserva",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (reserva == null) {
            Text(
                text = "No se encontró la reserva",
                color = MaterialTheme.colorScheme.error
            )

            BotonPrincipal(
                texto = "Ir a Mi agenda",
                alPresionar = {
                    navegar(Rutas.AGENDA)
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Reserva #${reserva.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Estado: ${reserva.estado}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    Text(
                        text = "Información de la sala",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Sala: ${sala?.nombre ?: reserva.codigoSala}"
                    )

                    Text(
                        text = "Código: ${reserva.codigoSala}"
                    )

                    if (sala != null) {
                        Text(
                            text = "Ubicación: ${sala.ubicacion}"
                        )

                        Text(
                            text = "Capacidad: ${sala.capacidad} personas"
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = "Información de la reserva",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Fecha: ${
                            reserva.fecha.format(formatoFecha)
                                .replaceFirstChar { it.uppercase() }
                        }"
                    )

                    Text(
                        text = "Horario: ${reserva.horaInicio.format(formatoHora)} a ${reserva.horaFin.format(formatoHora)}"
                    )

                    Text(
                        text = "Cantidad de bloques: ${reserva.cantidadBloques}"
                    )

                    Text(
                        text = "Motivo: ${reserva.motivo}"
                    )
                }
            }

            BotonPrincipal(
                texto = "Volver a Mi agenda",
                alPresionar = {
                    volver()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
