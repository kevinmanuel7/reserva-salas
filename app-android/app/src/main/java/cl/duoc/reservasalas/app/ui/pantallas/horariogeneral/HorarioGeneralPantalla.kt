
package cl.duoc.reservasalas.app.ui.pantallas.horariogeneral

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

private val FondoHorario = Color(0xFFF2F3F5)
private val OscuroHorario = Color(0xFF2D2927)
private val AmarilloHorario = Color(0xFFFFB91F)
private val TextoHorario = Color(0xFF302D2B)
private val SecundarioHorario = Color(0xFF605D5A)
private val BordeHorario = Color(0xFFDADADA)
private val RojoHorario = Color(0xFFD32F2F)

@Composable
fun HorarioGeneralPantalla(
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as ReservaSalasApp
    val repo = app.container.repositorioHorario
    val actual by repo.horario.collectAsState()

    var apertura by remember(actual) {
        mutableStateOf(actual.apertura.hour.toString())
    }

    var cierre by remember(actual) {
        mutableStateOf(actual.cierre.hour.toString())
    }

    var pausa by remember(actual) {
        mutableStateOf(actual.pausaMinutos.toString())
    }

    var dias by remember(actual) {
        mutableStateOf(actual.dias)
    }

    var error by remember {
        mutableStateOf("")
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    val diasSemana = listOf(
        DayOfWeek.MONDAY to "Lun",
        DayOfWeek.TUESDAY to "Mar",
        DayOfWeek.WEDNESDAY to "Mié",
        DayOfWeek.THURSDAY to "Jue",
        DayOfWeek.FRIDAY to "Vie",
        DayOfWeek.SATURDAY to "Sáb",
        DayOfWeek.SUNDAY to "Dom"
    )

    val horaApertura = apertura.toIntOrNull()
    val horaCierre = cierre.toIntOrNull()
    val minutosPausa = pausa.toIntOrNull()

    val horarioValido =
        horaApertura != null &&
                horaCierre != null &&
                minutosPausa != null &&
                horaApertura in 0..22 &&
                horaCierre in 1..23 &&
                horaCierre > horaApertura &&
                minutosPausa in 0..15 &&
                dias.isNotEmpty()

    val vistaPrevia = if (horarioValido) {
        HorarioGeneral(
            apertura = LocalTime.of(horaApertura!!, 0),
            cierre = LocalTime.of(horaCierre!!, 0),
            pausaMinutos = minutosPausa!!,
            dias = dias
        )
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoHorario)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OscuroHorario)
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
                        volver()
                    }
                )

                Text(
                    text = "Horario general",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(AmarilloHorario)
            )
        }

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
                    BordeHorario
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Configuración de horarios",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoHorario
                    )

                    Text(
                        text = "Este horario aplica a todas las salas que utilizan el horario general.",
                        fontSize = 14.sp,
                        color = SecundarioHorario,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(
                        color = BordeHorario
                    )

                    Text(
                        text = "Horario de funcionamiento",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoHorario
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = apertura,
                            onValueChange = {
                                apertura = it
                                    .filter(Char::isDigit)
                                    .take(2)
                                error = ""
                            },
                            label = {
                                Text("Apertura")
                            },
                            supportingText = {
                                Text("Hora de 0 a 22")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = cierre,
                            onValueChange = {
                                cierre = it
                                    .filter(Char::isDigit)
                                    .take(2)
                                error = ""
                            },
                            label = {
                                Text("Cierre")
                            },
                            supportingText = {
                                Text("Hora de 1 a 23")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Text(
                        text = "Las horas se ingresan en formato de 24 horas.",
                        fontSize = 13.sp,
                        color = SecundarioHorario
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    BordeHorario
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pausa entre bloques",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoHorario
                    )

                    Text(
                        text = "Define los minutos de pausa entre los bloques de reserva.",
                        fontSize = 14.sp,
                        color = SecundarioHorario
                    )

                    OutlinedTextField(
                        value = pausa,
                        onValueChange = {
                            pausa = it
                                .filter(Char::isDigit)
                                .take(2)
                            error = ""
                        },
                        label = {
                            Text("Minutos de pausa")
                        },
                        supportingText = {
                            Text("Permitido: 0 a 15 minutos")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    BordeHorario
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Días habilitados",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoHorario
                    )

                    Text(
                        text = "Selecciona los días en que las salas estarán disponibles.",
                        fontSize = 14.sp,
                        color = SecundarioHorario
                    )

                    diasSemana.chunked(4).forEach { grupo ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            grupo.forEach { (dia, nombre) ->
                                val seleccionado = dia in dias

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            dias = if (seleccionado) {
                                                dias - dia
                                            } else {
                                                dias + dia
                                            }
                                            error = ""
                                        },
                                    color = if (seleccionado) {
                                        AmarilloHorario
                                    } else {
                                        FondoHorario
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (seleccionado) {
                                            AmarilloHorario
                                        } else {
                                            BordeHorario
                                        }
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(46.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = nombre,
                                            fontSize = 14.sp,
                                            fontWeight = if (seleccionado) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Medium
                                            },
                                            color = TextoHorario
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    BordeHorario
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Vista previa del horario",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoHorario
                    )

                    HorizontalDivider(
                        color = BordeHorario
                    )

                    Text(
                        text = if (vistaPrevia != null) {
                            "${vistaPrevia.bloques().size} bloques disponibles por día habilitado"
                        } else {
                            "Ingresa un horario válido para ver la vista previa."
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoHorario
                    )

                    if (vistaPrevia != null) {
                        Text(
                            text = "Horario: ${horaApertura}:00 a ${horaCierre}:00",
                            fontSize = 14.sp,
                            color = SecundarioHorario
                        )

                        Text(
                            text = "Pausa: $minutosPausa minutos",
                            fontSize = 14.sp,
                            color = SecundarioHorario
                        )

                        Text(
                            text = "Días seleccionados: ${dias.size}",
                            fontSize = 14.sp,
                            color = SecundarioHorario
                        )
                    }
                }
            }

            if (error.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = RojoHorario.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        RojoHorario.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(14.dp),
                        color = RojoHorario,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Surface(
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = volver,
                    enabled = !guardando,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        OscuroHorario
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextoHorario,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val a = apertura.toIntOrNull()
                        val c = cierre.toIntOrNull()
                        val p = pausa.toIntOrNull()

                        if (
                            a == null ||
                            c == null ||
                            p == null ||
                            a !in 0..22 ||
                            c !in 1..23 ||
                            c <= a ||
                            p !in 0..15 ||
                            dias.isEmpty()
                        ) {
                            error = "Revisa las horas, la pausa (0 a 15 minutos) y los días seleccionados."
                        } else {
                            guardando = true

                            scope.launch {
                                try {
                                    repo.guardar(
                                        HorarioGeneral(
                                            apertura = LocalTime.of(a, 0),
                                            cierre = LocalTime.of(c, 0),
                                            pausaMinutos = p,
                                            dias = dias
                                        )
                                    )
                                    volver()
                                } catch (e: Exception) {
                                    error = "No se pudo guardar el horario. Inténtalo nuevamente."
                                } finally {
                                    guardando = false
                                }
                            }
                        }
                    },
                    enabled = !guardando,
                    modifier = Modifier
                        .weight(2f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloHorario,
                        contentColor = TextoHorario
                    )
                ) {
                    if (guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TextoHorario
                        )
                    } else {
                        Text(
                            text = "Guardar horario",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
