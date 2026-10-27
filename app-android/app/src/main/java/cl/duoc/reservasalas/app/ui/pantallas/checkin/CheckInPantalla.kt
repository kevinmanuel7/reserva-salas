package cl.duoc.reservasalas.app.ui.pantallas.checkin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.data.falso.DatosFalsos
import cl.duoc.reservasalas.app.data.modelo.formatearHora
import cl.duoc.reservasalas.app.ui.componentes.BarraSuperior
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.componentes.BotonSecundario
import cl.duoc.reservasalas.app.ui.componentes.FormaTarjeta
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.Blanco
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.Fondo
import cl.duoc.reservasalas.app.ui.theme.Gris85
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.RojoError
import cl.duoc.reservasalas.app.ui.theme.Superficie
import cl.duoc.reservasalas.app.ui.theme.VerdeExito
import java.time.LocalDate
import java.time.LocalTime


@Composable
fun CheckInPantalla(
    idReserva: Int,
    volver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckInViewModel = viewModel(factory = CheckInViewModel.factory(idReserva))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var uriPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }

    // 1. Lanzador de la cámara: recibe true si la foto quedó guardada.
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        val uri = uriPendiente
        if (guardada && uri != null) {
            viewModel.procesarFoto(uri)
        }
        uriPendiente = null
    }

    fun abrirCamara() {
        val uri = viewModel.crearArchivoFoto()
        uriPendiente = uri
        lanzadorCamara.launch(uri)
    }

    // 2. Lanzador del permiso: recibe true si el usuario aceptó.
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            viewModel.mostrarError("Sin permiso de cámara no se puede escanear. Puedes ingresar el código a mano.")
        }
    }

    // 3. Lo que hace el botón "Escanear".
    fun escanear() {
        val tieneCamara = contexto.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!tieneCamara) {
            viewModel.mostrarError("Este teléfono no tiene cámara. Ingresa el código a mano.")
            return
        }
        val permitido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (permitido) {
            abrirCamara()
        } else {
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    CheckInContenido(
        uiState = uiState,
        alEscanear = { escanear() },
        alCambiarCodigo = viewModel::onCodigoManualCambio,
        alConfirmarCodigo = viewModel::confirmarCodigoManual,
        alVolver = volver,
        modifier = modifier
    )
}

/** Dibuja la pantalla a partir del estado. No conoce al ViewModel, por eso sirve en el Preview. */
@Composable
fun CheckInContenido(
    uiState: CheckInUiState,
    alEscanear: () -> Unit,
    alCambiarCodigo: (String) -> Unit,
    alConfirmarCodigo: () -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = uiState.estado
    val reserva = uiState.reserva
    val procesando = estado is EstadoCheckIn.Procesando

    Column(modifier.fillMaxSize().background(Fondo)) {
        BarraSuperior(titulo = "Check-in", alVolver = alVolver)

        if (estado is EstadoCheckIn.Exito) {
            CheckInExitoso(codigoSala = reserva?.codigoSala.orEmpty(), hora = estado.hora, alVolver = alVolver)
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tarjeta con la reserva.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie, FormaTarjeta)
                        .border(1.dp, Borde, FormaTarjeta)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Sala esperada", fontSize = 13.sp, color = Gris85)
                        Text(reserva?.codigoSala ?: "-", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NegroDuoc)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Horario", fontSize = 13.sp, color = Gris85)
                        Text(reserva?.horario ?: "-", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)
                    }
                }

                // Visor decorativo con el marco amarillo.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(NegroDuoc, FormaTarjeta)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    Box(Modifier.size(120.dp).border(4.dp, AmarilloDuoc, RoundedCornerShape(14.dp)))
                    Text(
                        text = "Toma una foto del código QR pegado en la puerta de la sala",
                        color = Blanco,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "La primera vez, Android te pedirá permiso para usar la cámara. " +
                            "Si lo rechazas, puedes ingresar el código de la sala a mano.",
                    fontSize = 13.sp,
                    color = Gris85
                )

                if (estado is EstadoCheckIn.Error) {
                    Text(estado.mensaje, color = RojoError, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                // Ingreso manual: respaldo si el QR está dañado o no hay permiso de cámara.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie, FormaTarjeta)
                        .border(1.dp, Borde, FormaTarjeta)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ingresar código manualmente", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NegroDuoc)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.codigoManual,
                            onValueChange = alCambiarCodigo,
                            placeholder = { Text("Ej. LAB-01") },
                            singleLine = true,
                            enabled = !procesando,
                            modifier = Modifier.weight(1f)
                        )
                        BotonSecundario(
                            texto = "Confirmar",
                            alPresionar = alConfirmarCodigo,
                            habilitado = uiState.codigoManual.isNotBlank() && !procesando
                        )
                    }
                }
            }

            // Botón principal fijo abajo.
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Superficie)
                    .padding(16.dp)
            ) {
                BotonPrincipal(
                    texto = "Escanear QR de la sala",
                    alPresionar = alEscanear,
                    cargando = procesando,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Lo que se ve cuando el check-in quedó registrado. */
@Composable
private fun CheckInExitoso(codigoSala: String, hora: LocalTime, alVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(VerdeExito, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = Blanco, fontSize = 44.sp, fontWeight = FontWeight.Bold)
        }
        Text("¡Check-in realizado!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = NegroDuoc)
        Text(
            text = "Sala $codigoSala a las ${formatearHora(hora)}. La reserva quedó En uso y el coordinador podrá verificarla.",
            fontSize = 14.sp,
            color = Gris85,
            textAlign = TextAlign.Center
        )
        BotonPrincipal(texto = "Volver", alPresionar = alVolver, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun CheckInContenidoErrorPreview() {
    ReservaSalasTheme {
        CheckInContenido(
            uiState = CheckInUiState(
                reserva = DatosFalsos.reservas(LocalDate.now()).first(),
                estado = EstadoCheckIn.Error("Este código es de la sala S-102. Tu reserva es en LAB-01.")
            ),
            alEscanear = {},
            alCambiarCodigo = {},
            alConfirmarCodigo = {},
            alVolver = {}
        )
    }
}