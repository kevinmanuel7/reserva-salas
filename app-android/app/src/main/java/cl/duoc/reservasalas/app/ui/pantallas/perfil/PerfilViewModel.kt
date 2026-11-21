package cl.duoc.reservasalas.app.ui.pantallas.perfil

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.reservasalas.app.ReservaSalasApp
import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.local.AlmacenFotoPerfil
import cl.duoc.reservasalas.app.data.local.PerfilDataStore
import cl.duoc.reservasalas.app.data.local.SesionDataStore
import cl.duoc.reservasalas.app.data.modelo.AREAS_DUOC
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.repository.RepositorioPerfil
import cl.duoc.reservasalas.app.data.repository.RepositorioReservas
import cl.duoc.reservasalas.app.model.PerfilUsuario
import cl.duoc.reservasalas.app.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Lo que el Perfil necesita para dibujar la tarjeta del usuario y su actividad. */
data class PerfilUiState(
    val usuario: Usuario? = null,
    val reservasEsteMes: Int = 0,
    val checkInRealizados: Int = 0,
    /** Foto de perfil ya lista para dibujar. null = se muestran las iniciales. */
    val foto: ImageBitmap? = null
)

/**
 * Estado de la sección "Mis datos" y del diálogo "Cambiar contraseña".
 * [telefono] guarda solo los 8 dígitos que escribe el usuario (sin "+56 9").
 */
data class FormularioPerfil(
    val cargando: Boolean = true,
    val fechaNacimiento: LocalDate? = null,
    val telefono: String = "",
    val area: String? = null,
    val guardando: Boolean = false,
    val procesandoFoto: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val dialogoPassword: Boolean = false,
    val passwordActual: String = "",
    val passwordNueva: String = "",
    val passwordConfirmacion: String = "",
    val guardandoPassword: Boolean = false,
    val errorPassword: String? = null
)

