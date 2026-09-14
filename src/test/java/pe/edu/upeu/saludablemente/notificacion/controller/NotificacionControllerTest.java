package pe.edu.upeu.saludablemente.notificacion.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import pe.edu.upeu.saludablemente.notificacion.dto.NotificacionDto;
import pe.edu.upeu.saludablemente.notificacion.service.NotificacionService;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController controller;

    @Test
    void createsNotificationWithCreatedStatus() {
        NotificacionDto request = new NotificacionDto();
        NotificacionDto response = new NotificacionDto();
        response.setId(20L);
        when(notificacionService.enviarNotificacion(request)).thenReturn(response);

        var result = controller.enviar(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(notificacionService).enviarNotificacion(request);
    }
}
