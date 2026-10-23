package cl.duoc.reservasalas.app.data.falso

import cl.duoc.reservasalas.app.data.modelo.Equipo
import cl.duoc.reservasalas.app.data.modelo.EstadoEquipo
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.HorarioSala
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Pausa que simulan los repositorios falsos, para que se vean los indicadores de carga. */
internal const val RETARDO_FALSO_MS = 400L

/** Datos de la maqueta (especificación, sección "Datos falsos"). Los mismos para todo el equipo. */
object DatosFalsos {

    /**
     * Contraseña de TODOS los usuarios en la maqueta.
     * Es solo de la maqueta: NO es la PASSWORD_PRUEBA del backend, que nunca se escribe en el repositorio.
     */
    const val CONTRASENA_MAQUETA = "maqueta2026"

    /** Los 7 usuarios de docs/dataset/usuarios.json: mismos IDs y correos que ms-usuarios. */
    val usuarios: List<Usuario> = listOf(
        Usuario(1, "Coordinador Ficticio Uno", "coordinador1@duocuc.test", Rol.COORDINADOR),
        Usuario(2, "Coordinador Ficticio Dos", "coordinador2@duocuc.test", Rol.COORDINADOR),
        Usuario(3, "Relator Ficticio Uno", "relator1@duocuc.test", Rol.RELATOR),
        Usuario(4, "Relator Ficticio Dos", "relator2@duocuc.test", Rol.RELATOR),
        Usuario(5, "Relator Ficticio Tres", "relator3@duocuc.test", Rol.RELATOR),
        Usuario(6, "Relator Ficticio Cuatro", "relator4@duocuc.test", Rol.RELATOR),
        Usuario(7, "Relator Ficticio Cinco", "relator5@duocuc.test", Rol.RELATOR)
    )

    /** Las 6 salas. Se reemplazan cuando el mandante entregue la lista real de espacios. */
    val salas: List<Sala> = listOf(
        Sala(
            codigo = "S-101",
            nombre = "Sala 101",
            capacidad = 20,
            ubicacion = "Edificio A, piso 1",
            equipos = listOf(
                Equipo("Proyector", EstadoEquipo.CON_FALLA, "La imagen parpadea"),
                Equipo("Climatización")
            )
        ),
        Sala("S-102", "Sala 102", 30, "Edificio A, piso 1", listOf(Equipo("Proyector"))),
        Sala("S-201", "Sala 201", 15, "Edificio A, piso 2", listOf(Equipo("Climatización"))),
        Sala(
            codigo = "LAB-01",
            nombre = "Laboratorio de Computación 1",
            capacidad = 25,
            ubicacion = "Edificio B, piso 1",
            equipos = listOf(Equipo("Proyector"), Equipo("Computadores"), Equipo("Climatización"))
        ),
        Sala(
            codigo = "LAB-02",
            nombre = "Laboratorio de Computación 2",
            capacidad = 18,
            ubicacion = "Edificio B, piso 1",
            equipos = listOf(Equipo("Computadores")),
            horarioPropio = HorarioSala(LocalTime.of(9, 0), LocalTime.of(18, 0))
        ),
        Sala(
            codigo = "MUL-01",
            nombre = "Espacio Multipropósito",
            capacidad = 50,
            ubicacion = "Edificio C, piso 1",
            equipos = listOf(Equipo("Proyector"), Equipo("Climatización"))
        )
    )

    /**
     * Las 8 reservas de ejemplo. Las fechas se calculan desde [hoy], así la demo siempre
     * tiene reservas "de hoy". Con el RelojMaqueta (09:55) cada estado se ve en alguna pantalla.
     */
    fun reservas(hoy: LocalDate): List<Reserva> = listOf(
        // Empieza en 10 minutos: recordatorio y check-in habilitado (relator1).
        reserva(1, "LAB-01", 3, hoy, hora(10, 5), hora(12, 0), 2, "Clase de Programación Móvil", EstadoReserva.RESERVADA),
        // En uso: aviso de check-in para el coordinador.
        reserva(2, "S-201", 4, hoy, hora(9, 5), hora(11, 0), 2, "Taller de Bases de Datos", EstadoReserva.EN_USO, hora(9, 32)),
        // Empezó hace 50 minutos sin check-in: métrica "Sin check-in" y "Marcar no presentado".
        reserva(3, "S-102", 5, hoy, hora(9, 5), hora(10, 0), 1, "Reunión de coordinación docente", EstadoReserva.RESERVADA),
        // Ya verificada por el coordinador.
        reserva(4, "MUL-01", 6, hoy, hora(8, 0), hora(10, 0), 2, "Charla de bienvenida", EstadoReserva.VERIFICADA, hora(8, 5)),
        // Mañana: aparece en "Próximas" y se puede cancelar (relator1).
        reserva(5, "S-101", 3, hoy.plusDays(1), hora(10, 5), hora(12, 0), 2, "Clase de Programación Móvil", EstadoReserva.RESERVADA),
        // Reserva del coordinador: muestra que el coordinador también reserva.
        reserva(6, "LAB-02", 1, hoy.plusDays(3), hora(15, 5), hora(16, 0), 1, "Capacitación de coordinadores", EstadoReserva.RESERVADA),
        // Historial de relator1.
        reserva(7, "S-201", 3, hoy.minusDays(1), hora(9, 5), hora(10, 0), 1, "Tutoría", EstadoReserva.NO_PRESENTADO),
        reserva(8, "LAB-01", 3, hoy.minusDays(2), hora(11, 5), hora(12, 0), 1, "Laboratorio de práctica", EstadoReserva.CANCELADA)
    )

    private fun hora(horas: Int, minutos: Int): LocalTime = LocalTime.of(horas, minutos)

    private fun reserva(
        id: Int,
        codigoSala: String,
        idUsuario: Int,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime,
        cantidadBloques: Int,
        motivo: String,
        estado: EstadoReserva,
        horaCheckIn: LocalTime? = null
    ): Reserva {
        val usuario = usuarios.first { it.id == idUsuario }
        return Reserva(
            id = id,
            codigoSala = codigoSala,
            idUsuario = usuario.id,
            nombreUsuario = usuario.nombre,
            emailUsuario = usuario.email,
            fecha = fecha,
            horaInicio = horaInicio,
            horaFin = horaFin,
            cantidadBloques = cantidadBloques,
            motivo = motivo,
            estado = estado,
            horaCheckIn = horaCheckIn,
            creada = LocalDateTime.of(fecha.minusDays(3), LocalTime.of(18, 40))
        )
    }
}
