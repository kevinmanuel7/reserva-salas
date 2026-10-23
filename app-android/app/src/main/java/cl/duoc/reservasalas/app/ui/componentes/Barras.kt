package cl.duoc.reservasalas.app.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.R
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.Blanco
import cl.duoc.reservasalas.app.ui.theme.GrisSobreNegro
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme

/**
 * Marcador del logo de Duoc UC Sede Viña del Mar (versión blanca, sobre negro).
 * Cuando el mandante entregue el archivo del logo, se reemplaza por una Image.
 */
@Composable
fun LogoDuoc(modifier: Modifier = Modifier, grande: Boolean = false) {
    Box(
        modifier = modifier
            .border(1.dp, GrisSobreNegro, RoundedCornerShape(4.dp))
            .padding(horizontal = if (grande) 20.dp else 8.dp, vertical = if (grande) 12.dp else 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "DUOC UC",
            color = Blanco,
            fontWeight = FontWeight.Black,
            fontSize = if (grande) 22.sp else 12.sp,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Barra superior de todas las pantallas (menos el login): fondo negro, línea amarilla abajo.
 * - Pantallas principales (las de la barra inferior): sin [alVolver] → muestra el logo.
 * - Pantallas secundarias (Detalle, Reservar...): con [alVolver] → muestra la flecha atrás.
 * El fondo negro se extiende detrás de la barra de estado del teléfono (statusBarsPadding).
 */
@Composable
fun BarraSuperior(
    titulo: String,
    modifier: Modifier = Modifier,
    alVolver: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NegroDuoc)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (alVolver != null) {
                IconButton(onClick = alVolver) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volver),
                        contentDescription = "Volver",
                        tint = Blanco
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
                LogoDuoc()
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = titulo,
                color = Blanco,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(AmarilloDuoc)
        )
    }
}

/** Un ítem de la barra inferior: a qué ruta lleva, su texto y su ícono (res/drawable). */
data class ItemBarraInferior(
    val ruta: String,
    val texto: String,
    @param:DrawableRes val icono: Int
)

/**
 * Barra inferior: fondo negro, ítem activo en amarillo.
 * 4 ítems para el relator y 5 para el coordinador (los arma la navegación).
 */
@Composable
fun BarraInferior(
    items: List<ItemBarraInferior>,
    rutaActual: String?,
    alElegir: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NegroDuoc)
            .navigationBarsPadding()
            .height(64.dp)
    ) {
        items.forEach { item ->
            val activo = item.ruta == rutaActual
            val color = if (activo) AmarilloDuoc else GrisSobreNegro
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .selectable(selected = activo, role = Role.Tab, onClick = { alElegir(item.ruta) }),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(item.icono),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = item.texto,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview
@Composable
private fun BarrasPreview() {
    ReservaSalasTheme {
        Column {
            BarraSuperior(titulo = "Salas")
            BarraSuperior(titulo = "Reservar LAB-01", alVolver = {})
            BarraInferior(
                items = listOf(
                    ItemBarraInferior("a", "Inicio", R.drawable.ic_inicio),
                    ItemBarraInferior("b", "Salas", R.drawable.ic_salas),
                    ItemBarraInferior("c", "Mi agenda", R.drawable.ic_agenda),
                    ItemBarraInferior("d", "Perfil", R.drawable.ic_perfil)
                ),
                rutaActual = "b",
                alElegir = {}
            )
        }
    }
}
