package pe.edu.upeu.saludablemente.actividades.actividad.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.ActividadSolapadaException;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.HorarioActividadInvalidoException;
import pe.edu.upeu.saludablemente.actividades.actividad.mapper.ActividadMapper;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.ActividadRepository;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository repository;
    private final ActividadMapper mapper;

    @Override
    public List<ActividadResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ActividadResponse findById(Long id) {
        return mapper.toResponse(findActividad(id));
    }

    @Override
    @Transactional
    public ActividadResponse create(ActividadRequest request) {
        validarIntervaloHorario(request);
        validarSinSolapamiento(request, null);
        Actividad actividad = mapper.toEntity(request);
        return mapper.toResponse(repository.save(actividad));
    }

    @Override
    @Transactional
    public ActividadResponse update(Long id, ActividadRequest request) {
        Actividad actividad = findActividad(id);
        validarIntervaloHorario(request);
        validarSinSolapamiento(request, id);
        actividad.setNombre(request.getNombre());
        actividad.setFecha(request.getFecha());
        actividad.setHoraInicio(request.getHoraInicio());
        actividad.setHoraFin(request.getHoraFin());
        actividad.setLugar(request.getLugar());
        actividad.setCreadorId(request.getCreadorId());
        return mapper.toResponse(repository.save(actividad));
    }

    private Actividad findActividad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad with id " + id + " was not found"));
    }

    private void validarIntervaloHorario(ActividadRequest request) {
        if (!request.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new HorarioActividadInvalidoException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private void validarSinSolapamiento(ActividadRequest request, Long idExcluido) {
        boolean existeSolapamiento = repository.existeSolapamiento(
                request.getLugar(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin(),
                idExcluido
        );

        if (existeSolapamiento) {
            throw new ActividadSolapadaException("Ya existe una actividad programada en ese lugar y horario");
        }
    }
}
