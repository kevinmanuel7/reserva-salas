package cl.duoc.reservasalas.app.data.remote.dto

import cl.duoc.reservasalas.app.data.modelo.Equipo
import cl.duoc.reservasalas.app.data.modelo.EstadoEquipo
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.Feriado
import cl.duoc.reservasalas.app.data.modelo.HorarioGeneral
import cl.duoc.reservasalas.app.data.modelo.HorarioSala
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.model.PerfilUsuario
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Conversiones entre el JSON del backend (DTO) y los modelos que usan las pantallas.
 * No se simula nada: se arma el DTO a mano, igual al JSON del contrato, y se revisa el modelo.
 * Si el contrato cambia y alguien olvida actualizar una conversión, estas pruebas lo detectan.
 */
class DtosTest {

    // ---------- Bloques: número ↔ hora ----------

    @Test
    fun bloques_elPrimeroEmpiezaEnPuntoYLosDemasALas05() {
        assertEquals(LocalTime.of(8, 0), horaInicioBloque(1))
        assertEquals(LocalTime.of(9, 5), horaInicioBloque(2))
        assertEquals(LocalTime.of(20, 5), horaInicioBloque(13))
        assertEquals(LocalTime.of(9, 0), horaFinBloque(1))
        assertEquals(LocalTime.of(21, 0), horaFinBloque(13))
    }

    @Test
    fun numeroBloque_esElInversoDeLaHoraDeInicio() {
        assertEquals(1, numeroBloque(LocalTime.of(8, 0)))
        assertEquals(2, numeroBloque(LocalTime.of(9, 5)))
        assertEquals(13, numeroBloque(LocalTime.of(20, 5)))
    }

    // ---------- Reserva ----------

    private val reservaDto = ReservaDto(
        id = 42, salaId = 5, usuarioId = 7, nombreRelator = "Ana", actividad = "Excel",
        fecha = "2026-11-24", bloqueInicio = 3, bloqueFin = 4, estado = "ACTIVA"
    )

    @Test
    fun reservaActiva_seConvierteEnReservadaConSusHoras() {
        val reserva = reservaDto.aModelo(codigoSala = "S-101", emailUsuario = "ID de usuario: 7")

        assertEquals(42, reserva.id)
        assertEquals("S-101", reserva.codigoSala)
        assertEquals(7, reserva.idUsuario)
        assertEquals(LocalDate.of(2026, 11, 24), reserva.fecha)
        assertEquals("10:05–12:00", reserva.horario)
        assertEquals(2, reserva.cantidadBloques)
        assertEquals("Excel", reserva.motivo)
        assertEquals(EstadoReserva.RESERVADA, reserva.estado)
        assertNull(reserva.horaCheckIn)
    }

    @Test
    fun reserva_elEstadoSeDeduceDeLosCamposDelServidor() {
        fun estado(dto: ReservaDto) = dto.aModelo("S-101", "").estado

        assertEquals(EstadoReserva.CANCELADA, estado(reservaDto.copy(estado = "CANCELADA")))
        assertEquals(EstadoReserva.EN_USO, estado(reservaDto.copy(horaCheckIn = "10:07")))
        assertEquals(EstadoReserva.VERIFICADA, estado(reservaDto.copy(horaCheckIn = "10:07", verificada = true)))
        assertEquals(EstadoReserva.NO_PRESENTADO, estado(reservaDto.copy(noPresentado = true)))
        assertEquals(LocalTime.of(10, 7), reservaDto.copy(horaCheckIn = "10:07").aModelo("S-101", "").horaCheckIn)
    }

    // ---------- Sala ----------

    private val salaDto = SalaDto(
        id = 5, codigo = "S-101", nombre = "Sala 101", tipo = "SALA", capacidad = 30,
        tieneProyector = true, tieneComputadores = false, tieneClimatizacion = true, activa = true,
        ubicacion = "Piso 1", horaApertura = "10:00", horaCierre = "18:00",
        equipos = listOf(EquipoDto("Proyector", "CON_FALLA", "Parpadea"), EquipoDto("Pizarra", "OPERATIVO"))
    )

    @Test
    fun sala_conHorarioPropioYEquipos_seConvierteCompleta() {
        assertEquals(
            Sala(
                codigo = "S-101",
                nombre = "Sala 101",
                capacidad = 30,
                ubicacion = "Piso 1",
                equipos = listOf(
                    Equipo("Proyector", EstadoEquipo.CON_FALLA, "Parpadea"),
                    Equipo("Pizarra", EstadoEquipo.OPERATIVO)
                ),
                horarioPropio = HorarioSala(LocalTime.of(10, 0), LocalTime.of(18, 0)),
                id = 5
            ),
            salaDto.aModelo()
        )
    }

    @Test
    fun sala_sinHorarioNiUbicacion_usaElGeneralYUnTextoPorDefecto() {
        val sala = salaDto.copy(ubicacion = null, horaApertura = null, horaCierre = null).aModelo()

        assertNull(sala.horarioPropio)
        assertEquals("Sin ubicación", sala.ubicacion)
    }

    @Test
    fun peticionDeSala_copiaDelOriginalLoQueLaAppNoEdita() {
        val editada = salaDto.aModelo().copy(nombre = "Sala renovada", capacidad = 25)

        val peticion = editada.aPeticion(salaDto)

        assertEquals("Sala renovada", peticion.nombre)
        assertEquals(25, peticion.capacidad)
        assertEquals("SALA", peticion.tipo)
        assertEquals(true, peticion.tieneProyector)
        assertEquals("10:00", peticion.horaApertura)
        assertEquals("18:00", peticion.horaCierre)
        assertEquals(EquipoDto("Proyector", "CON_FALLA", "Parpadea"), peticion.equipos?.first())
        assertEquals(false, peticion.usaHorarioGeneral)
    }

    @Test
    fun peticionDeSala_sinHorarioPropio_pideVolverAlHorarioGeneral() {
        val peticion = salaDto.aModelo().copy(horarioPropio = null).aPeticion(salaDto)

        assertNull(peticion.horaApertura)
        assertEquals(true, peticion.usaHorarioGeneral)
    }

    // ---------- Horario general ----------

    @Test
    fun horario_idaYVuelta_conservaLosDatosYOrdenaLosDias() {
        val horario = HorarioGeneral(
            apertura = LocalTime.of(9, 0),
            cierre = LocalTime.of(20, 0),
            pausaMinutos = 10,
            dias = setOf(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY)
        )

        val dto = horario.aDto()

        assertEquals(HorarioDto("09:00", "20:00", 10, listOf("MONDAY", "WEDNESDAY")), dto)
        assertEquals(horario, dto.aModelo())
    }

    // ---------- Usuario y feriado ----------

    @Test
    fun usuario_conDatosDePerfil_seConvierteEnPerfil() {
        val dto = UsuarioDto(3, "Ana", "ana@duocuc.test", "COORDINADOR", "1990-05-01", "+56912345678", "Docencia")

        assertEquals(
            PerfilUsuario(
                Usuario(3, "Ana", "ana@duocuc.test", Rol.COORDINADOR),
                LocalDate.of(1990, 5, 1),
                "+56912345678",
                "Docencia"
            ),
            dto.aPerfil()
        )
        assertNull(dto.copy(fechaNacimiento = null).aPerfil().fechaNacimiento)
    }

    @Test
    fun feriado_seConvierteConSuFecha() {
        assertEquals(
            Feriado(LocalDate.of(2026, 12, 25), "Navidad"),
            FeriadoDto("2026-12-25", "Navidad", global = true).aModelo()
        )
    }
}
