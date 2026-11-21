package cl.duoc.reservasalas.app.ui.pantallas.perfil

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.R
import cl.duoc.reservasalas.app.data.modelo.AREAS_DUOC
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import cl.duoc.reservasalas.app.ui.componentes.BarraSuperior
import cl.duoc.reservasalas.app.ui.componentes.BotonPeligro
import cl.duoc.reservasalas.app.ui.componentes.BotonPrincipal
import cl.duoc.reservasalas.app.ui.componentes.BotonSecundario
import cl.duoc.reservasalas.app.ui.componentes.ChipRol
import cl.duoc.reservasalas.app.ui.componentes.FormaTarjeta
import cl.duoc.reservasalas.app.ui.componentes.TarjetaMetrica
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import cl.duoc.reservasalas.app.ui.theme.AmarilloDuoc
import cl.duoc.reservasalas.app.ui.theme.Borde
import cl.duoc.reservasalas.app.ui.theme.Fondo
import cl.duoc.reservasalas.app.ui.theme.Gris85
import cl.duoc.reservasalas.app.ui.theme.NegroDuoc
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme
import cl.duoc.reservasalas.app.ui.theme.RojoError
import cl.duoc.reservasalas.app.ui.theme.Superficie
import cl.duoc.reservasalas.app.ui.theme.VerdeExito
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FormatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Pantalla Perfil. Conecta el ViewModel con la parte que dibuja (PerfilContenido) y maneja lo
 * que necesita Android: el permiso de cámara, la cámara y el selector de la galería.
 */
@Composable
fun PerfilPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(
        factory = PerfilViewModel.Factory
    )
) {
    // collectAsStateWithLifecycle: convierte el StateFlow en un valor que, al cambiar,
    // vuelve a dibujar la pantalla (y deja de escuchar si la app pasa a segundo plano).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // rememberSaveable: el valor sobrevive si el teléfono gira o Android recrea la pantalla.
    var confirmarCierre by rememberSaveable { mutableStateOf(false) }
    var opcionesFoto by rememberSaveable { mutableStateOf(false) }
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Cámara: guarda la foto en el archivo que se le entrega (uriCamara).
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        val uri = uriCamara
        if (guardada && uri != null) viewModel.usarFoto(uri)
        uriCamara = null
    }

    fun abrirCamara() {
        val uri = viewModel.crearArchivoCamara()
        uriCamara = uri
        lanzadorCamara.launch(uri)
    }

    // Permiso de cámara en tiempo de ejecución, igual que en el check-in.
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else viewModel.mostrarError("Sin permiso de cámara no se puede tomar la foto.")
    }

    fun tomarFoto() {
        if (!contexto.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            viewModel.mostrarError("Este teléfono no tiene cámara. Elige una foto de la galería.")
            return
        }
        val permitido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (permitido) abrirCamara() else lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    // Selector de fotos del sistema: no necesita permisos (en Android 8 abre el selector de archivos).
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.usarFoto(uri)
    }

    PerfilContenido(
        uiState = uiState,
        formulario = formulario,
        alTocarFoto = { opcionesFoto = true },
        alCambiarFecha = viewModel::onFechaNacimientoCambio,
        alCambiarTelefono = viewModel::onTelefonoCambio,
        alCambiarArea = viewModel::onAreaCambio,
        alGuardarDatos = viewModel::guardarDatos,
        alAbrirPassword = viewModel::abrirDialogoPassword,
        alAbrirHorario = { navegar(Rutas.HORARIO_GENERAL) },
        alCerrarSesion = { confirmarCierre = true },
        modifier = modifier
    )

    if (opcionesFoto) {
        DialogoOpcionesFoto(
            tieneFoto = uiState.foto != null,
            alTomarFoto = { opcionesFoto = false; tomarFoto() },
            alElegirGaleria = {
                opcionesFoto = false
                lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            alQuitarFoto = { opcionesFoto = false; viewModel.quitarFoto() },
            alCerrar = { opcionesFoto = false }
        )
    }

    if (formulario.dialogoPassword) {
        DialogoCambiarPassword(
            formulario = formulario,
            alCambiarActual = viewModel::onPasswordActualCambio,
            alCambiarNueva = viewModel::onPasswordNuevaCambio,
            alCambiarConfirmacion = viewModel::onPasswordConfirmacionCambio,
            alConfirmar = viewModel::cambiarPassword,
            alCerrar = viewModel::cerrarDialogoPassword
        )
    }

    if (confirmarCierre) {
        // TODO: reemplazar por DialogoConfirmacion
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            containerColor = Superficie,
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = NegroDuoc) },
            text = { Text("¿Seguro que quieres cerrar sesión?", color = NegroDuoc) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCierre = false
                    viewModel.cerrarSesion(alTerminar = { navegar(Rutas.LOGIN) })
                }) {
                    Text("Cerrar sesión", color = RojoError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierre = false }) {
                    Text("Cancelar", color = NegroDuoc)
                }
            }
        )
    }
}

