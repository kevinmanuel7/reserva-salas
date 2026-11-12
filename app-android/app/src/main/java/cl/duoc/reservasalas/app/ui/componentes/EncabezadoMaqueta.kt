package cl.duoc.reservasalas.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc

@Composable
fun EncabezadoMaqueta(titulo: String, volver: (() -> Unit)? = null) {
    Column {
        Row(Modifier.fillMaxWidth().heightIn(min = 68.dp).background(NegroDuoc).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (volver != null) {
                TextButton(onClick = volver) { Text("‹", fontSize = 32.sp, color = Color.White) }
            }
            Text(titulo, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.fillMaxWidth().height(4.dp).background(AmarilloDuoc))
    }
}
