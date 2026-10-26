package cl.duoc.reservasalas.app.ui.pantallas.horariogeneral

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Horario General de Salas",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                            .padding(16.