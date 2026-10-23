package cl.duoc.reservasalas.app.ui.pantallas.iniciorelator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.ui.graphics.Color
import cl.duoc.reservasalas.app.ui.navegacion.Rutas

import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.componentes.AvisoDestacado

@Composable
fun InicioRelatorPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: InicioRelatorViewModel = viewModel(
        factory = InicioRelatorViewModel.Factory
    )

    val estado = viewModel.uiState.collectAsState().value

    val fechaActual = estado.fecha

    val formatoFecha = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM",
        Locale("es", "CL")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hola, ${estado.nombre} 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = fechaActual.format(formatoFecha)
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium
        )

        estado.recordatorio?.let { reserva ->
            AvisoDestacado(
                texto = "Tu reserva en ${reserva.codigoSala} comienza a las ${reserva.horario.substringBefore("–")}",
                alPresionar = {
                    navegar(Rutas.reserva(reserva.id))
                }
            )
        }

        HorizontalDivider()

        Text(
            text = "Tu próxima reserva",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (estado.cargando) {
                    Text("Cargando reservas...")
                } else {
                    val reserva = estado.proximaReserva

                    if (reserva == null) {
                        Text("No tienes reservas próximas")
                    } else {
                        Text(
                            text = "Sala: ${reserva.codigoSala}",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Fecha: ${reserva.fecha}"
                        )

                        Text(
                            text = "Horario: ${reserva.horario}"
                        )

                        Text(
                            text = "Estado: ${reserva.estado.texto}"
                        )

                        if (reserva.checkInHabilitado(viewModel.reloj.ahora())) {
                            BotonPrincipal(
                                texto = "Hacer check-in",
                                alPresionar = {
                                    navegar(Rutas.checkin(reserva.id))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    }
                }
            }
        }

        BotonPrincipal(
            texto = "Reservar sala",
            alPresionar = {
                navegar(Rutas.SALAS)
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Esta semana",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    navegar(Rutas.AGENDA)
                }
            ) {
                Text("Ver todas")
            }
        }

        if (estado.reservasSemana.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No tienes reservas próximas esta semana",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            estado.reservasSemana.forEach { reserva ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navegar(Rutas.reserva(reserva.id))
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = reserva.codigoSala,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = reserva.fecha.format(formatoFecha)
                                .replaceFirstChar { it.uppercase() }
                        )

                        Text(
                            text = reserva.horario
                        )
                    }
                }
            }
        }
    }
}
