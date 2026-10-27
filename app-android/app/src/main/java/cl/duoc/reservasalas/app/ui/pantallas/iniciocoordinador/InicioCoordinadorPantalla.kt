package cl.duoc.reservasalas.app.ui.pantallas.iniciocoordinador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ui.navegacion.Rutas

// Colores Corporativos Duoc UC
val DuocBlack = Color(0xFF2D2926)
val DuocYellow = Color(0xFFFFB81C)
val BackgroundGray = Color(0xFFF4F4F6)

/**
 * Inicio (ruta inicioCoordinador). Dueño: Bastián.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioCoordinadorPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Panel Coordinador",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Bienvenido, Coordinador",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuocBlack
                )
                Text(
                    text = "Gestión y supervisión de salas de clases",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Opción 1: Supervisar Reservas
            item {
                OpcionMenuCard(
                    titulo = "Supervisar Reserva",
                    descripcion = "Revisar estado e incidencias de una reserva activa",
                    textoBoton = "Supervisar",
                    onClick = { navegar(Rutas.RESERVAS_DIA) }
                )
            }

            // Opción 2: Reservas del Día
            item {
                OpcionMenuCard(
                    titulo = "Reservas del Día",
                    descripcion = "Consulta la programación diaria de todas las salas",
                    textoBoton = "Ver Reservas",
                    onClick = { navegar(Rutas.RESERVAS_DIA) }
                )
            }

            // Opción 3: Horario General
            item {
                OpcionMenuCard(
                    titulo = "Horario General",
                    descripcion = "Ver disponibilidad general por bloque horario",
                    textoBoton = "Ver Horarios",
                    onClick = { navegar(Rutas.HORARIO_GENERAL) }
                )
            }

            // Opción 4: Editar Sala
            item {
                OpcionMenuCard(
                    titulo = "Administrar / Editar Sala",
                    descripcion = "Modificar equipamiento, capacidad o estado de sala",
                    textoBoton = "Editar Sala",
                    onClick = { navegar(Rutas.EDITAR_SALA) }
                )
            }
        }
    }
}

@Composable
private fun OpcionMenuCard(
    titulo: String,
    descripcion: String,
    textoBoton: String,
    onClick: () -> Unit
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
                .padding(16.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuocBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = descripcion,
                fontSize = 13.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DuocYellow,
                    contentColor = DuocBlack
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = textoBoton,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}