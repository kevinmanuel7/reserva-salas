package cl.duoc.reservasalas.app.model

/** Lo que se guarda en el teléfono después de un login correcto. */
data class Sesion(
    val token: String,
    val usuario: Usuario
)
