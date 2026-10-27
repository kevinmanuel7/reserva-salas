package cl.duoc.reservasalas.app.ui.pantallas.checkin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion


@Composable
fun CheckInPantalla(
    idReserva: Int,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Kevin): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Check-in", dueno = "Kevin", modifier = modifier, alVolver = volver)
}
