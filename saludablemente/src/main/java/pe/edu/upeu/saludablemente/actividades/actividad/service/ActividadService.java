package pe.edu.upeu.saludablemente.actividades.actividad.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.RespuestaActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudEstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.mapper.ActividadMapper;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.BloqueoLugarActividadRepository;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.ActividadRepository;
import pe.edu.upeu.saludablemente.exception.ConflictException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final BloqueoLugarActividadRepository bloqueoLugarRepository;
    private final ActividadMapper actividadMapper;

    public ActividadService(ActividadRepository actividadRepository,
                           BloqueoLugarActividadRepository bloqueoLugarRepository,
                           ActividadMapper actividadMapper) {
        this.actividadRepository = actividadRepository;
        this.bloqueoLugarRepository = bloqueoLugarRepository;
        this.actividadMapper = actividadMapper;
    }

    public List<RespuestaActividad> findAll(EstadoActividad state) {
        List<Actividad> activities = state == null
                ? actividadRepository.findAll()
                : actividadRepository.findAllByState(state);
        return activities.stream().map(actividadMapper::toResponse).toList();
    }

    public RespuestaActividad findById(Long id) {
        return actividadMapper.toResponse(findActivity(id));
    }

    @Transactional
    public RespuestaActividad create(SolicitudActividad request) {
        Schedule schedule = schedule(request);
        lockAndValidateOverlap(schedule, null);
        Actividad activity = actividadMapper.toEntity(request, schedule.place(), schedule.placeKey());
        return actividadMapper.toResponse(actividadRepository.save(activity));
    }

    @Transactional
    public RespuestaActividad update(Long id, SolicitudActividad request) {
        Actividad activity = findActivity(id);
        Schedule schedule = schedule(request);
        lockAndValidateOverlap(schedule, id);
        actividadMapper.updateEntity(activity, request, schedule.place(), schedule.placeKey());
        return actividadMapper.toResponse(activity);
    }

    @Transactional
    public RespuestaActividad updateState(Long id, SolicitudEstadoActividad request) {
        Actividad activity = findActivity(id);
        activity.changeState(request.state());
        return actividadMapper.toResponse(activity);
    }

    private void lockAndValidateOverlap(Schedule schedule, Long excludedId) {
        bloqueoLugarRepository.ensureExists(schedule.placeKey());
        bloqueoLugarRepository.lockByPlaceKey(schedule.placeKey());
        if (actividadRepository.existsOverlapping(schedule.placeKey(), schedule.startAt(), schedule.endAt(), excludedId)) {
            throw new ConflictException("Another activity overlaps at this place");
        }
    }

    private Schedule schedule(SolicitudActividad request) {
        LocalDateTime startAt = LocalDateTime.of(request.activityDate(), request.startTime());
        LocalDateTime endAt = LocalDateTime.of(request.activityDate(), request.endTime());
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        String place = request.place().trim().replaceAll("\\s+", " ");
        return new Schedule(startAt, endAt, place, place.toUpperCase(Locale.ROOT));
    }

    private Actividad findActivity(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
    }

    private record Schedule(LocalDateTime startAt, LocalDateTime endAt, String place, String placeKey) {
    }
}
