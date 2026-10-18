package cl.duoc.reservasalas.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.Gris85
import cl.duoc.reservasalas.app.ui.theme.GrisOcupado
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.RojoError
import cl.duoc.reservasalas.app.ui.theme.Superficie

private val FormaBoton = RoundedCornerShape(10.dp)
private val AltoMinimoBoton = 52.dp

/**
 * Acción principal de la pantalla: relleno amarillo, texto negro en negrita.
 * Con [cargando] = true muestra un indicador de carga y no se puede presionar.
 */
@Composable
fun BotonPrincipal(
    texto: String,
    alPresionar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    Button(
        onClick = alPresionar,
        enabled = habilitado && !cargando,
        modifier = modifier.heightIn(min = AltoMinimoBoton),
        shape = FormaBoton,
        colors = ButtonDefaults.buttonColors(
            containerColor = AmarilloDuoc,
            contentColor = NegroDuoc,
            disabledContainerColor = GrisOcupado,
            disabledContentColor = Gris85
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = NegroDuoc,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

/** Acción secundaria: fondo blanco, contorno y texto negro (ej. "Cancelar", "Editar sala"). */
@Composable
fun BotonSecundario(
    texto: String,
    alPresionar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    OutlinedButton(
        onClick = alPresionar,
        enabled = habilitado,
        modifier = modifier.heightIn(min = AltoMinimoBoton),
        shape = FormaBoton,
        border = BorderStroke(1.dp, if (habilitado) NegroDuoc else Borde),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Superficie,
            contentColor = NegroDuoc
        )
    ) {
        Text(text = texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

/** Acción peligrosa: fondo blanco, contorno y texto rojo (ej. "Cancelar reserva", "Cerrar sesión"). */
@Composable
fun BotonPeligro(
    texto: String,
    alPresionar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    OutlinedButton(
        onClick = alPresionar,
        enabled = habilitado,
        modifier = modifier.heightIn(min = AltoMinimoBoton),
        shape = FormaBoton,
        border = BorderStroke(1.dp, if (habilitado) RojoError else Borde),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Superficie,
            contentColor = RojoError
        )
    ) {
        Text(text = texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonesPreview() {
    ReservaSalasTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonPrincipal("Reservar sala", {}, Modifier.fillMaxWidth())
            BotonPrincipal("Guardando", {}, Modifier.fillMaxWidth(), cargando = true)
            BotonSecundario("Cancelar", {}, Modifier.fillMaxWidth())
            BotonPeligro("Cancelar reserva", {}, Modifier.fillMaxWidth())
        }
    }
}
