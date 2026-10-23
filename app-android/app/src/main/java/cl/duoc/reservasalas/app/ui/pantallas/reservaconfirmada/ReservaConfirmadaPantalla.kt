
package cl.duoc.reservasalas.app.ui.pantallas.reservaconfirmada

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.componentes.BotonSecundario
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReservaConfirmadaPantalla(
    idReserva: Int,
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ReservaConfirmadaViewModel = viewModel(
        factory = ReservaConfirmadaViewModel.Factory
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = "¡Reserva confirmada!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tu reserva se ha registrado correctamente",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (reserva == null) {
            Text(
                text = "No se encontró la información de la reserva",
                color = MaterialTheme.colorScheme.error
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
                        text = "Detalle de la reserva",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    Text(
                        text = "Número de reserva: #${reserva.id}",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Estado: ${reserva.estado}"
                    )

                    Text(
                        text = "Sala: ${sala?.nombre ?: reserva.codigoSala}"
                    )

                    Text(
                        text = "Código: ${reserva.codigoSala}"
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
                        text = "Bloques reservados: ${reserva.cantidadBloques}"
                    )

                    Text(
                        text = "Motivo: ${reserva.motivo}"
                    )
                }
            }
        }

        BotonPrincipal(
            texto = "Ver mi agenda",
            alPresionar = {
                navegar(Rutas.AGENDA)
            },
            modifier = Modifier.fillMaxWidth()
        )

        BotonSecundario(
            texto = "Volver al inicio",
            alPresionar = {
                navegar(Rutas.INICIO_RELATOR)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
