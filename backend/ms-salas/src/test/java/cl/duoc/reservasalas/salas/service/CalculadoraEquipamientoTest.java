package cl.duoc.reservasalas.salas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import cl.duoc.reservasalas.salas.model.Equipo;
import cl.duoc.reservasalas.salas.model.EstadoEquipo;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pruebas del cálculo de tieneProyector, tieneComputadores y tieneClimatizacion
 * a partir de la lista de equipos. No levantan Spring.
 */
class CalculadoraEquipamientoTest {

    private static Equipo operativo(String nombre) {
        return new Equipo(nombre, EstadoEquipo.OPERATIVO, null);
    }

    @Test
    void proyector_seDetectaPorSuNombre() {
        List<Equipo> equipos = List.of(operativo("Proyector"));

        assertTrue(CalculadoraEquipamiento.tieneProyector(equipos));
        assertFalse(CalculadoraEquipamiento.tieneComputadores(equipos));
        assertFalse(CalculadoraEquipamiento.tieneClimatizacion(equipos));
    }

    @Test
    void computadores_seDetectaPorSuNombre() {
        List<Equipo> equipos = List.of(operativo("Computadores"));

        assertFalse(CalculadoraEquipamiento.tieneProyector(equipos));
        assertTrue(CalculadoraEquipamiento.tieneComputadores(equipos));
        assertFalse(CalculadoraEquipamiento.tieneClimatizacion(equipos));
    }

    @Test
    void climatizacion_seDetectaPorSuNombre() {
        List<Equipo> equipos = List.of(operativo("Climatización"));

        assertFalse(CalculadoraEquipamiento.tieneProyector(equipos));
        assertFalse(CalculadoraEquipamiento.tieneComputadores(equipos));
        assertTrue(CalculadoraEquipamiento.tieneClimatizacion(equipos));
    }

    @Test
    void ignoraMayusculasYTildes() {
        assertTrue(CalculadoraEquipamiento.tieneClimatizacion(List.of(operativo("CLIMATIZACIÓN"))));
        assertTrue(CalculadoraEquipamiento.tieneClimatizacion(List.of(operativo("climatizacion"))));
        assertTrue(CalculadoraEquipamiento.tieneProyector(List.of(operativo("PROYECTOR"))));
        assertTrue(CalculadoraEquipamiento.tieneComputadores(List.of(operativo("computadores"))));
    }

    @Test
    void unEquipoConFalla_cuentaIgual() {
        List<Equipo> equipos = List.of(new Equipo("Proyector", EstadoEquipo.CON_FALLA, "La imagen parpadea"));

        assertTrue(CalculadoraEquipamiento.tieneProyector(equipos));
    }

    @Test
    void listaVacia_dejaLosTresEnFalso() {
        List<Equipo> equipos = List.of();

        assertFalse(CalculadoraEquipamiento.tieneProyector(equipos));
        assertFalse(CalculadoraEquipamiento.tieneComputadores(equipos));
        assertFalse(CalculadoraEquipamiento.tieneClimatizacion(equipos));
    }

    @Test
    void equiposSinPalabraClave_dejanLosTresEnFalso() {
        List<Equipo> equipos = List.of(operativo("Pizarra"), operativo("Parlantes"));

        assertFalse(CalculadoraEquipamiento.tieneProyector(equipos));
        assertFalse(CalculadoraEquipamiento.tieneComputadores(equipos));
        assertFalse(CalculadoraEquipamiento.tieneClimatizacion(equipos));
    }

    /**
     * Los booleanos guardados en docs/dataset/salas.json deben coincidir con los que
     * se calculan desde su lista de equipos. Si alguien edita el dataset y los deja
     * contradictorios, esta prueba falla.
     */
    @Test
    void datasetDeSalas_booleanosCoincidenConSusEquipos() throws IOException {
        ObjectMapper mapper = JsonMapper.builder().build();
        List<Map<String, Object>> salas;
        try (InputStream json = new ClassPathResource("dataset/salas.json").getInputStream()) {
            salas = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() { });
        }

        assertEquals(9, salas.size());
        for (Map<String, Object> sala : salas) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lista = (List<Map<String, Object>>) sala.get("equipos");
            List<Equipo> equipos = lista.stream()
                    .map(e -> new Equipo((String) e.get("nombre"),
                            EstadoEquipo.valueOf((String) e.get("estado")),
                            (String) e.get("comentario")))
                    .toList();
            String codigo = (String) sala.get("codigo");

            assertEquals(sala.get("tieneProyector"), CalculadoraEquipamiento.tieneProyector(equipos), codigo);
            assertEquals(sala.get("tieneComputadores"), CalculadoraEquipamiento.tieneComputadores(equipos), codigo);
            assertEquals(sala.get("tieneClimatizacion"), CalculadoraEquipamiento.tieneClimatizacion(equipos), codigo);
        }
    }
}
