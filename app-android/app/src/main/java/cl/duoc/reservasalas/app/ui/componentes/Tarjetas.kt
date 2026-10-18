package cl.duoc.reservasalas.app.ui.componentes

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.R
import cl.duoc.reservasalas.app.data.falso.DatosFalsos
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.AmarilloSuave
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.Gris85
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.Superficie

/** Forma de todas las tarjetas de la app. */
val FormaTarjeta = RoundedCornerShape(12.dp)

/** Fondo blanco, borde gris y esquinas redondeadas. Si recibe [alPresionar], se puede tocar. */
private fun Modifier.estiloTarjeta(alPresionar: (() -> Unit)?): Modifier {
    val base = this
        .background(Superficie, FormaTarjeta)
        .border(1.dp, Borde, FormaTarjeta)
    return if (alPresionar != null) base.clickable(onClick = alPresionar) else base
}

/**
 * Tarjeta de una sala para las listas: código, capacidad, nombre y chips de equipamiento.
 * Si algún equipo tiene falla, agrega el chip rojo "Con falla".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaSala(
    sala: Sala,
    alPresionar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .estiloTarjeta(alPresionar)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(sala.codigo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_personas),
                contentDescription = null,
                tint = Gris85,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("${sala.capacidad} personas", fontSize = 13.sp, color = Gris85)
        }
        Text(sala.nombre, fontSize = 14.sp, color = NegroDuoc)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sala.equipos.forEach { ChipEquipo(it.nombre) }
            if (sala.tieneFallas) ChipEquipo("Con falla", conFalla = true)
        }
    }
}

/**
 * Número grande con una descripción debajo (ej. "4" / "reservas para hoy").
 * Se usa en el Inicio del coordinador y en el Perfil.
 */
@Composable
fun TarjetaMetrica(
    numero: String,
    descripcion: String,
    modifier: Modifier = Modifier,
    colorNumero: Color = NegroDuoc,
    alPresionar: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .estiloTarjeta(alPresionar)
            .padding(14.dp)
    ) {
        Text(numero, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = colorNumero)
        Text(descripcion, fontSize = 13.sp, color = Gris85)
    }
}

/**
 * Aviso destacado en amarillo suave con una campana (ej. el recordatorio de 15 minutos).
 * Si recibe [alPresionar], se puede tocar.
 */
@Composable
fun AvisoDestacado(
    texto: String,
    modifier: Modifier = Modifier,
    alPresionar: (() -> Unit)? = null
) {
    val tocable = if (alPresionar != null) Modifier.clickable(onClick = alPresionar) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AmarilloSuave, FormaTarjeta)
            .border(1.dp, AmarilloDuoc, FormaTarjeta)
            .then(tocable)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_campana),
            contentDescription = null,
            tint = NegroDuoc,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(texto, fontSize = 14.sp, color = NegroDuoc)
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetasPreview() {
    ReservaSalasTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AvisoDestacado("Tu reserva en LAB-01 empieza a las 10:05")
            TarjetaSala(DatosFalsos.salas.first(), alPresionar = {})
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TarjetaMetrica("4", "reservas para hoy", Modifier.weight(1f))
                TarjetaMetrica("1", "sala con fallas", Modifier.weight(1f))
            }
        }
    }
}
