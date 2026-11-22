package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.HOY_PRUEBA
import cl.duoc.reservasalas.app.RELATORA_PRUEBA
import cl.duoc.reservasalas.app.data.modelo.Bloque
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.remote.ReservasApi
import cl.duoc.reservasalas.app.data.remote.dto.CrearReservaPeticionDto
import cl.duoc.reservasalas.app.data.remote.dto.ReservaDto
import cl.duoc.reservasalas.app.errorHttp
import cl.duoc.reservasalas.app.jsonError
import cl.duoc.reservasalas.app.relojFijo
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

/**
 * Pruebas de RepositorioReservasRemoto sin servidor.
 * - ReservasApi (Retrofit) se simula con MockK: cada prueba dice qué responde cada endpoint, o qué
 *   error lanza (HttpException con el JSON de error del contrato, o IOException = sin conexión).
 * - RepositorioSalas también es un mock: solo hace falta que conozca la sala S-101 (id 5).
 * - El reloj está fijo el lunes 23/11/2026 a las 09:00, para que las validaciones de fecha no
 *   dependan del día en que se ejecuten las pruebas.
 */
class RepositorioReservasRemotoTest {

    private val api = mockk<ReservasApi>()
    private val repositorioSalas = mockk<RepositorioSalas>()
    private val sala = Sala("S-101", "Sala 101", 30, "Piso 1", emptyList(), id = 5)
    private lateinit var repositorio: RepositorioReservasRemoto

    // Bloques del horario predeterminado: [2] = 10:05–11:00, [3] = 11:05–12:00...
    private val bloques = HorarioGeneral().bloques()
    private val manana: LocalDate = HOY_PRUEBA.plusDays(1)

    @Before
    fun preparar() {
        every { repositorioSalas.salas } returns MutableStateFlow(listOf(sala))
        every { repositorioSalas.buscar(any()) } returns null
        every { repositorioSalas.buscar("S-101") } returns sala
        repositorio = RepositorioReservasRemoto(
            api,
            repositorioSalas,
            relojFijo(LocalTime.of(9, 0)),
            Json { ignoreUnknownKeys = true }
        )
    }

    /** Reserva tal como la envía ms-reservas. Por defecto: sala 5, mañana, bloques 3 y 4 (10:05–12:00). */
    private fun dto(
        id: Long = 1,
        estado: String = "ACTIVA",
        horaCheckIn: String? = null,
        verificada: Boolean? = null,
        noPresentado: Boolean? = null
    ) = ReservaDto(
        id = id, salaId = 5, usuarioId = 7, nombreRelator = "Ana María Relatora", actividad = "Excel",
        fecha = manana.toString(), bloqueInicio = 3, bloqueFin = 4, estado = estado,
        horaCheckIn = horaCheckIn, verificada = verificada, noPresentado = noPresentado
    )

    /** Deja una reserva (id 1) cargada en memoria, como si una pantalla ya hubiera refrescado. */
    private suspend fun cargarUnaReserva() {
        coEvery { api.listar(any()) } returns emptyList()
        coEvery { api.listar(manana.toString()) } returns listOf(dto())
        repositorio.refrescar()
    }

    // ---------- refrescar ----------

    @Test
    fun refrescar_pide22DiasYConvierteLasReservasDelServidor() = runTest {
        cargarUnaReserva()

        // De 7 días atrás a 14 días adelante: 22 peticiones, una por día.
        coVerify(exactly = 22) { api.listar(any()) }
        coVerify { api.listar("2026-11-16") }
        coVerify { api.listar("2026-12-07") }

        val reserva = repositorio.reservas.value.single()
        assertEquals("S-101", reserva.codigoSala)
        assertEquals("10:05–12:00", reserva.horario)
        assertEquals("ID de usuario: 7", reserva.emailUsuario)
        assertEquals(EstadoReserva.RESERVADA, reserva.estado)
        assertEquals(reserva, repositorio.buscar(1))
    }

    @Test
    fun refrescar_sinSalasCargadas_lasPidePrimero() = runTest {
        every { repositorioSalas.salas } returns MutableStateFlow(emptyList())
        coEvery { repositorioSalas.refrescar() } just Runs
        coEvery { api.listar(any()) } returns listOf(dto())

        repositorio.refrescar()

        coVerify { repositorioSalas.refrescar() }
        // La sala sigue sin conocerse: la reserva queda sin código, pero no se pierde.
        assertEquals("", repositorio.reservas.value.first().codigoSala)
    }

