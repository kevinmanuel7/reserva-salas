package cl.duoc.reservasalas.app.ui.pantallas.editarsala

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import cl.duoc.reservasalas.app.ui.theme.Blanco
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.RojoError
import cl.duoc.reservasalas.app.ui.theme.Superficie
import cl.duoc.reservasalas.app.ui.theme.VerdeExito

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        BarraSuperior(titulo = "Editar Sala $codigo", alVolver = volver)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Equipamiento",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuocBlack
                    )

                    EquipoEditable(
                        nombre = "Proyector",
                        operativo = tieneProyector,
                        alCambiar = { tieneProyector = it }
                    )

                    HorizontalDivider(color = Borde)

                    EquipoEditable(
                        nombre = "Climatización",
                        operativo = tieneAireAcondicionado,
                        alCambiar = { tieneAireAcondicionado = it }
                    )
                }
            }
        }

        // Botones fijos abajo, como en la imagen 04-Editar-sala.
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
                texto = "Guardar cambios",
                alPresionar = volver,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

/**
 * Un equipo de la sala con su selector "Operativo / Con falla".
 * Si tiene falla, pide un comentario (obligatorio según la imagen 04-Editar-sala).
 */
@Composable
private fun EquipoEditable(
    nombre: String,
    operativo: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    var comentario by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = nombre,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NegroDuoc
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = operativo,
                onClick = { alCambiar(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = VerdeExito,
                    activeContentColor = Blanco
                ),
                icon = {}
            ) {
                Text("Operativo")
            }
            SegmentedButton(
                selected = !operativo,
                onClick = { alCambiar(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = RojoError,
                    activeContentColor = Blanco
                ),
                icon = {}
            ) {
                Text("Con falla")
            }
        }

        if (!operativo) {
            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario (obligatorio si tiene falla)") },
                modifier = Modifier.fillMaxWidth(),
                isError = comentario.isBlank()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun EditarSalaPantallaPreview() {
    ReservaSalasTheme {
        EditarSalaPantalla(codigo = "S-101", volver = {})
    }
}
