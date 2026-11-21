package cl.duoc.reservasalas.salas.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import cl.duoc.reservasalas.salas.model.Equipo;

/**
 * Calcula tieneProyector, tieneComputadores y tieneClimatizacion a partir de la
 * lista de equipos de una sala. Compara el nombre sin mayúsculas ni tildes, por
 * palabra clave. Un equipo CON_FALLA cuenta igual: la sala lo tiene, y la falla
 * queda descrita en su comentario.
 */
public final class CalculadoraEquipamiento {

    private CalculadoraEquipamiento() {
    }

    public static boolean tieneProyector(List<Equipo> equipos) {
        return algunoContiene(equipos, "proyector");
    }

    public static boolean tieneComputadores(List<Equipo> equipos) {
        return algunoContiene(equipos, "computador");
    }

    public static boolean tieneClimatizacion(List<Equipo> equipos) {
        return algunoContiene(equipos, "climatiz", "aire acondicionado");
    }

    private static boolean algunoContiene(List<Equipo> equipos, String... palabrasClave) {
        if (equipos == null) {
            return false;
        }
        for (Equipo equipo : equipos) {
            String nombre = normalizar(equipo.getNombre());
            for (String palabra : palabrasClave) {
                if (nombre.contains(palabra)) {
                    return true;
                }
            }
        }
        return false;
    }

    // "Climatización" -> "climatizacion": separa las tildes de las letras y las quita.
    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
