
package cl.duoc.reservasalas.app.ui.pantallas.checkin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import java.time.LocalDate
import java.time.LocalTime

private val FondoCheckIn = Color(0xFFF2F3F5)
private val OscuroCheckIn = Color(0xFF2D2927)
private val AmarilloCheckIn = Color(0xFFFFB91F)
private val TextoCheckIn = Color(0xFF302D2B)
private val SecundarioCheckIn = Color(0xFF605D5A)
private val BordeCheckIn = Color(0xFFDADADA)
private val VerdeCheckIn = Color(0xFF388E3C)
private val RojoCheckIn = Color(0xFFD32F2F)

@Composable
fun CheckInPantalla(
    idReserva: Int,
    volver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckInViewModel = viewModel(
        factory = CheckInViewModel.factory(idReserva)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    var uriPendiente by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { guardada ->
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

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            viewModel.mostrarError(
                "Sin permiso de cámara no se puede escanear. Puedes ingresar el código a mano."
            )
        }
    }

    fun escanear() {
        val tieneCamara = contexto.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY
        )

        if (!tieneCamara) {
            viewModel.mostrarError(
                "Este teléfono no tiene cámara. Ingresa el código a mano."
            )
            return
        }

        val permitido = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permitido) {
            abrirCamara()
        } else {
            lanzadorPermiso.launch(
                Manifest.permission.CAMERA
            )
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCheckIn)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroCheckIn)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "‹",
                    color = Color.White,
                    fontSize = 38.sp,
                    modifier = Modifier.clickable {
                        alVolver()
                    }
                )

                Text(
                    text = "Check-in",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloCheckIn)
            )
        }

        if (estado is EstadoCheckIn.Exito) {
            CheckInExitoso(
                codigoSala = reserva?.codigoSala.orEmpty(),
                hora = estado.hora,
                alVolver = alVolver
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        BordeCheckIn
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Sala esperada",
                                color = SecundarioCheckIn,
                                fontSize = 13.sp
                            )

                            Text(
                                text = reserva?.codigoSala ?: "-",
                                color = TextoCheckIn,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Horario",
                                color = SecundarioCheckIn,
                                fontSize = 13.sp
                            )

                            Text(
                                text = reserva?.horario ?: "-",
                                color = TextoCheckIn,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = OscuroCheckIn,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "Escanea el código QR",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .size(148.dp)
                                .border(
                                    width = 4.dp,
                                    color = AmarilloCheckIn,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▦",
                                color = AmarilloCheckIn,
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Toma una foto del código QR que está pegado en la puerta de la sala.",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp
                        )
                    }
                }

                Text(
                    text = "La primera vez, Android solicitará permiso para utilizar la cámara. Si no puedes escanear el QR, también puedes ingresar el código manualmente.",
                    color = SecundarioCheckIn,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                if (estado is EstadoCheckIn.Error) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = RojoCheckIn.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            RojoCheckIn.copy(alpha = 0.35f)
                        )
                    ) {
                        Text(
                            text = estado.mensaje,
                            modifier = Modifier.padding(14.dp),
                            color = RojoCheckIn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        BordeCheckIn
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Ingresar código manualmente",
                            color = TextoCheckIn,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Escribe el código de la sala para registrar tu llegada.",
                            color = SecundarioCheckIn,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = uiState.codigoManual,
                            onValueChange = alCambiarCodigo,
                            placeholder = {
                                Text("Ej. LAB-01")
                            },
                            label = {
                                Text("Código de sala")
                            },
                            singleLine = true,
                            enabled = !procesando,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = alConfirmarCodigo,
                            enabled = uiState.codigoManual.isNotBlank() &&
                                    !procesando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OscuroCheckIn,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Confirmar código",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Surface(
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = alEscanear,
                        enabled = !procesando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloCheckIn,
                            contentColor = TextoCheckIn
                        )
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = TextoCheckIn
                            )

                            Spacer(Modifier.width(12.dp))
                        }

                        Text(
                            text = if (procesando) {
                                "Procesando..."
                            } else {
                                "Escanear QR de la sala"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInExitoso(
    codigoSala: String,
    hora: LocalTime,
    alVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCheckIn)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                BordeCheckIn
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            VerdeCheckIn,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "¡Check-in realizado!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextoCheckIn,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tu llegada a la sala $codigoSala fue registrada a las ${formatearHora(hora)}.",
                    fontSize = 15.sp,
                    color = SecundarioCheckIn,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                HorizontalDivider(
                    color = BordeCheckIn
                )

                Text(
                    text = "La reserva quedó En uso y el coordinador podrá verificarla.",
                    fontSize = 14.sp,
                    color = SecundarioCheckIn,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                Button(
                    onClick = alVolver,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloCheckIn,
                        contentColor = TextoCheckIn
                    )
                ) {
                    Text(
                        text = "Volver",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun CheckInContenidoErrorPreview() {
    ReservaSalasTheme {
        CheckInContenido(
            uiState = CheckInUiState(
                reserva = DatosFalsos.reservas(
                    LocalDate.now()
                ).first(),
                estado = EstadoCheckIn.Error(
                    "Este código es de la sala S-102. Tu reserva es en LAB-01."
                )
            ),
            alEscanear = {},
            alCambiarCodigo = {},
            alConfirmarCodigo = {},
            alVolver = {}
        )
    }
}
