package cl.duoc.reservasalas.app

import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.model.Usuario
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/*
 * Datos y ayudas compartidas por varias clases de prueba, para no repetirlos en cada una.
 */

/** Lunes 23 de noviembre de 2026: el "hoy" de casi todas las pruebas. */
val HOY_PRUEBA: LocalDate = LocalDate.of(2026, 11, 23)

/** Relatora de prueba (id 7) y su sesión. */
val RELATORA_PRUEBA = Usuario(7, "Ana María Relatora", "ana@duocuc.test", Rol.RELATOR)
val SESION_PRUEBA = Sesion("token-de-prueba", RELATORA_PRUEBA)

/** Reloj detenido en [ahora]: así las reglas que dependen de la hora dan siempre lo mismo. */
fun relojFijo(ahora: LocalDateTime): Reloj = object : Reloj {
    override fun ahora(): LocalDateTime = ahora
}

/** Reloj detenido hoy (23/11/2026) a la [hora] indicada. */
fun relojFijo(hora: LocalTime): Reloj = relojFijo(LocalDateTime.of(HOY_PRUEBA, hora))

/** Una reserva de prueba. Por defecto: sala S-101, hoy 10:05–11:00, de la relatora 7, Reservada. */
fun reservaDePrueba(
    id: Int = 1,
    codigoSala: String = "S-101",
    idUsuario: Int = RELATORA_PRUEBA.id,
    fecha: LocalDate = HOY_PRUEBA,
    inicio: LocalTime = LocalTime.of(10, 5),
    fin: LocalTime = inicio.withMinute(0).plusHours(1),
    estado: EstadoReserva = EstadoReserva.RESERVADA,
    horaCheckIn: LocalTime? = null
) = Reserva(
    id = id,
    codigoSala = codigoSala,
    idUsuario = idUsuario,
    nombreUsuario = "Ana María Relatora",
    emailUsuario = "ana@duocuc.test",
    fecha = fecha,
    horaInicio = inicio,
    horaFin = fin,
    cantidadBloques = 1,
    motivo = "Excel intermedio",
    estado = estado,
    horaCheckIn = horaCheckIn,
    creada = LocalDateTime.of(fecha.minusDays(1), LocalTime.NOON)
)

/**
 * La misma excepción que lanza Retrofit cuando el servidor responde con un error.
 * [cuerpo] es el JSON de error, ej. {"codigo":"USUARIO_OCUPADO","mensaje":"..."}.
 */
fun errorHttp(codigo: Int, cuerpo: String = ""): HttpException =
    HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

/** JSON de error estándar de los microservicios (plan, sección 5). */
fun jsonError(codigo: String, mensaje: String) = """{"codigo":"$codigo","mensaje":"$mensaje"}"""
