package cl.duoc.reservasalas.app.ui.pantallas.reservasdia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ui.navegacion.Rutas

val DuocBlack = Color(0xFF2D2926)
val DuocYellow = Color(0xFFFFB81C)
val BackgroundGray = Color(0xFFF4F4F6)

data class ReservaItem(
    val id: String,
    val sala: String,
    val docente: String,
    val horario: String,
    val estado: String
)

/**
 * Reservas (ruta reservasDia). Dueño: Bastián.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasDiaPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listaReservas = listOf(
        ReservaItem("1", "S-101", "Carlos Mendoza", "08:30 - 10:00", "En Uso"),
        ReservaItem("2", "S-102", "Ana María Silva", "10:15 - 11:45", "Confirmada"),
        ReservaItem("3", "L-201", "Roberto Gómez", "12:00 - 13:30", "Pendiente"),
        ReservaItem("4", "S-305", "Patricia Morales", "14:30 - 16:00", "Confirmada")
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reservas del Día",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navegar("volver") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DuocBlack
                )
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Programación Diaria",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuocBlack
                )
                Text(
                    text = "Salas reservadas para la jornada de hoy",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(listaReservas) { reserva ->
                ReservaCard(
                    reserva = reserva,
                    onClickDetalle = { navegar(Rutas.SUPERVISAR) }
                )
            }
        }
    }
}

@Composable
private fun ReservaCard(
    reserva: ReservaItem,
    onClickDetalle: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sala ${reserva.sala}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuocBlack
                )
                EstadoChip(estado = reserva.estado)
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            Text(
                text = "Docente: ${reserva.docente}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            Text(
                text = "Horario: ${reserva.horario}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Button(
                onClick = onClickDetalle,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DuocYellow,
                    contentColor = DuocBlack
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ver Supervisión",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EstadoChip(estado: String) {
    val (bgColor, textColor) = when (estado) {
        "En Uso" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "Confirmada" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        else -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = estado,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}