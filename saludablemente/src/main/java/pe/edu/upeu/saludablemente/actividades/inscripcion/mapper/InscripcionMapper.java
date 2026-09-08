package pe.edu.upeu.saludablemente.actividades.inscripcion.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.RespuestaInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;

@Component
public class InscripcionMapper {
    public RespuestaInscripcion toResponse(Inscripcion enrollment) {
        return new RespuestaInscripcion(enrollment.getId(), enrollment.getActivityId(), enrollment.getPersonId(),
                enrollment.getState(), enrollment.getEnrolledAt(), enrollment.getCancelledAt());
    }
}
