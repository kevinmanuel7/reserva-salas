package cl.duoc.reservasalas.app.ui.pantallas.supervisar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DuocBlack = Color(0xFF2D2926)
val DuocYellow = Color(0xFFFFB81C)
val BackgroundGray = Color(0xFFF4F4F6)

/**
 * Supervisar reserva (ruta supervisar/{idReserva}). Dueño: Bastián.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisarPantalla(
    idReserva: Int,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var observaciones by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf("En Uso") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Supervisar Reserva #$idReserva",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = volver) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Detalle de la Reserva
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
                        text = "Información de la Reserva",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuocBlack
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sala:", fontWeight = FontWeight.Bold, color = DuocBlack)
                        Text("S-101", color = Color.DarkGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Docente:", fontWeight = FontWeight.Bold, color = DuocBlack)
                        Text("Carlos Mendoza", color = Color.DarkGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Horario:", fontWeight = FontWeight.Bold, color = DuocBlack)
                        Text("08:30 - 10:00", color = Color.DarkGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Asignatura:", fontWeight = FontWeight.Bold, color = DuocBlack)
                        Text("Aplicaciones Móviles", color = Color.DarkGray)
                    }
                }
            }

            // Tarjeta de Registro de Incidencias / Observaciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors