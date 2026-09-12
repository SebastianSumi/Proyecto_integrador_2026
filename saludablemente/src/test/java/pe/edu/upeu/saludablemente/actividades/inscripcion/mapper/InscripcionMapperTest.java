package pe.edu.upeu.saludablemente.actividades.inscripcion.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionRequest;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionResponse;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InscripcionMapperTest {

    private final InscripcionMapper mapper = Mappers.getMapper(InscripcionMapper.class);

    @Test
    void mapsRequestFieldsAndIgnoresEntityManagedFields() {
        InscripcionRequest request = new InscripcionRequest();
        request.setActividadId(10L);
        request.setPersonaId(20L);

        Inscripcion inscripcion = mapper.toEntity(request);

        assertEquals(10L, inscripcion.getActividadId());
        assertEquals(20L, inscripcion.getPersonaId());
        assertNull(inscripcion.getId());
        assertEquals(EstadoInscripcion.INSCRITA, inscripcion.getEstado());
        assertNull(inscripcion.getInscritaEn());
        assertNull(inscripcion.getCanceladaEn());
    }

    @Test
    void mapsFullEntityLifecycleToResponse() {
        LocalDateTime inscritaEn = LocalDateTime.of(2026, 9, 12, 9, 30);
        LocalDateTime canceladaEn = LocalDateTime.of(2026, 9, 13, 10, 15);
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(1L);
        inscripcion.setActividadId(10L);
        inscripcion.setPersonaId(20L);
        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcion.setInscritaEn(inscritaEn);
        inscripcion.setCanceladaEn(canceladaEn);

        InscripcionResponse response = mapper.toResponse(inscripcion);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getActividadId());
        assertEquals(20L, response.getPersonaId());
        assertEquals(EstadoInscripcion.CANCELADA, response.getEstado());
        assertEquals(inscritaEn, response.getInscritaEn());
        assertEquals(canceladaEn, response.getCanceladaEn());
    }
}
