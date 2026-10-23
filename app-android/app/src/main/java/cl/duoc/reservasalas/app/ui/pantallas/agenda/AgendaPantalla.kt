
package cl.duoc.reservasalas.app.ui.pantallas.agenda

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun AgendaPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AgendaViewModel = viewModel(
        factory = AgendaViewModel.Factory
    )

    val reservas = viewModel.reservas.collectAsState().value
    val salas = viewModel.salas.collectAsState().value
    val idUsuario = viewModel.idUsuario.collectAsState().value

    val misReservas = reservas
        .filter { reserva ->
            reserva.idUsuario == idUsuario
        }
        .sortedWith(
            compareBy(
                { it.fecha },
                { it.horaInicio }
            )
        )

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
        Text(
            text = "Mi agenda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Consulta tus reservas de salas",
            style = MaterialTheme.typography.bodyLarge
        )

        HorizontalDivider()

        if (idUsuario == null) {
            Text("Cargando tus reservas...")
        } else if (misReservas.isEmpty()) {
            Text(
                text = "Todavía no tienes reservas registradas",
                style = MaterialTheme.typography.titleMedium
            )

            BotonPrincipal(
                texto = "Reservar una sala",
                alPresionar = {
                    navegar(Rutas.SALAS)
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Mis reservas: ${misReservas.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            misReservas.forEach { reserva ->
                val sala = salas.find {
                    it.codigo == reserva.codigoSala
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navegar(Rutas.reserva(reserva.id))
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = reserva.codigoSala,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = reserva.estado.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = sala?.nombre ?: reserva.codigoSala,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = reserva.fecha.format(formatoFecha)
                                .replaceFirstChar { it.uppercase() }
                        )

                        Text(
                            text = "${reserva.horaInicio.format(formatoHora)} a ${reserva.horaFin.format(formatoHora)}"
                        )

                        Text(
                            text = "Motivo: ${reserva.motivo}"
                        )

                        Text(
                            text = "Ver detalle →",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            BotonPrincipal(
                texto = "Reservar otra sala",
                alPresionar = {
                    navegar(Rutas.SALAS)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
