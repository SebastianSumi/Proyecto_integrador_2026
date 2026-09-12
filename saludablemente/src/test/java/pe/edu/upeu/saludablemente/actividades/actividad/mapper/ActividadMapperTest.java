package pe.edu.upeu.saludablemente.actividades.actividad.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActividadMapperTest {

    private final ActividadMapper mapper = Mappers.getMapper(ActividadMapper.class);

    @Test
    void mapsRequestFieldsAndPreservesEntityManagedFields() {
        ActividadRequest request = new ActividadRequest();
        request.setNombre("Caminata saludable");
        request.setFecha(LocalDate.of(2026, 9, 12));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(9, 0));
        request.setLugar("Parque central");
        request.setCreadorId(7L);

        Actividad actividad = mapper.toEntity(request);

        assertEquals("Caminata saludable", actividad.getNombre());
        assertEquals(LocalDate.of(2026, 9, 12), actividad.getFecha());
        assertEquals(LocalTime.of(8, 0), actividad.getHoraInicio());
        assertEquals(LocalTime.of(9, 0), actividad.getHoraFin());
        assertEquals("Parque central", actividad.getLugar());
        assertEquals(7L, actividad.getCreadorId());
        assertNull(actividad.getId());
        assertEquals(EstadoActividad.PROGRAMADA, actividad.getEstado());
    }

    @Test
    void mapsAllEntityFieldsToResponse() {
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Caminata saludable");
        actividad.setFecha(LocalDate.of(2026, 9, 12));
        actividad.setHoraInicio(LocalTime.of(8, 0));
        actividad.setHoraFin(LocalTime.of(9, 0));
        actividad.setLugar("Parque central");
        actividad.setEstado(EstadoActividad.EN_CURSO);
        actividad.setCreadorId(7L);

        ActividadResponse response = mapper.toResponse(actividad);

        assertEquals(1L, response.getId());
        assertEquals("Caminata saludable", response.getNombre());
        assertEquals(LocalDate.of(2026, 9, 12), response.getFecha());
        assertEquals(LocalTime.of(8, 0), response.getHoraInicio());
        assertEquals(LocalTime.of(9, 0), response.getHoraFin());
        assertEquals("Parque central", response.getLugar());
        assertEquals(EstadoActividad.EN_CURSO, response.getEstado());
        assertEquals(7L, response.getCreadorId());
    }
}