/**
 * Dibuja el perfil. No conoce al ViewModel: recibe el estado y avisa cada acción con una
 * función ("al..."). Por eso se puede ver en @Preview con datos escritos a mano.
 */
@Composable
fun PerfilContenido(
    uiState: PerfilUiState,
    formulario: FormularioPerfil,
    alTocarFoto: () -> Unit,
    alCambiarFecha: (LocalDate) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alCambiarArea: (String) -> Unit,
    alGuardarDatos: () -> Unit,
    alAbrirPassword: () -> Unit,
    alAbrirHorario: () -> Unit,
    alCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usuario = uiState.usuario

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(titulo = "Mi perfil")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (usuario != null) {
                TarjetaUsuario(
                    usuario = usuario,
                    foto = uiState.foto,
                    procesandoFoto = formulario.procesandoFoto,
                    alTocarFoto = alTocarFoto
                )
            } else {
                Text(
                    text = "Cargando información del usuario...",
                    color = Gris85,
                    modifier = Modifier
                        .fillMaxWidth()
                        .estiloTarjeta()
                        .padding(20.dp)
                )
            }

            MensajesPerfil(formulario)

            Titulo("Mis datos")
            SeccionMisDatos(
                formulario = formulario,
                alCambiarFecha = alCambiarFecha,
                alCambiarTelefono = alCambiarTelefono,
                alCambiarArea = alCambiarArea,
                alGuardar = alGuardarDatos
            )

            Titulo("Mi actividad")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    numero = uiState.reservasEsteMes.toString(),
                    descripcion = "Reservas este mes",
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    numero = uiState.checkInRealizados.toString(),
                    descripcion = "Check-in realizados",
                    modifier = Modifier.weight(1f)
                )
            }

            if (usuario?.rol == Rol.COORDINADOR) {
                Titulo("Administración")
                FilaLista(
                    titulo = "Horario general de bloques",
                    descripcion = "Configurar horarios de las salas",
                    alPresionar = alAbrirHorario
                )
            }

            Titulo("Cuenta")
            FilaLista(
                titulo = "Cambiar contraseña",
                descripcion = "Necesitas tu contraseña actual",
                alPresionar = alAbrirPassword
            )
            BotonPeligro(
                texto = "Cerrar sesión",
                alPresionar = alCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Sede Viña del Mar · Datos ficticios con fines académicos",
                fontSize = 12.sp,
                color = Gris85,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Fondo blanco, borde gris y esquinas de tarjeta (igual que las tarjetas compartidas). */
private fun Modifier.estiloTarjeta(): Modifier =
    this
        .background(Superficie, FormaTarjeta)
        .border(1.dp, Borde, FormaTarjeta)

@Composable
private fun Titulo(texto: String) {
    Text(text = texto, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)
}

/** Tarjeta con la foto (o las iniciales), el nombre, el correo y el rol. Nada de esto se edita. */
@Composable
private fun TarjetaUsuario(
    usuario: Usuario,
    foto: ImageBitmap?,
    procesandoFoto: Boolean,
    alTocarFoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .estiloTarjeta()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FotoPerfil(
                nombre = usuario.nombre,
                foto = foto,
                procesando = procesandoFoto,
                alPresionar = alTocarFoto
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(usuario.nombre, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)
                Text(usuario.email, fontSize = 13.sp, color = Gris85)
            }
        }
        HorizontalDivider(color = Borde)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tipo de usuario", fontSize = 14.sp, color = Gris85)
            ChipRol(usuario.rol)
        }
    }
}

/**
 * Círculo de 68 dp que se puede tocar: muestra la foto si existe y, si no, las iniciales
 * sobre amarillo. Mientras se guarda una foto nueva muestra un indicador de carga.
 */
