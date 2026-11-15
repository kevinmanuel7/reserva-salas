package cl.duoc.reservasalas.reservas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.reservasalas.reservas.dto.BloqueDto;
import cl.duoc.reservasalas.reservas.model.Bloque;
import cl.duoc.reservasalas.reservas.repository.BloqueRepository;

@ExtendWith(MockitoExtension.class)
class BloqueServiceTest {

    @Mock
    private BloqueRepository bloqueRepository;

    @InjectMocks
    private BloqueService bloqueService;

    @Test
    void listarBloques_devuelveListaOrdenada() {
        when(bloqueRepository.findAllByOrderByNumeroAsc()).thenReturn(List.of(
                new Bloque(1, "08:00", "09:00"),
                new Bloque(2, "09:05", "10:00")
        ));

        List<BloqueDto> resultado = bloqueService.listarBloques();

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).numero());
        assertEquals("08:00", resultado.get(0).horaInicio());
        verify(bloqueRepository).findAllByOrderByNumeroAsc();
    }
}
