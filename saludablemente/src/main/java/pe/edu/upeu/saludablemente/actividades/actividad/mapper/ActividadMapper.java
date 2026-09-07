package pe.edu.upeu.saludablemente.actividades.actividad.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.RespuestaActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;

import java.time.LocalDateTime;

@Component
public class ActividadMapper {

    public Actividad toEntity(SolicitudActividad request, String place, String placeKey) {
        return new Actividad(normalize(request.name()), normalizeNullable(request.description()),
                LocalDateTime.of(request.activityDate(), request.startTime()),
                LocalDateTime.of(request.activityDate(), request.endTime()),
                place, placeKey, request.creatorUserId());
    }

    public void updateEntity(Actividad activity, SolicitudActividad request, String place, String placeKey) {
        activity.updateDetails(normalize(request.name()), normalizeNullable(request.description()),
                LocalDateTime.of(request.activityDate(), request.startTime()),
                LocalDateTime.of(request.activityDate(), request.endTime()), place, placeKey);
    }

    public RespuestaActividad toResponse(Actividad activity) {
        return new RespuestaActividad(activity.getId(), activity.getName(), activity.getDescription(),
                activity.getStartAt().toLocalDate(), activity.getStartAt().toLocalTime(),
                activity.getEndAt().toLocalTime(), activity.getPlace(), activity.getState(),
                activity.getCreatorUserId());
    }

    private String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
