
package cl.duoc.reservasalas.app.data.falso

import cl.duoc.reservasalas.app.data.modelo.Equipo
import cl.duoc.reservasalas.app.data.modelo.EstadoEquipo
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.HorarioSala
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Usuario
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal const val RETARDO_FALSO_MS = 400L

object DatosFalsos {

    const val CONTRASENA_MAQUETA = "maqueta2026"

    val usuarios: List<Usuario> = listOf(
        Usuario(1, "Coordinador Ficticio Uno", "coordinador1@duocuc.test", Rol.COORDINADOR),
        Usuario(2, "Coordinador Ficticio Dos", "coordinador2@duocuc.test", Rol.COORDINADOR),
        Usuario(3, "Relator Ficticio Uno", "relator1@duocuc.test", Rol.RELATOR),
        Usuario(4, "Relator Ficticio Dos", "relator2@duocuc.test", Rol.RELATOR),
        Usuario(5, "Relator Ficticio Tres", "relator3@duocuc.test", Rol.RELATOR),
        Usuario(6, "Relator Ficticio Cuatro", "relator4@duocuc.test", Rol.RELATOR),
        Usuario(7, "Relator Ficticio Cinco", "relator5@duocuc.test", Rol.RELATOR)
    )

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
        Sala(
            "S-102",
            "Sala 102",
            30,
            "Edificio A, piso 1",
            listOf(Equipo("Proyector"))
        ),
        Sala(
            "S-201",
            "Sala 201",
            15,
            "Edificio A, piso 2",
            listOf(Equipo("Climatización"))
        ),
        Sala(
            codigo = "LAB-01",
            nombre = "Laboratorio de Computación 1",
            capacidad = 25,
            ubicacion = "Edificio B, piso 1",
            equipos = listOf(
                Equipo("Proyector"),
                Equipo("Computadores"),
                Equipo("Climatización")
            )
        ),
        Sala(
            codigo = "LAB-02",
            nombre = "Laboratorio de Computación 2",
            capacidad = 18,
            ubicacion = "Edificio B, piso 1",
            equipos = listOf(Equipo("Computadores")),
            horarioPropio = HorarioSala(
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)
            )
        ),
        Sala(
            codigo = "MUL-01",
            nombre = "Espacio Multipropósito",
            capacidad = 50,
            ubicacion = "Edificio C, piso 1",
            equipos = listOf(
                Equipo("Proyector"),
                Equipo("Climatización")
            )
        )
    )

    fun reservas(hoy: LocalDate): List<Reserva> {
        val fechaActual = ajustarDiaHabil(hoy)
        val siguienteDiaHabil = sumarDiasHabiles(fechaActual, 1)
        val tercerDiaHabil = sumarDiasHabiles(fechaActual, 3)
        val anteriorDiaHabil = restarDiasHabiles(fechaActual, 1)
        val segundoDiaAnteriorHabil = restarDiasHabiles(fechaActual, 2)

        return listOf(
            reserva(
                1,
                "LAB-01",
                3,
                fechaActual,
                hora(10, 5),
                hora(12, 0),
                2,
                "Clase de Programación Móvil",
                EstadoReserva.RESERVADA
            ),
            reserva(
                2,
                "S-201",
                4,
                fechaActual,
                hora(9, 5),
                hora(11, 0),
                2,
                "Taller de Bases de Datos",
                EstadoReserva.EN_USO,
                hora(9, 32)
            ),
            reserva(
                3,
                "S-102",
                5,
                fechaActual,
                hora(9, 5),
                hora(10, 0),
                1,
                "Reunión de coordinación docente",
                EstadoReserva.RESERVADA
            ),
            reserva(
                4,
                "MUL-01",
                6,
                fechaActual,
                hora(8, 0),
                hora(10, 0),
                2,
                "Charla de bienvenida",
                EstadoReserva.VERIFICADA,
                hora(8, 5)
            ),
            reserva(
                5,
                "S-101",
                3,
                siguienteDiaHabil,
                hora(10, 5),
                hora(12, 0),
                2,
                "Clase de Programación Móvil",
                EstadoReserva.RESERVADA
            ),
            reserva(
                6,
                "LAB-02",
                1,
                tercerDiaHabil,
                hora(15, 5),
                hora(16, 0),
                1,
                "Capacitación de coordinadores",
                EstadoReserva.RESERVADA
            ),
            reserva(
                7,
                "S-201",
                3,
                anteriorDiaHabil,
                hora(9, 5),
                hora(10, 0),
                1,
                "Tutoría",
                EstadoReserva.NO_PRESENTADO
            ),
            reserva(
                8,
                "LAB-01",
                3,
                segundoDiaAnteriorHabil,
                hora(11, 5),
                hora(12, 0),
                1,
                "Laboratorio de práctica",
                EstadoReserva.CANCELADA
            )
        )
    }

    private fun esDiaHabil(fecha: LocalDate): Boolean {
        return fecha.dayOfWeek != DayOfWeek.SATURDAY &&
                fecha.dayOfWeek != DayOfWeek.SUNDAY
    }

    private fun ajustarDiaHabil(fecha: LocalDate): LocalDate {
        var resultado = fecha

        while (!esDiaHabil(resultado)) {
            resultado = resultado.plusDays(1)
        }

        return resultado
    }

    private fun sumarDiasHabiles(
        fecha: LocalDate,
        cantidad: Int
    ): LocalDate {
        var resultado = fecha
        var diasContados = 0

        while (diasContados < cantidad) {
            resultado = resultado.plusDays(1)

            if (esDiaHabil(resultado)) {
                diasContados++
            }
        }

        return resultado
    }

    private fun restarDiasHabiles(
        fecha: LocalDate,
        cantidad: Int
    ): LocalDate {
        var resultado = fecha
        var diasContados = 0

        while (diasContados < cantidad) {
            resultado = resultado.minusDays(1)

            if (esDiaHabil(resultado)) {
                diasContados++
            }
        }

        return resultado
    }

    private fun hora(
        horas: Int,
        minutos: Int
    ): LocalTime {
        return LocalTime.of(horas, minutos)
    }

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
        val usuario = usuarios.first {
            it.id == idUsuario
        }

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
            creada = LocalDateTime.of(
                fecha.minusDays(3),
                LocalTime.of(18, 40)
            )
        )
    }
}
