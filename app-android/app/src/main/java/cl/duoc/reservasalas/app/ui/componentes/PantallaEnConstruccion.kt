package cl.duoc.reservasalas.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ui.theme.Fondo
import cl.duoc.reservasalas.app.ui.theme.Gris85
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc

/**
 * Relleno temporal: lo muestran las pantallas que todavía no se programan.
 * Cada dueño reemplaza su relleno por la pantalla real.
 */
@Composable
fun PantallaEnConstruccion(
    titulo: String,
    dueno: String,
    modifier: Modifier = Modifier,
    alVolver: (() -> Unit)? = null
) {
    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior(titulo = titulo, alVolver = alVolver)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pantalla en construcción",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NegroDuoc
            )
            Text(
                text = "A cargo de: $dueno",
                fontSize = 14.sp,
                color = Gris85,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
