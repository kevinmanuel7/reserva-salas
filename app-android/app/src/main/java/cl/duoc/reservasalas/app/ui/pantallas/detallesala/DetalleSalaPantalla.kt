
package cl.duoc.reservasalas.app.ui.pantallas.detallesala

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
import cl.duoc.reservasalas.app.data.modelo.EstadoEquipo
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import java.time.format.DateTimeFormatter

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

    val salas = viewModel.salas.collectAsState().value
    val sala = salas.find { it.codigo == codigo }

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

        if (sala == null) {
            Text(
                text = "No se encontró la sala",
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            Text(
                text = sala.codigo,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = sala.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información de la sala",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Capacidad: ${sala.capacidad} personas"
                    )

                    Text(
                        text = "Ubicación: ${sala.ubicacion}"
                    )

                    Text(
                        text = if (sala.horarioPropio == null) {
                            "Horario: General"
                        } else {
                            "Horario: ${sala.horarioPropio.apertura.format(formatoHora)} a ${sala.horarioPropio.cierre.format(formatoHora)}"
                        }
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "Equipamiento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (sala.equipos.isEmpty()) {
                Text("Esta sala no tiene equipamiento registrado")
            } else {
                sala.equipos.forEach { equipo ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = equipo.nombre,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Estado: ${equipo.estado.texto}"
                            )

                            if (equipo.estado == EstadoEquipo.CON_FALLA) {
                                equipo.comentario?.let { comentario ->
                                    Text(
                                        text = "Observación: $comentario",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            BotonPrincipal(
                texto = "Reservar esta sala",
                alPresionar = {
                    navegar(Rutas.reservar(sala.codigo))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
