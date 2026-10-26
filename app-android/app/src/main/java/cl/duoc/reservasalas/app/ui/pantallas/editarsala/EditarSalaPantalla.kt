package cl.duoc.reservasalas.app.ui.pantallas.editarsala

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
 * Editar sala (ruta editarSala/{codigo}). Dueño: Bastián.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarSalaPantalla(
    codigo: String,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombreSala by remember { mutableStateOf("Laboratorio Computación") }
    var capacidad by remember { mutableStateOf("30") }
    var estaHabilitada by remember { mutableStateOf(true) }
    var tieneProyector by remember { mutableStateOf(true) }
    var tieneAireAcondicionado by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Sala $codigo",
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información General",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuocBlack
                    )

                    OutlinedTextField(
                        value = nombreSala,
                        onValueChange = { nombreSala = it },
                        label = { Text("Nombre de la Sala") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = capacidad,
                        onValueChange = { capacidad = it },
                        label = { Text("Capacidad (personas)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado (Habilitada)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuocBlack
                        )
                        Switch(
                            checked = estaHabilitada,
                            onCheckedChange = { estaHabilitada = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DuocYellow)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(container