    @Test
    fun refrescar_sinConexion_conservaLaUltimaLista() = runTest {
        cargarUnaReserva()
        coEvery { api.listar(any()) } throws IOException("sin red")

        repositorio.refrescar()

        assertEquals(1, repositorio.reservas.value.size)
    }

    // ---------- crear: validaciones locales (no llegan al servidor) ----------

    private suspend fun crearCon(
        lista: List<Bloque>,
        fecha: LocalDate = manana,
        codigoSala: String = "S-101"
    ): ResultadoReserva = repositorio.crear(codigoSala, RELATORA_PRUEBA, fecha, lista, "Excel")

    private suspend fun assertErrorSinLlamarAlServidor(mensaje: String, resultado: ResultadoReserva) {
        assertEquals(ResultadoReserva.Error(mensaje), resultado)
        coVerify(exactly = 0) { api.crear(any()) }
    }

    @Test
    fun crear_sinBloques_pideElegirAlMenosUno() = runTest {
        assertErrorSinLlamarAlServidor("Elige al menos un bloque", crearCon(emptyList()))
    }

    @Test
    fun crear_masDe4Bloques_esRechazada() = runTest {
        assertErrorSinLlamarAlServidor(
            "Puedes reservar hasta 4 bloques seguidos",
            crearCon(bloques.subList(2, 7))
        )
    }

    @Test
    fun crear_bloquesQueNoSonSeguidos_esRechazada() = runTest {
        assertErrorSinLlamarAlServidor(
            "Los bloques deben ser seguidos",
            crearCon(listOf(bloques[2], bloques[4]))
        )
    }

    @Test
    fun crear_fechaPasadaOMasDe14Dias_esRechazada() = runTest {
        val mensaje = "Solo puedes reservar desde hoy hasta 14 días más"
        assertErrorSinLlamarAlServidor(mensaje, crearCon(listOf(bloques[2]), HOY_PRUEBA.minusDays(1)))
        assertErrorSinLlamarAlServidor(mensaje, crearCon(listOf(bloques[2]), HOY_PRUEBA.plusDays(15)))
    }

    @Test
    fun crear_hoyUnBloqueQueYaEmpezo_esRechazada() = runTest {
        // Son las 09:00: el bloque 08:00–09:00 ya empezó.
        assertErrorSinLlamarAlServidor("Ese bloque ya empezó", crearCon(listOf(bloques[0]), HOY_PRUEBA))
    }

    @Test
    fun crear_salaQueNoExiste_esRechazada() = runTest {
        assertErrorSinLlamarAlServidor("No se encontró la sala", crearCon(listOf(bloques[2]), codigoSala = "X-999"))
    }

    // ---------- crear: respuesta del servidor ----------

    @Test
    fun crear_exito_enviaLosNumerosDeBloqueYAgregaLaReserva() = runTest {
        val peticion = slot<CrearReservaPeticionDto>()
        coEvery { api.crear(capture(peticion)) } returns dto(id = 50)

        // Bloques desordenados y actividad con espacios: el repositorio ordena y recorta.
        val resultado = repositorio.crear("S-101", RELATORA_PRUEBA, manana, listOf(bloques[3], bloques[2]), "  Excel  ")

        assertEquals(CrearReservaPeticionDto(5, manana.toString(), 3, 4, "Excel"), peticion.captured)
        assertTrue(resultado is ResultadoReserva.Exito)
        assertEquals(50, (resultado as ResultadoReserva.Exito).reserva.id)
        assertEquals(listOf(50), repositorio.reservas.value.map { it.id })
    }

    private suspend fun crearConError(error: Exception): ResultadoReserva {
        coEvery { api.crear(any()) } throws error
        return crearCon(listOf(bloques[2]))
    }

    @Test
    fun crear_409UsuarioOcupado_muestraMensajeClaro() = runTest {
        val resultado = crearConError(errorHttp(409, jsonError("USUARIO_OCUPADO", "El usuario ya tiene una reserva")))

        assertEquals(ResultadoReserva.Error("Ya tienes una reserva en ese horario"), resultado)
    }

