package pe.edu.upeu.saludablemente.metas.meta.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaCreateRequest;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaResponse;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaUpdateRequest;
import pe.edu.upeu.saludablemente.metas.meta.entity.EstadoMeta;
import pe.edu.upeu.saludablemente.metas.meta.entity.Meta;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetaMapperTest {

    private final MetaMapper mapper = Mappers.getMapper(MetaMapper.class);

    @Test
    void mapsCreateRequestAndPreservesEntityManagedFields() {
        MetaCreateRequest request = new MetaCreateRequest();
        request.setPersonaId(7L);
        request.setTipoMeta("Peso");
        request.setDescripcion("Alcanzar un peso saludable");
        request.setValorObjetivo(new BigDecimal("65.00"));
        request.setValorActual(new BigDecimal("72.50"));
        request.setFechaInicio(LocalDate.of(2026, 9, 1));
        request.setFechaLimite(LocalDate.of(2026, 12, 1));

        Meta meta = mapper.toEntity(request);

        assertEquals(7L, meta.getPersonaId());
        assertEquals("Peso", meta.getTipoMeta());
        assertEquals("Alcanzar un peso saludable", meta.getDescripcion());
        assertEquals(new BigDecimal("65.00"), meta.getValorObjetivo());
        assertEquals(new BigDecimal("72.50"), meta.getValorActual());
        assertEquals(LocalDate.of(2026, 9, 1), meta.getFechaInicio());
        assertEquals(LocalDate.of(2026, 12, 1), meta.getFechaLimite());
        assertNull(meta.getId());
        assertEquals(EstadoMeta.EN_CURSO, meta.getEstado());
    }

    @Test
    void mapsAllEntityFieldsToResponse() {
        Meta meta = meta(10L, 8L, EstadoMeta.CUMPLIDA);

        MetaResponse response = mapper.toResponse(meta);

        assertEquals(10L, response.getId());
        assertEquals(8L, response.getPersonaId());
        assertEquals("Peso", response.getTipoMeta());
        assertEquals("Alcanzar un peso saludable", response.getDescripcion());
        assertEquals(new BigDecimal("65.00"), response.getValorObjetivo());
        assertEquals(new BigDecimal("65.00"), response.getValorActual());
        assertEquals(LocalDate.of(2026, 9, 1), response.getFechaInicio());
        assertEquals(LocalDate.of(2026, 12, 1), response.getFechaLimite());
        assertEquals(EstadoMeta.CUMPLIDA, response.getEstado());
    }

    @Test
    void updatesOnlyMutableFields() {
        Meta meta = meta(10L, 8L, EstadoMeta.EN_CURSO);
        MetaUpdateRequest request = new MetaUpdateRequest();
        request.setTipoMeta("Grasa visceral");
        request.setDescripcion("Reducir el indicador");
        request.setValorObjetivo(new BigDecimal("8.00"));
        request.setValorActual(new BigDecimal("10.00"));
        request.setFechaInicio(LocalDate.of(2026, 10, 1));
        request.setFechaLimite(LocalDate.of(2027, 1, 1));

        mapper.updateEntity(request, meta);

        assertEquals(10L, meta.getId());
        assertEquals(8L, meta.getPersonaId());
        assertEquals(EstadoMeta.EN_CURSO, meta.getEstado());
        assertEquals("Grasa visceral", meta.getTipoMeta());
        assertEquals("Reducir el indicador", meta.getDescripcion());
        assertEquals(new BigDecimal("8.00"), meta.getValorObjetivo());
        assertEquals(new BigDecimal("10.00"), meta.getValorActual());
        assertEquals(LocalDate.of(2026, 10, 1), meta.getFechaInicio());
        assertEquals(LocalDate.of(2027, 1, 1), meta.getFechaLimite());
    }

    private Meta meta(Long id, Long personaId, EstadoMeta estado) {
        Meta meta = new Meta();
        meta.setId(id);
        meta.setPersonaId(personaId);
        meta.setTipoMeta("Peso");
        meta.setDescripcion("Alcanzar un peso saludable");
        meta.setValorObjetivo(new BigDecimal("65.00"));
        meta.setValorActual(new BigDecimal("65.00"));
        meta.setFechaInicio(LocalDate.of(2026, 9, 1));
        meta.setFechaLimite(LocalDate.of(2026, 12, 1));
        meta.setEstado(estado);
        return meta;
    }
}
