package pe.edu.upeu.saludablemente.metas.meta.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.saludablemente.metas.meta.entity.EstadoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidCreateRequest() {
        MetaCreateRequest request = new MetaCreateRequest();
        request.setPersonaId(0L);
        request.setTipoMeta(" ");
        request.setDescripcion("x".repeat(201));
        request.setValorObjetivo(BigDecimal.ZERO);
        request.setValorActual(new BigDecimal("-0.01"));
        request.setFechaInicio(LocalDate.of(2026, 9, 1));
        request.setFechaLimite(LocalDate.of(2026, 12, 1));

        assertEquals(5, validator.validate(request).size());
    }

    @Test
    void acceptsValidCreateRequest() {
        MetaCreateRequest request = validCreateRequest();

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsInvalidUpdateRequestWithoutAllowingPersonaReplacement() {
        MetaUpdateRequest request = new MetaUpdateRequest();
        request.setTipoMeta(" ");
        request.setValorObjetivo(BigDecimal.ZERO);
        request.setValorActual(new BigDecimal("-0.01"));

        assertEquals(5, validator.validate(request).size());
        assertTrue(java.util.Arrays.stream(MetaUpdateRequest.class.getDeclaredFields())
                .noneMatch(field -> field.getName().equals("personaId")));
    }

    @Test
    void buildsPublicResponseWithAllMetaFields() {
        MetaResponse response = MetaResponse.builder()
                .id(1L)
                .personaId(9L)
                .tipoMeta("Peso")
                .descripcion("Reach a healthy weight")
                .valorObjetivo(new BigDecimal("65.00"))
                .valorActual(new BigDecimal("68.50"))
                .fechaInicio(LocalDate.of(2026, 9, 1))
                .fechaLimite(LocalDate.of(2026, 12, 1))
                .estado(EstadoMeta.EN_CURSO)
                .build();

        assertEquals(1L, response.getId());
        assertEquals(9L, response.getPersonaId());
        assertEquals("Peso", response.getTipoMeta());
        assertEquals(new BigDecimal("65.00"), response.getValorObjetivo());
        assertEquals(EstadoMeta.EN_CURSO, response.getEstado());
    }

    private MetaCreateRequest validCreateRequest() {
        MetaCreateRequest request = new MetaCreateRequest();
        request.setPersonaId(9L);
        request.setTipoMeta("Peso");
        request.setDescripcion("Reach a healthy weight");
        request.setValorObjetivo(new BigDecimal("65.00"));
        request.setValorActual(new BigDecimal("68.50"));
        request.setFechaInicio(LocalDate.of(2026, 9, 1));
        request.setFechaLimite(LocalDate.of(2026, 12, 1));
        return request;
    }
}