    @Test
    fun crear_409CruceHorario_avisaQueElBloqueEstaOcupado() = runTest {
        val resultado = crearConError(errorHttp(409, jsonError("CRUCE_HORARIO", "Cruce")))

        assertEquals(ResultadoReserva.Error("Uno de los bloques ya está ocupado"), resultado)
    }

    @Test
    fun crear_400_muestraElMensajeDelServidor() = runTest {
        val resultado = crearConError(errorHttp(400, jsonError("DATOS_INVALIDOS", "La actividad es obligatoria")))

        assertEquals(ResultadoReserva.Error("La actividad es obligatoria"), resultado)
    }

    @Test
    fun crear_errorSinJsonOConMensajeVacio_muestraMensajeGenerico() = runTest {
        val generico = ResultadoReserva.Error("No se pudo crear la reserva. Intenta nuevamente.")

        assertEquals(generico, crearConError(errorHttp(500, "<html>error</html>")))
        assertEquals(generico, crearConError(errorHttp(400, jsonError("OTRO", ""))))
        assertEquals(generico, crearConError(IllegalArgumentException("JSON inesperado")))
    }

    @Test
    fun crear_sinConexion_muestraSinConexion() = runTest {
        assertEquals(ResultadoReserva.Error("Sin conexión"), crearConError(IOException("sin red")))
    }

    // ---------- cancelar, check-in, verificar y no presentado ----------

    @Test
    fun cancelar_reemplazaLaReservaEnMemoria() = runTest {
        cargarUnaReserva()
        coEvery { api.cancelar(1) } returns dto(estado = "CANCELADA")

        repositorio.cancelar(1)

        assertEquals(1, repositorio.reservas.value.size)
        assertEquals(EstadoReserva.CANCELADA, repositorio.buscar(1)?.estado)
    }

    @Test
    fun checkIn_guardaLaHoraDelServidor() = runTest {
        cargarUnaReserva()
        coEvery { api.registrarCheckIn(1) } returns dto(horaCheckIn = "10:07")

        repositorio.registrarCheckIn(1, LocalTime.of(10, 0))

        assertEquals(EstadoReserva.EN_USO, repositorio.buscar(1)?.estado)
        assertEquals(LocalTime.of(10, 7), repositorio.buscar(1)?.horaCheckIn)
    }

    @Test
    fun verificarYNoPresentado_actualizanElEstado() = runTest {
        cargarUnaReserva()
        coEvery { api.verificar(1) } returns dto(horaCheckIn = "10:07", verificada = true)
        coEvery { api.marcarNoPresentado(1) } returns dto(noPresentado = true)

        repositorio.marcarVerificada(1)
        assertEquals(EstadoReserva.VERIFICADA, repositorio.buscar(1)?.estado)

        repositorio.marcarNoPresentado(1)
        assertEquals(EstadoReserva.NO_PRESENTADO, repositorio.buscar(1)?.estado)
    }

    @Test
    fun cancelar_conErrorDelServidor_lanzaElMensajeYNoCambiaLaLista() = runTest {
        cargarUnaReserva()
        coEvery { api.cancelar(1) } throws errorHttp(400, jsonError("RESERVA_INICIADA", "La reserva ya empezó"))

        val error = runCatching { repositorio.cancelar(1) }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertEquals("La reserva ya empezó", error?.message)
        assertEquals(EstadoReserva.RESERVADA, repositorio.buscar(1)?.estado)
    }

    @Test
    fun checkIn_sinConexion_lanzaSinConexion() = runTest {
        cargarUnaReserva()
        coEvery { api.registrarCheckIn(1) } throws IOException("sin red")

        val error = runCatching { repositorio.registrarCheckIn(1, LocalTime.of(10, 0)) }.exceptionOrNull()

        assertEquals("Sin conexión", error?.message)
    }

    @Test
    fun verificar_errorDesconocido_usaElMensajeDeLaAccion() = runTest {
        coEvery { api.verificar(1) } throws RuntimeException("detalle técnico")
        coEvery { api.marcarNoPresentado(1) } throws errorHttp(500)

        assertEquals(
            "No se pudo verificar la reserva",
            runCatching { repositorio.marcarVerificada(1) }.exceptionOrNull()?.message
        )
        assertEquals(
            "No se pudo marcar como no presentado",
            runCatching { repositorio.marcarNoPresentado(1) }.exceptionOrNull()?.message
        )
    }
}