class PerfilViewModel(
    private val sesionDataStore: SesionDataStore,
    repositorioReservas: RepositorioReservas,
    private val reloj: Reloj,
    private val repositorioPerfil: RepositorioPerfil,
    private val perfilDataStore: PerfilDataStore,
    private val almacenFoto: AlmacenFotoPerfil
) : ViewModel() {

    // "_formulario" es privado y se puede modificar; "formulario" es la versión de solo
    // lectura que observa la pantalla. Así solo el ViewModel cambia el estado.
    private val _formulario = MutableStateFlow(FormularioPerfil())
    val formulario: StateFlow<FormularioPerfil> = _formulario.asStateFlow()

    private val _foto = MutableStateFlow<ImageBitmap?>(null)

    // Último perfil que respondió el servidor. Sirve para enviar solo lo que cambió.
    private var perfilGuardado: PerfilUsuario? = null

    /**
     * combine junta varios Flow: cada vez que cambia la sesión, las reservas o la foto,
     * se vuelve a calcular el estado de la pantalla.
     */
    val uiState: StateFlow<PerfilUiState> =
        combine(sesionDataStore.sesion, repositorioReservas.reservas, _foto) { sesion, reservas, foto ->
            val usuario = sesion?.usuario
            val hoy = reloj.hoy()
            val mias = reservas.filter { it.idUsuario == usuario?.id }
            PerfilUiState(
                usuario = usuario,
                reservasEsteMes = mias.count {
                    it.fecha.year == hoy.year &&
                        it.fecha.month == hoy.month &&
                        it.estado != EstadoReserva.CANCELADA
                },
                checkInRealizados = mias.count { it.horaCheckIn != null },
                foto = foto
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilUiState())

    init {
        cargarPerfil()
        observarFoto()
    }

    // ---------- Mis datos ----------

    /** GET /usuarios/me al abrir la pantalla: llena el formulario con lo guardado. */
    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                val perfil = repositorioPerfil.obtenerPerfil()
                perfilGuardado = perfil
                _formulario.update { it.copy(cargando = false).conPerfil(perfil) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update { it.copy(cargando = false, mensajeError = e.message ?: MENSAJE_ERROR_CARGA) }
            }
        }
    }

    fun onFechaNacimientoCambio(fecha: LocalDate) {
        _formulario.update { it.copy(fechaNacimiento = fecha).sinMensajes() }
    }

    /** Deja solo los dígitos y como máximo 8 (lo que va después de "+56 9"). */
    fun onTelefonoCambio(texto: String) {
        val digitos = texto.filter { it.isDigit() }.take(DIGITOS_TELEFONO)
        _formulario.update { it.copy(telefono = digitos).sinMensajes() }
    }

    fun onAreaCambio(area: String) {
        _formulario.update { it.copy(area = area).sinMensajes() }
    }

    /**
     * Valida y, si todo está bien, envía PUT /usuarios/me.
     * Solo se envían los campos que cambiaron; los demás van null y el servidor los conserva.
     */
    fun guardarDatos() {
        val actual = _formulario.value
        if (actual.guardando) return

        val error = validarFecha(actual.fechaNacimiento)
            ?: validarTelefono(actual.telefono)
            ?: validarArea(actual.area)
        if (error != null) {
            _formulario.update { it.copy(mensajeError = error, mensajeExito = null) }
            return
        }

        val guardado = perfilGuardado
        val telefonoCompleto = actual.telefono.takeIf { it.isNotEmpty() }?.let { PREFIJO_TELEFONO + it }
        val fecha = actual.fechaNacimiento.takeIf { it != guardado?.fechaNacimiento }
        val telefono = telefonoCompleto.takeIf { it != guardado?.telefono }
        val area = actual.area.takeIf { it != guardado?.area }

        if (fecha == null && telefono == null && area == null) {
            _formulario.update { it.copy(mensajeExito = MENSAJE_SIN_CAMBIOS, mensajeError = null) }
            return
        }

        _formulario.update { it.copy(guardando = true).sinMensajes() }
        viewModelScope.launch {
            try {
                val perfil = repositorioPerfil.actualizarPerfil(fecha, telefono, area)
                perfilGuardado = perfil
                _formulario.update {
                    it.copy(guardando = false, mensajeExito = MENSAJE_DATOS_GUARDADOS).conPerfil(perfil)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update { it.copy(guardando = false, mensajeError = e.message ?: MENSAJE_ERROR_GUARDAR) }
            }
        }
    }

    // Cada validación devuelve el mensaje de error, o null si está bien.
    // "?:" (operador Elvis) en guardarDatos pasa a la siguiente solo si la anterior dio null.

    private fun validarFecha(fecha: LocalDate?): String? {
        if (fecha == null) return null
        val hoy = reloj.hoy()
        return when {
            fecha.isAfter(hoy) -> MENSAJE_FECHA_FUTURA
            // Quien cumple 18 justo hoy nació en hoy.minusYears(18): ese día sí se acepta.
            fecha.isAfter(hoy.minusYears(EDAD_MINIMA)) -> MENSAJE_MENOR_DE_EDAD
            else -> null
        }
    }

    // Vacío = el usuario no escribió teléfono: no se valida ni se envía.
    private fun validarTelefono(digitos: String): String? =
        if (digitos.isNotEmpty() && digitos.length != DIGITOS_TELEFONO) MENSAJE_TELEFONO_INVALIDO else null

    private fun validarArea(area: String?): String? =
        if (area != null && area !in AREAS_DUOC) MENSAJE_AREA_INVALIDA else null

    // ---------- Cambiar contraseña ----------

    fun abrirDialogoPassword() {
        _formulario.update { it.copy(dialogoPassword = true, errorPassword = null).sinMensajes() }
    }

    /** Cierra el diálogo y borra lo escrito: las contraseñas no quedan en memoria. */
    fun cerrarDialogoPassword() {
        _formulario.update { it.sinPasswords().copy(dialogoPassword = false) }
    }

    fun onPasswordActualCambio(texto: String) {
        _formulario.update { it.copy(passwordActual = texto, errorPassword = null) }
    }

    fun onPasswordNuevaCambio(texto: String) {
        _formulario.update { it.copy(passwordNueva = texto, errorPassword = null) }
    }

    fun onPasswordConfirmacionCambio(texto: String) {
        _formulario.update { it.copy(passwordConfirmacion = texto, errorPassword = null) }
    }

    fun cambiarPassword() {
        val actual = _formulario.value
        if (actual.guardandoPassword) return

        val error = when {
            actual.passwordActual.isEmpty() -> MENSAJE_FALTA_PASSWORD_ACTUAL
            actual.passwordNueva.length < LARGO_MINIMO_PASSWORD -> MENSAJE_PASSWORD_CORTA
            actual.passwordNueva == actual.passwordActual -> MENSAJE_PASSWORD_IGUAL
            actual.passwordConfirmacion != actual.passwordNueva -> MENSAJE_CONFIRMACION_DISTINTA
            else -> null
        }
        if (error != null) {
            _formulario.update { it.copy(errorPassword = error) }
            return
        }

        _formulario.update { it.copy(guardandoPassword = true, errorPassword = null) }
        viewModelScope.launch {
            try {
                repositorioPerfil.cambiarPassword(actual.passwordActual, actual.passwordNueva)
                _formulario.update {
                    it.sinPasswords().copy(dialogoPassword = false, mensajeExito = MENSAJE_PASSWORD_CAMBIADA)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update {
                    it.copy(guardandoPassword = false, errorPassword = e.message ?: MENSAJE_ERROR_PASSWORD)
                }
            }
        }
    }

    // ---------- Foto de perfil ----------

    /**
     * Sigue la foto del usuario conectado. flatMapLatest: si cambia el usuario, deja de
     * escuchar la foto del anterior y empieza a escuchar la del nuevo.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarFoto() {
        viewModelScope.launch {
            sesionDataStore.sesion
                .map { it?.usuario?.id }
                .distinctUntilChanged()
                .flatMapLatest { id -> if (id == null) flowOf(null) else perfilDataStore.foto(id) }
                // Si no se puede leer el DataStore, se muestran las iniciales en vez de cerrar la app.
                .catch { emit(null) }
                .collect { ruta -> _foto.value = ruta?.let { almacenFoto.cargar(it) } }
        }
    }

    /** Archivo donde la cámara dejará la foto. Lo pide la pantalla antes de abrir la cámara. */
    fun crearArchivoCamara(): Uri = almacenFoto.crearArchivoCamara()

    /** Sirve para la foto de la cámara y para la elegida en la galería. */
    fun usarFoto(origen: Uri) {
        viewModelScope.launch {
            val id = sesionDataStore.leerSesion()?.usuario?.id ?: return@launch
            _formulario.update { it.copy(procesandoFoto = true).sinMensajes() }
            try {
                val ruta = almacenFoto.guardar(origen, id)
                perfilDataStore.guardarFoto(id, ruta)
                // El archivo se llama igual que antes, así que el DataStore no avisa el cambio:
                // la foto nueva se lee aquí directamente.
                _foto.value = almacenFoto.cargar(ruta)
                _formulario.update { it.copy(procesandoFoto = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update { it.copy(procesandoFoto = false, mensajeError = MENSAJE_ERROR_FOTO) }
            }
        }
    }

    fun quitarFoto() {
        viewModelScope.launch {
            val id = sesionDataStore.leerSesion()?.usuario?.id ?: return@launch
            perfilDataStore.foto(id).first()?.let { almacenFoto.borrar(it) }
            perfilDataStore.borrarFoto(id)
            _foto.value = null
        }
    }

    /** Muestra un error (por ejemplo, permiso de cámara negado). */
    fun mostrarError(mensaje: String) {
        _formulario.update { it.copy(mensajeError = mensaje, mensajeExito = null) }
    }

    // ---------- Sesión ----------

    /** Borra la sesión del teléfono y, cuando termina, avisa con [alTerminar]. La foto se conserva. */
    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            sesionDataStore.borrarSesion()
            alTerminar()
        }
    }

    // Funciones de extensión privadas: se escriben como si fueran parte de FormularioPerfil.
    private fun FormularioPerfil.sinMensajes() = copy(mensajeExito = null, mensajeError = null)

    private fun FormularioPerfil.sinPasswords() = copy(
        passwordActual = "",
        passwordNueva = "",
        passwordConfirmacion = "",
        guardandoPassword = false,
        errorPassword = null
    )

    // Pasa el perfil del servidor al formulario. El teléfono se guarda sin "+569".
    private fun FormularioPerfil.conPerfil(perfil: PerfilUsuario) = copy(
        fechaNacimiento = perfil.fechaNacimiento,
        telefono = perfil.telefono?.removePrefix(PREFIJO_TELEFONO)?.filter { it.isDigit() }.orEmpty(),
        area = perfil.area
    )

    companion object {
        const val PREFIJO_TELEFONO = "+569"
        const val DIGITOS_TELEFONO = 8
        const val EDAD_MINIMA = 18L
        const val LARGO_MINIMO_PASSWORD = 8

        const val MENSAJE_FECHA_FUTURA = "La fecha de nacimiento no puede ser futura"
        const val MENSAJE_MENOR_DE_EDAD = "Debes tener al menos 18 años"
        const val MENSAJE_TELEFONO_INVALIDO = "Ingresa los 8 dígitos del celular"
        const val MENSAJE_AREA_INVALIDA = "Elige un área de la lista"
        const val MENSAJE_SIN_CAMBIOS = "No hay cambios para guardar"
        const val MENSAJE_DATOS_GUARDADOS = "Tus datos se guardaron"
        const val MENSAJE_ERROR_CARGA = "No se pudo cargar tu perfil"
        const val MENSAJE_ERROR_GUARDAR = "No se pudieron guardar tus datos"
        const val MENSAJE_ERROR_FOTO = "No se pudo guardar la foto. Intenta con otra imagen."

        const val MENSAJE_FALTA_PASSWORD_ACTUAL = "Ingresa tu contraseña actual"
        const val MENSAJE_PASSWORD_CORTA = "La contraseña nueva debe tener al menos 8 caracteres"
        const val MENSAJE_PASSWORD_IGUAL = "La contraseña nueva debe ser distinta de la actual"
        const val MENSAJE_CONFIRMACION_DISTINTA = "La confirmación no coincide con la contraseña nueva"
        const val MENSAJE_PASSWORD_CAMBIADA = "Contraseña actualizada"
        const val MENSAJE_ERROR_PASSWORD = "No se pudo cambiar la contraseña"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val contenedor = (this[APPLICATION_KEY] as ReservaSalasApp).container
                PerfilViewModel(
                    contenedor.sesionDataStore,
                    contenedor.repositorioReservas,
                    contenedor.reloj,
                    contenedor.repositorioPerfil,
                    contenedor.perfilDataStore,
                    contenedor.almacenFotoPerfil
                )
            }
        }
    }
}
