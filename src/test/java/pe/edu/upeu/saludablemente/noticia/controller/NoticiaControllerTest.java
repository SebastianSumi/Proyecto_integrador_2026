package pe.edu.upeu.saludablemente.noticia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import pe.edu.upeu.saludablemente.noticia.dto.NoticiaDto;
import pe.edu.upeu.saludablemente.noticia.service.NoticiaService;

@ExtendWith(MockitoExtension.class)
class NoticiaControllerTest {

    @Mock
    private NoticiaService noticiaService;

    @InjectMocks
    private NoticiaController controller;

    @Test
    void createsNewsWithCreatedStatus() {
        NoticiaDto request = new NoticiaDto();
        NoticiaDto response = new NoticiaDto();
        response.setId(10L);
        when(noticiaService.crearNoticia(request)).thenReturn(response);

        var result = controller.crear(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(noticiaService).crearNoticia(request);
    }
}