@Composable
private fun FotoPerfil(
    nombre: String,
    foto: ImageBitmap?,
    procesando: Boolean,
    alPresionar: () -> Unit
) {
    val iniciales = nombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(AmarilloDuoc)
            .clickable(enabled = !procesando, onClick = alPresionar),
        contentAlignment = Alignment.Center
    ) {
        when {
            procesando -> CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = NegroDuoc,
                strokeWidth = 2.dp
            )
            foto != null -> Image(
                bitmap = foto,
                contentDescription = "Foto de perfil. Toca para cambiarla",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            else -> Text(iniciales, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = NegroDuoc)
        }
    }
}

/** Mensaje de éxito (verde) o de error (rojo, con ícono) de la última acción. */
@Composable
private fun MensajesPerfil(formulario: FormularioPerfil) {
    formulario.mensajeError?.let { mensaje ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Superficie, FormaTarjeta)
                .border(1.dp, RojoError, FormaTarjeta)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = "Error",
                tint = RojoError,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(mensaje, color = RojoError, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    formulario.mensajeExito?.let { mensaje ->
        Text(
            text = "✓ $mensaje",
            color = VerdeExito,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .background(Superficie, FormaTarjeta)
                .border(1.dp, VerdeExito, FormaTarjeta)
                .padding(14.dp)
        )
    }
}

/**
 * Fecha de nacimiento, teléfono y área, con el botón "Guardar cambios".
 * Las reglas (mayor de 18, 8 dígitos, área de la lista) las revisa el ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionMisDatos(
    formulario: FormularioPerfil,
    alCambiarFecha: (LocalDate) -> Unit,
    alCambiarTelefono: (String) -> Unit,
    alCambiarArea: (String) -> Unit,
    alGuardar: () -> Unit
) {
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }
    var menuAreaAbierto by rememberSaveable { mutableStateOf(false) }
    val habilitado = !formulario.cargando && !formulario.guardando

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .estiloTarjeta()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (formulario.cargando) {
            Text("Cargando tus datos...", fontSize = 13.sp, color = Gris85)
        }

        // Fila de la fecha: al tocarla abre el calendario.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = habilitado) { mostrarCalendario = true }
                .border(1.dp, Borde, FormaTarjeta)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("Fecha de nacimiento", fontSize = 12.sp, color = Gris85)
            Text(
                text = formulario.fechaNacimiento?.format(FormatoFecha) ?: "Toca para elegir",
                fontSize = 16.sp,
                color = if (formulario.fechaNacimiento != null) NegroDuoc else Gris85
            )
        }

        // TODO: reemplazar por CampoTexto
        OutlinedTextField(
            value = formulario.telefono,
            onValueChange = alCambiarTelefono,
            label = { Text("Teléfono celular") },
            // prefix: texto fijo antes de lo que escribe el usuario; no se puede borrar.
            prefix = { Text("+56 9 ") },
            placeholder = { Text("12345678") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth()
        )

        // Menú desplegable con las áreas. readOnly: el usuario solo elige, no escribe.
        // TODO: reemplazar por CampoSeleccion
        ExposedDropdownMenuBox(
            expanded = menuAreaAbierto,
            onExpandedChange = { if (habilitado) menuAreaAbierto = it }
        ) {
            OutlinedTextField(
                value = formulario.area ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Área") },
                placeholder = { Text("Elige tu área") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAreaAbierto) },
                enabled = habilitado,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, habilitado)
            )
            ExposedDropdownMenu(
                expanded = menuAreaAbierto,
                onDismissRequest = { menuAreaAbierto = false },
                containerColor = Superficie
            ) {
                AREAS_DUOC.forEach { area ->
                    DropdownMenuItem(
                        text = { Text(area, color = NegroDuoc) },
                        onClick = {
                            alCambiarArea(area)
                            menuAreaAbierto = false
                        }
                    )
                }
            }
        }

        BotonPrincipal(
            texto = "Guardar cambios",
            alPresionar = alGuardar,
            habilitado = !formulario.cargando,
            cargando = formulario.guardando,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (mostrarCalendario) {
        // El DatePicker trabaja en milisegundos UTC: por eso la conversión usa ZoneOffset.UTC
        // (con la zona del teléfono la fecha podría correrse un día).
        val estadoCalendario = rememberDatePickerState(
            initialSelectedDateMillis = formulario.fechaNacimiento
                ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoCalendario.selectedDateMillis?.let { millis ->
                        alCambiarFecha(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    mostrarCalendario = false
                }) {
                    Text("Aceptar", color = NegroDuoc, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar", color = NegroDuoc)
                }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
}

/** Fila tocable con título, descripción y flecha (ej. "Cambiar contraseña"). */
@Composable
private fun FilaLista(
    titulo: String,
    descripcion: String,
    alPresionar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .estiloTarjeta()
            .clickable(onClick = alPresionar)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NegroDuoc)
            Text(descripcion, fontSize = 13.sp, color = Gris85)
        }
        Text("›", fontSize = 28.sp, color = Gris85)
    }
}

