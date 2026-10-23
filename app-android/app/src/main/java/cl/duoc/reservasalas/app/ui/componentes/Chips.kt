package cl.duoc.reservasalas.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.ui.theme.AmarilloSuave
import cl.duoc.reservasalas.app.ui.theme.Blanco
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.Fondo
import cl.duoc.reservasalas.app.ui.theme.GrisCancelada
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.RojoError
import cl.duoc.reservasalas.app.ui.theme.Superficie
import cl.duoc.reservasalas.app.ui.theme.VerdeExito

private val FormaChip = RoundedCornerShape(50)

/** Base de todos los chips: texto chico dentro de una "píldora". */
@Composable
private fun Chip(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    borde: Color? = null
) {
    val conBorde = if (borde != null) Modifier.border(1.dp, borde, FormaChip) else Modifier
    Text(
        text = texto,
        color = colorTexto,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier
            .background(fondo, FormaChip)
            .then(conBorde)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Estado de una reserva con su color (especificación, "Estados de reserva"). */
@Composable
fun ChipEstado(estado: EstadoReserva, modifier: Modifier = Modifier) {
    when (estado) {
        EstadoReserva.RESERVADA -> Chip(estado.texto, AmarilloSuave, NegroDuoc, modifier)
        EstadoReserva.EN_USO -> Chip(estado.texto, VerdeExito, Blanco, modifier)
        EstadoReserva.VERIFICADA -> Chip(estado.texto, Superficie, VerdeExito, modifier, borde = VerdeExito)
        EstadoReserva.NO_PRESENTADO -> Chip(estado.texto, RojoError, Blanco, modifier)
        EstadoReserva.CANCELADA -> Chip(estado.texto, GrisCancelada, Blanco, modifier)
    }
}

/**
 * Nombre de un equipo (gris claro). Con [conFalla] = true se ve rojo:
 * se usa para el chip "Con falla" de las tarjetas de sala.
 */
@Composable
fun ChipEquipo(nombre: String, modifier: Modifier = Modifier, conFalla: Boolean = false) {
    if (conFalla) {
        Chip(nombre, RojoError, Blanco, modifier)
    } else {
        Chip(nombre, Fondo, NegroDuoc, modifier, borde = Borde)
    }
}

/** Rol del usuario ("Relator" o "Coordinador"), en negro. */
@Composable
fun ChipRol(rol: Rol, modifier: Modifier = Modifier) {
    val texto = when (rol) {
        Rol.RELATOR -> "Relator"
        Rol.COORDINADOR -> "Coordinador"
    }
    Chip(texto, NegroDuoc, Blanco, modifier)
}

@Preview(showBackground = true)
@Composable
private fun ChipsPreview() {
    ReservaSalasTheme {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            EstadoReserva.entries.forEach { ChipEstado(it) }
        }
    }
}
