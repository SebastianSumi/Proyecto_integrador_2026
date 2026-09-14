package pe.edu.upeu.saludablemente.actividades.inscripcion.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InscripcionDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsRequestWithoutPositiveIdentifiers() {
        InscripcionRequest request = new InscripcionRequest();
        request.setActividadId(0L);
        request.setPersonaId(null);

        assertEquals(2, validator.validate(request).size());
    }

    @Test
    void acceptsRequestWithPositiveIdentifiers() {
        InscripcionRequest request = new InscripcionRequest();
        request.setActividadId(3L);
        request.setPersonaId(8L);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void buildsPublicResponseWithServerControlledLifecycle() {
        LocalDateTime inscritaEn = LocalDateTime.of(2026, 9, 12, 10, 30);
        LocalDateTime canceladaEn = LocalDateTime.of(2026, 9, 13, 9, 0);

        InscripcionResponse response = InscripcionResponse.builder()
                .id(1L)
                .actividadId(3L)
                .personaId(8L)
                .estado(EstadoInscripcion.CANCELADA)
                .inscritaEn(inscritaEn)
                .canceladaEn(canceladaEn)
                .build();

        assertEquals(1L, response.getId());
        assertEquals(3L, response.getActividadId());
        assertEquals(8L, response.getPersonaId());
        assertEquals(EstadoInscripcion.CANCELADA, response.getEstado());
        assertEquals(inscritaEn, response.getInscritaEn());
        assertEquals(canceladaEn, response.getCanceladaEn());
    }
}
