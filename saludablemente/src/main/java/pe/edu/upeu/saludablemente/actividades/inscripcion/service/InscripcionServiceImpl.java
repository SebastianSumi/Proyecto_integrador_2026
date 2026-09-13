package pe.edu.upeu.saludablemente.actividades.inscripcion.service;

import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InscripcionServiceImpl implements InscripcionService {

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
        actividadService.findById(request.getActividadId());

        if (repository.existsByActividadIdAndPersonaIdAndEstado(
                request.getActividadId(), request.getPersonaId(), EstadoInscripcion.INSCRITA)) {
            throw new InscripcionVigenteException("La persona ya cuenta con una inscripción vigente para esta actividad");
        }

        Inscripcion inscripcion = mapper.toEntity(request);
        inscripcion.setEstado(EstadoInscripcion.INSCRITA);
        inscripcion.setInscritaEn(LocalDateTime.now());
        return mapper.toResponse(repository.save(inscripcion));
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
        return mapper.toResponse(repository.save(inscripcion));
    }

    private Inscripcion findInscripcion(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion with id " + id + " was not found"));
    }
}
