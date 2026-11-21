package cl.duoc.reservasalas.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Manejo de los archivos de la foto de perfil. Es una interfaz para que PerfilViewModel no
 * dependa de Android: en las pruebas se usa una versión falsa que no toca archivos.
 */
interface AlmacenFotoPerfil {
    /** Archivo temporal donde la cámara deja la foto recién tomada. */
    fun crearArchivoCamara(): Uri

    /** Copia la imagen de [origen] a la memoria interna, reducida, y devuelve la ruta del archivo. */
    suspend fun guardar(origen: Uri, idUsuario: Int): String

    /** Lee el archivo para mostrarlo en pantalla. null si no existe o no se puede leer. */
    suspend fun cargar(ruta: String): ImageBitmap?

    suspend fun borrar(ruta: String)
}

/**
 * Implementación real: guarda la foto en context.filesDir (memoria interna de la app, que
 * otras apps no pueden leer y que se borra al desinstalar).
 */
class AlmacenFotoPerfilArchivos(private val contexto: Context) : AlmacenFotoPerfil {

    // Misma carpeta y FileProvider que la cámara del check-in (res/xml/file_paths.xml: cache-path "fotos").
    override fun crearArchivoCamara(): Uri {
        val carpeta = File(contexto.cacheDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "perfil-${System.currentTimeMillis()}.jpg")
        archivo.createNewFile()
        return FileProvider.getUriForFile(contexto, AUTORIDAD_FILE_PROVIDER, archivo)
    }

    // withContext(Dispatchers.IO): leer y escribir archivos se hace fuera del hilo de la pantalla.
    override suspend fun guardar(origen: Uri, idUsuario: Int): String = withContext(Dispatchers.IO) {
        val reducida = leerReducida(origen) ?: throw IOException("No se pudo leer la imagen")
        val destino = File(contexto.filesDir, "foto_perfil_$idUsuario.jpg")
        destino.outputStream().use { salida ->
            reducida.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        }
        reducida.recycle()
        // Si era una foto de la cámara, el temporal ya no sirve.
        if (origen.authority == AUTORIDAD_FILE_PROVIDER) {
            origen.lastPathSegment?.let { File(File(contexto.cacheDir, "fotos"), it).delete() }
        }
        destino.absolutePath
    }

    override suspend fun cargar(ruta: String): ImageBitmap? = withContext(Dispatchers.IO) {
        BitmapFactory.decodeFile(ruta)?.asImageBitmap()
    }

    override suspend fun borrar(ruta: String) {
        withContext(Dispatchers.IO) { File(ruta).delete() }
    }

    /**
     * Lee la imagen sin cargarla entera: primero solo sus medidas, después una versión
     * más chica (inSampleSize) y al final la escala para que su lado mayor sea de 512 px.
     * Una foto de cámara de 12 megapíxeles ocuparía ~48 MB en memoria si se leyera completa.
     */
    private fun leerReducida(origen: Uri): Bitmap? {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contexto.contentResolver.openInputStream(origen)?.use { BitmapFactory.decodeStream(it, null, medidas) }
        val ladoMayor = max(medidas.outWidth, medidas.outHeight)
        if (ladoMayor <= 0) return null

        // inSampleSize = 2 lee 1 de cada 2 píxeles por lado (la imagen queda 4 veces más chica).
        var muestreo = 1
        while (ladoMayor / (muestreo * 2) >= LADO_MAXIMO) muestreo *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        val leida = contexto.contentResolver.openInputStream(origen)?.use {
            BitmapFactory.decodeStream(it, null, opciones)
        } ?: return null

        val escala = LADO_MAXIMO.toFloat() / max(leida.width, leida.height)
        if (escala >= 1f) return leida
        val reducida = Bitmap.createScaledBitmap(
            leida,
            (leida.width * escala).roundToInt(),
            (leida.height * escala).roundToInt(),
            true
        )
        if (reducida !== leida) leida.recycle()
        return reducida
    }

    private companion object {
        const val AUTORIDAD_FILE_PROVIDER = "cl.duoc.reservasalas.app.fileprovider"
        const val LADO_MAXIMO = 512
        const val CALIDAD_JPEG = 90
    }
}
