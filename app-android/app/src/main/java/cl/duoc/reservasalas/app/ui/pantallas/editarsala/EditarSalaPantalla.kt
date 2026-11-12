
package cl.duoc.reservasalas.app.ui.pantallas.editarsala

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.modelo.*
import kotlinx.coroutines.launch
import java.time.LocalTime

private val Fondo = Color(0xFFF2F3F5)
private val Negro = Color(0xFF2D2927)
private val Amarillo = Color(0xFFFFB91F)
private val Texto = Color(0xFF302D2B)
private val Gris = Color(0xFF605D5A)
private val Borde = Color(0xFFDADADA)
private val Rojo = Color(0xFFD32F2F)
private val Verde = Color(0xFF2E7D32)

@Composable
fun EditarSalaPantalla(
    codigo: String,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as ReservaSalasApp
    val repo = app.container.repositorioSalas
    val salas by repo.salas.collectAsState()
    val sala = salas.find { it.codigo == codigo }
    val scope = rememberCoroutineScope()

    var equipos by remember(sala) {
        mutableStateOf(sala?.equipos ?: emptyList())
    }

    var nombre by remember(sala) {
        mutableStateOf(sala?.nombre ?: "")
    }

    var capacidad by remember(sala) {
        mutableStateOf(sala?.capacidad?.toString() ?: "")
    }

    var horarioGeneral by remember(sala) {
        mutableStateOf(sala?.horarioPropio == null)
    }

    var apertura by remember(sala) {
        mutableStateOf(
            (sala?.horarioPropio?.apertura?.hour ?: 8).toString()
        )
    }

    var cierre by remember(sala) {
        mutableStateOf(
            (sala?.horarioPropio?.cierre?.hour ?: 21).toString()
        )
    }

    var error by remember {
        mutableStateOf("")
    }

    var agregar by remember {
        mutableStateOf(false)
    }

    var editandoDatos by remember {
        mutableStateOf(false)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    val horario by app.container.repositorioHorario.horario.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Negro)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "‹",
                    color = Color.White,
                    fontSize = 40.sp,
                    modifier = Modifier.clickable {
                        volver()
                    }
                )

                Text(
                    text = "Editar $codigo",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Amarillo)
            )
        }

        if (sala == null) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Sala no encontrada",
                    color = Rojo
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 16.dp,
                        vertical = 18.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Borde)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Negro)
                                .padding(15.dp)
                        ) {
                            Text(
                                text = "Datos",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DatoSala(
                                titulo = "Código",
                                valor = sala.codigo,
                                modifier = Modifier.weight(1f)
                            )

                            DatoSala(
                                titulo = "Nombre",
                                valor = nombre,
                                modifier = Modifier.weight(1.5f)
                            )

                            DatoSala(
                                titulo = "Capacidad",
                                valor = capacidad,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        TextButton(
                            onClick = {
                                editandoDatos = !editandoDatos
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = if (editandoDatos) {
                                    "Ocultar edición"
                                } else {
                                    "Modificar datos"
                                },
                                color = Negro,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (editandoDatos) {
                            Column(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = {
                                        nombre = it
                                        error = ""
                                    },
                                    label = {
                                        Text("Nombre")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = capacidad,
                                    onValueChange = {
                                        capacidad = it.filter(Char::isDigit)
                                        error = ""
                                    },
                                    label = {
                                        Text("Capacidad")
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Borde)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Negro)
                                .padding(15.dp)
                        ) {
                            Text(
                                text = "Horario de la sala",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Usar horario general",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Texto
                                )

                                Text(
                                    text = "${horario.apertura} a ${horario.cierre}, lunes a viernes",
                                    fontSize = 14.sp,
                                    color = Gris
                                )
                            }

                            Switch(
                                checked = horarioGeneral,
                                onCheckedChange = {
                                    horarioGeneral = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Amarillo,
                                    checkedTrackColor = Negro
                                )
                            )
                        }

                        if (!horarioGeneral) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = apertura,
                                    onValueChange = {
                                        apertura = it
                                            .filter(Char::isDigit)
                                            .take(2)
                                    },
                                    label = {
                                        Text("Apertura")
                                    },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                OutlinedTextField(
                                    value = cierre,
                                    onValueChange = {
                                        cierre = it
                                            .filter(Char::isDigit)
                                            .take(2)
                                    },
                                    label = {
                                        Text("Cierre")
                                    },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Equipamiento",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto
                    )

                    Text(
                        text = "+ Agregar equipo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto,
                        modifier = Modifier.clickable {
                            agregar = !agregar
                        }
                    )
                }

                if (agregar) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Borde)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            CATALOGO_EQUIPOS
                                .filter { nombreEquipo ->
                                    equipos.none {
                                        it.nombre == nombreEquipo
                                    }
                                }
                                .forEach { nombreEquipo ->
                                    Text(
                                        text = "+ $nombreEquipo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                equipos = equipos + Equipo(nombreEquipo)
                                                agregar = false
                                            }
                                            .padding(12.dp),
                                        color = Texto,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                        }
                    }
                }

                equipos.forEachIndexed { index, equipo ->
                    val conFalla = equipo.estado == EstadoEquipo.CON_FALLA

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (conFalla) Rojo else Borde
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = equipo.nombre,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Texto
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            if (!conFalla) Verde else Color.White
                                        )
                                        .clickable {
                                            equipos = equipos.toMutableList().also {
                                                it[index] = equipo.copy(
                                                    estado = EstadoEquipo.OPERATIVO,
                                                    comentario = null
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Operativo",
                                        fontSize = 14.sp,
                                        fontWeight = if (!conFalla) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                        color = if (!conFalla) {
                                            Color.White
                                        } else {
                                            Gris
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            if (conFalla) Rojo else Color.White
                                        )
                                        .clickable {
                                            equipos = equipos.toMutableList().also {
                                                it[index] = equipo.copy(
                                                    estado = EstadoEquipo.CON_FALLA
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Con falla",
                                        fontSize = 14.sp,
                                        fontWeight = if (conFalla) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                        color = if (conFalla) {
                                            Color.White
                                        } else {
                                            Gris
                                        }
                                    )
                                }
                            }

                            if (conFalla) {
                                Text(
                                    text = "Comentario (obligatorio si tiene falla)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Texto
                                )

                                OutlinedTextField(
                                    value = equipo.comentario.orEmpty(),
                                    onValueChange = { valor ->
                                        equipos = equipos.toMutableList().also {
                                            it[index] = equipo.copy(
                                                comentario = valor
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = Rojo,
                        fontSize = 14.sp
                    )
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
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Negro)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Texto,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            val cap = capacidad.toIntOrNull()
                            val a = apertura.toIntOrNull()
                            val c = cierre.toIntOrNull()

                            val equiposInvalidos = equipos.any {
                                it.estado == EstadoEquipo.CON_FALLA &&
                                        it.comentario.isNullOrBlank()
                            }

                            val horarioInvalido = !horarioGeneral &&
                                    (
                                            a == null ||
                                                    c == null ||
                                                    a !in 0..22 ||
                                                    c !in 1..23 ||
                                                    c <= a
                                            )

                            if (
                                nombre.isBlank() ||
                                cap == null ||
                                cap < 1 ||
                                equiposInvalidos ||
                                horarioInvalido
                            ) {
                                error = "Revisa nombre, capacidad, horario y comentarios."
                            } else {
                                guardando = true

                                scope.launch {
                                    try {
                                        repo.guardar(
                                            sala.copy(
                                                nombre = nombre,
                                                capacidad = cap!!,
                                                equipos = equipos,
                                                horarioPropio = if (horarioGeneral) {
                                                    null
                                                } else {
                                                    HorarioSala(
                                                        LocalTime.of(a!!, 0),
                                                        LocalTime.of(c!!, 0)
                                                    )
                                                }
                                            )
                                        )
                                        volver()
                                    } catch (e: Exception) {
                                        error = "No se pudieron guardar los cambios."
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
                            containerColor = Amarillo,
                            contentColor = Texto
                        )
                    ) {
                        Text(
                            text = if (guardando) {
                                "Guardando..."
                            } else {
                                "Guardar cambios"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoSala(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = titulo,
            fontSize = 13.sp,
            color = Gris
        )

        Text(
            text = valor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Texto
        )
    }
}
