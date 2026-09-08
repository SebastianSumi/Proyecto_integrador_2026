package pe.edu.upeu.saludablemente.actividades.inscripcion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.ActividadRepository;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.RespuestaInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.mapper.InscripcionMapper;
import pe.edu.upeu.saludablemente.actividades.inscripcion.repository.InscripcionRepository;
import pe.edu.upeu.saludablemente.exception.ConflictException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InscripcionService {
    private final ActividadRepository actividadRepository;
    private final InscripcionRepository inscripcionRepository;
    private final InscripcionMapper inscripcionMapper;

    public InscripcionService(ActividadRepository actividadRepository, InscripcionRepository inscripcionRepository,
                             InscripcionMapper inscripcionMapper) {
        this.actividadRepository = actividadRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.inscripcionMapper = inscripcionMapper;
    }

    public List<RespuestaInscripcion> findByActivity(Long activityId, boolean includeCancelled) {
        requireActivity(activityId);
        List<Inscripcion> enrollments = includeCancelled
                ? inscripcionRepository.findAllByActivityIdOrderByEnrolledAtAsc(activityId)
                : inscripcionRepository.findAllByActivityIdAndStateOrderByEnrolledAtAsc(activityId, EstadoInscripcion.ENROLLED);
        return enrollments.stream().map(inscripcionMapper::toResponse).toList();
    }

    @Transactional
    public RespuestaInscripcion enroll(Long activityId, Long personId) {
        return enrollBatch(activityId, List.of(personId)).getFirst();
    }

    @Transactional
    public List<RespuestaInscripcion> enrollBatch(Long activityId, List<Long> personIds) {
        requireScheduledActivityForUpdate(activityId);
        validateBatch(personIds);
        LocalDateTime now = LocalDateTime.now();
        List<Inscripcion> enrollments = personIds.stream().map(personId -> {
            Inscripcion enrollment = inscripcionRepository.findByActivityIdAndPersonId(activityId, personId).orElse(null);
            if (enrollment != null && enrollment.getState() == EstadoInscripcion.ENROLLED) {
                throw new ConflictException("Person is already enrolled in this activity: " + personId);
            }
            if (enrollment == null) {
                enrollment = new Inscripcion(activityId, personId, now);
            } else {
                enrollment.reactivate(now);
            }
            return inscripcionRepository.save(enrollment);
        }).toList();
        return enrollments.stream().map(inscripcionMapper::toResponse).toList();
    }

    @Transactional
    public RespuestaInscripcion cancel(Long activityId, Long personId) {
        requireScheduledActivityForUpdate(activityId);
        Inscripcion enrollment = inscripcionRepository.findByActivityIdAndPersonId(activityId, personId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", personId));
        enrollment.cancel(LocalDateTime.now());
        return inscripcionMapper.toResponse(enrollment);
    }

    private void validateBatch(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            throw new IllegalArgumentException("At least one person id is required");
        }
        if (personIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Person ids must be positive");
        }
        if (new HashSet<>(personIds).size() != personIds.size()) {
            throw new IllegalArgumentException("Person ids must not be duplicated in the same batch");
        }
    }

    private Actividad requireScheduledActivityForUpdate(Long activityId) {
        Actividad activity = actividadRepository.findByIdForUpdate(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
        if (activity.getState() != EstadoActividad.SCHEDULED) {
            throw new ConflictException("Enrollment can only change while the activity is scheduled");
        }
        return activity;
    }

    private Actividad requireActivity(Long activityId) {
        return actividadRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
    }
}
