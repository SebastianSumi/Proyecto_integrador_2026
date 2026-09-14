package pe.edu.upeu.saludablemente.actividades.inscripcion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.actividades.actividad.service.ActividadService;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionRequest;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionResponse;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.exception.InscripcionVigenteException;
import pe.edu.upeu.saludablemente.actividades.inscripcion.mapper.InscripcionMapper;
import pe.edu.upeu.saludablemente.actividades.inscripcion.repository.InscripcionRepository;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.observability.TransactionLog;

import java.time.LocalDateTime;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InscripcionServiceImpl implements InscripcionService {

    private static final String ACTIVE_ENROLLMENT_INDEX = "UK_INSCRIPCION_VIGENTE";

    private final InscripcionRepository repository;
    private final InscripcionMapper mapper;
    private final ActividadService actividadService;

    @Override
    public List<InscripcionResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public InscripcionResponse findById(Long id) {
        return mapper.toResponse(findInscripcion(id));
    }

    @Override
    @Transactional
    public InscripcionResponse register(InscripcionRequest request) {
        actividadService.validateExists(request.getActividadId());

        if (repository.existsByActividadIdAndPersonaIdAndEstado(
                request.getActividadId(), request.getPersonaId(), EstadoInscripcion.INSCRITA)) {
            throw new InscripcionVigenteException("La persona ya cuenta con una inscripción vigente para esta actividad");
        }

        Inscripcion inscripcion = mapper.toEntity(request);
        inscripcion.setEstado(EstadoInscripcion.INSCRITA);
        inscripcion.setInscritaEn(LocalDateTime.now());
        try {
            InscripcionResponse response = mapper.toResponse(repository.saveAndFlush(inscripcion));
            TransactionLog.afterCommit(() -> log.info(
                    "enrollment.registered id={} actividadId={} personaId={} estado={}", response.getId(),
                    response.getActividadId(), response.getPersonaId(), response.getEstado()));
            return response;
        } catch (DataIntegrityViolationException exception) {
            if (isActiveEnrollmentUniqueViolation(exception)) {
                throw new InscripcionVigenteException(
                        "La persona ya cuenta con una inscripción vigente para esta actividad"
                );
            }
            throw exception;
        }
    }

    @Override
    @Transactional
    public InscripcionResponse cancel(Long id) {
        Inscripcion inscripcion = findInscripcion(id);
        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA) {
            return mapper.toResponse(inscripcion);
        }

        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcion.setCanceladaEn(LocalDateTime.now());
        InscripcionResponse response = mapper.toResponse(repository.save(inscripcion));
        TransactionLog.afterCommit(() -> log.info(
                "enrollment.cancelled id={} actividadId={} personaId={} estado={}", response.getId(),
                response.getActividadId(), response.getPersonaId(), response.getEstado()));
        return response;
    }

    private Inscripcion findInscripcion(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion with id " + id + " was not found"));
    }

    private boolean isActiveEnrollmentUniqueViolation(DataIntegrityViolationException exception) {
        boolean oracleUniqueViolation = false;
        boolean knownIndexMentioned = false;
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Throwable current = exception; current != null && visited.add(current); current = current.getCause()) {
            String message = current.getMessage();
            if (message != null) {
                String normalizedMessage = message.toUpperCase(java.util.Locale.ROOT);
                oracleUniqueViolation |= normalizedMessage.contains("ORA-00001");
                knownIndexMentioned |= normalizedMessage.contains(ACTIVE_ENROLLMENT_INDEX);
            }
            if (current instanceof SQLException sqlException && sqlException.getErrorCode() == 1) {
                oracleUniqueViolation = true;
            }
        }

        return oracleUniqueViolation && knownIndexMentioned;
    }
}