/** Diálogo con las tres opciones de la foto. "Quitar foto" aparece solo si hay foto. */
@Composable
private fun DialogoOpcionesFoto(
    tieneFoto: Boolean,
    alTomarFoto: () -> Unit,
    alElegirGaleria: () -> Unit,
    alQuitarFoto: () -> Unit,
    alCerrar: () -> Unit
) {
    // TODO: reemplazar por DialogoOpciones
    AlertDialog(
        onDismissRequest = alCerrar,
        containerColor = Superficie,
        title = { Text("Foto de perfil", fontWeight = FontWeight.Bold, color = NegroDuoc) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "La foto se guarda solo en este teléfono.",
                    fontSize = 13.sp,
                    color = Gris85
                )
                BotonSecundario("Tomar foto", alTomarFoto, Modifier.fillMaxWidth())
                BotonSecundario("Elegir de la galería", alElegirGaleria, Modifier.fillMaxWidth())
                if (tieneFoto) {
                    BotonPeligro("Quitar foto", alQuitarFoto, Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = alCerrar) { Text("Cerrar", color = NegroDuoc) }
        }
    )
}

/**
 * Diálogo "Cambiar contraseña". PasswordVisualTransformation muestra puntos en vez de
 * las letras. Las reglas las revisa el ViewModel y el error se muestra dentro del diálogo.
 */
@Composable
private fun DialogoCambiarPassword(
    formulario: FormularioPerfil,
    alCambiarActual: (String) -> Unit,
    alCambiarNueva: (String) -> Unit,
    alCambiarConfirmacion: (String) -> Unit,
    alConfirmar: () -> Unit,
    alCerrar: () -> Unit
) {
    val guardando = formulario.guardandoPassword

    // TODO: reemplazar por DialogoFormulario
    AlertDialog(
        onDismissRequest = { if (!guardando) alCerrar() },
        containerColor = Superficie,
        title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold, color = NegroDuoc) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoPassword("Contraseña actual", formulario.passwordActual, alCambiarActual, !guardando)
                CampoPassword("Contraseña nueva", formulario.passwordNueva, alCambiarNueva, !guardando)
                CampoPassword(
                    "Repite la contraseña nueva",
                    formulario.passwordConfirmacion,
                    alCambiarConfirmacion,
                    !guardando
                )
                Text("Mínimo 8 caracteres.", fontSize = 12.sp, color = Gris85)
                formulario.errorPassword?.let { error ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_error),
                            contentDescription = "Error",
                            tint = RojoError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = RojoError, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            BotonPrincipal(texto = "Cambiar", alPresionar = alConfirmar, cargando = guardando)
        },
        dismissButton = {
            TextButton(onClick = alCerrar, enabled = !guardando) { Text("Cancelar", color = NegroDuoc) }
        }
    )
}

@Composable
private fun CampoPassword(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    habilitado: Boolean
) {
    // TODO: reemplazar por CampoTexto
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun PerfilCoordinadorPreview() {
    ReservaSalasTheme {
        PerfilContenido(
            uiState = PerfilUiState(
                usuario = Usuario(1, "Coordinador Ficticio Uno", "coordinador1@duocuc.test", Rol.COORDINADOR),
                reservasEsteMes = 3,
                checkInRealizados = 2
            ),
            formulario = FormularioPerfil(
                cargando = false,
                fechaNacimiento = LocalDate.of(1990, 5, 20),
                telefono = "12345678",
                area = "Informática y Telecomunicaciones",
                mensajeExito = PerfilViewModel.MENSAJE_DATOS_GUARDADOS
            ),
            alTocarFoto = {},
            alCambiarFecha = {},
            alCambiarTelefono = {},
            alCambiarArea = {},
            alGuardarDatos = {},
            alAbrirPassword = {},
            alAbrirHorario = {},
            alCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogoPasswordPreview() {
    ReservaSalasTheme {
        DialogoCambiarPassword(
            formulario = FormularioPerfil(
                dialogoPassword = true,
                passwordActual = "clave",
                errorPassword = PerfilViewModel.MENSAJE_PASSWORD_CORTA
            ),
            alCambiarActual = {},
            alCambiarNueva = {},
            alCambiarConfirmacion = {},
            alConfirmar = {},
            alCerrar = {}
        )
    }
}
