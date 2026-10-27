package cl.duoc.reservasalas.app.ui.pantallas.horariogeneral

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ui.componentes.BarraSuperior
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.componentes.BotonSecundario
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.GrisOcupado
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.Superficie
import cl.duoc.reservasalas.app.ui.theme.VerdeExito

val DuocBlack = Color(0xFF2D2926)
val DuocYellow = Color(0xFFFFB81C)
val BackgroundGray = Color(0xFFF4F4F6)

data class BloqueHorario(
    val hora: String,
    val estadoS101: String,
    val estadoS102: String,
    val estadoL201: String
)

/**
 * Horario general (ruta horarioGeneral). Dueño: Bastián.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioGeneralPantalla(
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bloques = listOf(
        BloqueHorario("08:30 - 10:00", "Ocupado", "Disponible", "Ocupado"),
        BloqueHorario("10:15 - 11:45", "Disponible", "Ocupado", "Disponible"),
        BloqueHorario("12:00 - 13:30", "Ocupado", "Disponible", "Disponible"),
        BloqueHorario("14:30 - 16:00", "Disponible", "Disponible", "Ocupado"),
        BloqueHorario("16:15 - 17:45", "Ocupado", "Ocupado", "Disponible")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        BarraSuperior(titulo = "Horario General de Salas", alVolver = volver)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Disponibilidad por Bloque Horario",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuocBlack
                )
                Text(
                    text = "Vista rápida de ocupación de la sede",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(bloques) { bloque ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = bloque.hora,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuocBlack
                        )

                        HorizontalDivider(color = Borde)

                        EstadoSala(sala = "S-101", estado = bloque.estadoS101)
                        EstadoSala(sala = "S-102", estado = bloque.estadoS102)
                        EstadoSala(sala = "L-201", estado = bloque.estadoL201)
                    }
                }
            }
        }

        // Botones fijos abajo, como en la imagen 05-Horario-general.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Superficie)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BotonSecundario(
                texto = "Cancelar",
                alPresionar = volver,
                modifier = Modifier.weight(1f)
            )
            BotonPrincipal(
                texto = "Guardar",
                alPresionar = volver,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

/** Una fila con el código de la sala y su estado en el bloque: "Disponible" (verde) u "Ocupado" (gris). */
@Composable
private fun EstadoSala(sala: String, estado: String) {
    val disponible = estado == "Disponible"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sala,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = NegroDuoc
        )
        Text(
            text = estado,
            modifier = Modifier
                .background(
                    color = if (disponible) VerdeExito else GrisOcupado,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = if (disponible) Superficie else NegroDuoc,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HorarioGeneralPantallaPreview() {
    ReservaSalasTheme {
        HorarioGeneralPantalla(volver = {})
    }
}
