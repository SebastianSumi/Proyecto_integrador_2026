package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActividadDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidCreateRequest() {
        ActividadRequest request = new ActividadRequest();
        request.setNombre(" ");
        request.setFecha(null);
        request.setHoraInicio(null);
        request.setHoraFin(null);
        request.setLugar(" ");
        request.setCreadorId(0L);

        assertEquals(6, validator.validate(request).size());
    }

    @Test
    void acceptsValidCreateRequest() {
        ActividadRequest request = new ActividadRequest();
        request.setNombre("Caminata saludable");
        request.setFecha(LocalDate.of(2026, 9, 12));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(9, 0));
        request.setLugar("Parque central");
        request.setCreadorId(7L);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void buildsPublicResponse() {
        ActividadResponse response = ActividadResponse.builder()
                .id(1L)
                .nombre("Caminata saludable")
                .fecha(LocalDate.of(2026, 9, 12))
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(9, 0))
                .lugar("Parque central")
                .estado(EstadoActividad.PROGRAMADA)
                .creadorId(7L)
                .build();

        assertEquals(1L, response.getId());
        assertEquals("Caminata saludable", response.getNombre());
        assertEquals(EstadoActividad.PROGRAMADA, response.getEstado());
        assertEquals(7L, response.getCreadorId());
    }
}
