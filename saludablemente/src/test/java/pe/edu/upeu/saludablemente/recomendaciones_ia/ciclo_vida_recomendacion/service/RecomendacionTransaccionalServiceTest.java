package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.saludablemente.auditoria.service.AuditoriaService;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.mapper.RecomendacionMapper;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.repository.RecomendacionDetalleRepository;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.repository.RecomendacionIARepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecomendacionTransaccionalServiceTest {

    @Mock
    private RecomendacionIARepository recomendacionRepository;
    @Mock
    private RecomendacionDetalleRepository detalleRepository;
    @Mock
    private VigenciaVersionadoService vigenciaVersionadoService;
    @Mock
    private RecomendacionMapper mapper;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private RecomendacionTransaccionalService service;

    @Test
    void obtenerDetalleAuditoriaThrowsNotFoundWhenRecommendationDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(recomendacionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerDetalleAuditoria(id));
    }

    @Test
    void confirmarLecturaThrowsNotFoundWhenRecommendationDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(recomendacionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.confirmarLectura(id));
    }
}