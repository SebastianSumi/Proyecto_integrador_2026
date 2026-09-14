package pe.edu.upeu.saludablemente.asistencia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import pe.edu.upeu.saludablemente.asistencia.dto.AsistenciaDto;
import pe.edu.upeu.saludablemente.asistencia.dto.SincronizacionOfflineDto;
import pe.edu.upeu.saludablemente.asistencia.service.AsistenciaService;

@ExtendWith(MockitoExtension.class)
class AsistenciaControllerTest {

    @Mock
    private AsistenciaService asistenciaService;

    @InjectMocks
    private AsistenciaController controller;

    @Test
    void createsAttendanceWithCreatedStatus() {
        AsistenciaDto request = new AsistenciaDto();
        AsistenciaDto response = new AsistenciaDto();
        response.setId(30L);
        when(asistenciaService.registrarAsistencia(request)).thenReturn(response);

        var result = controller.registrar(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(asistenciaService).registrarAsistencia(request);
    }

    @Test
    void keepsOfflineSynchronizationAsOk() {
        AsistenciaDto registro = new AsistenciaDto();
        SincronizacionOfflineDto payload = new SincronizacionOfflineDto();
        payload.setRegistrosOffline(List.of(registro));
        when(asistenciaService.sincronizarAsistenciasOffline(payload.getRegistrosOffline()))
                .thenReturn(List.of(registro));

        var result = controller.sincronizarOffline(payload);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(registro), result.getBody());
        verify(asistenciaService).sincronizarAsistenciasOffline(payload.getRegistrosOffline());
    }
}
