package pe.edu.upeu.saludablemente.actividades.actividad.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadDetalleResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadAgregado;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResumen;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.CriterioConsultaActividadInvalidoException;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.ActividadSolapadaException;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.HorarioActividadInvalidoException;
import pe.edu.upeu.saludablemente.actividades.actividad.mapper.ActividadMapper;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.ActividadRepository;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.AgendaActividadLock;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.observability.TransactionLog;
import pe.edu.upeu.saludablemente.personal.service.PersonaService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ActividadServiceImpl implements ActividadService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "nombre", "fecha", "horaInicio", "horaFin", "lugar", "estado"
    );

    private final ActividadRepository repository;
    private final ActividadMapper mapper;
    private final AgendaActividadLock agendaActividadLock;
    private final PersonaService personaService;

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
    public ActividadDetalleResponse findDetalleById(Long id) {
        Actividad actividad = repository.findDetalleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad with id " + id + " was not found"));
        return mapper.toDetalleResponse(actividad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadResumen> search(EstadoActividad estado, LocalDate desde, LocalDate hasta, Sort sort) {
        validarRangoFechas(desde, hasta);
        return repository.buscar(estado, desde, hasta, normalizarSort(sort));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadAgregado> getAggregates(LocalDate desde, LocalDate hasta) {
        validarRangoFechas(desde, hasta);
        return repository.agregados(desde, hasta);
    }

    @Override
    public void validateExists(Long id) {
        findActividad(id);
    }

    @Override
    @Transactional
    public ActividadResponse create(ActividadRequest request) {
        validarIntervaloHorario(request);
        personaService.validateActivePersona(request.getCreadorId());
        bloquearAgenda(request.getLugar(), request.getFecha());
        validarSinSolapamiento(request, null);
        Actividad actividad = mapper.toEntity(request);
        ActividadResponse response = mapper.toResponse(repository.save(actividad));
        TransactionLog.afterCommit(() -> log.info("activity.created id={} estado={}", response.getId(), response.getEstado()));
        return response;
    }

    @Override
    @Transactional
    public ActividadResponse update(Long id, ActividadRequest request) {
        Actividad actividad = findActividad(id);
        validarIntervaloHorario(request);
        personaService.validateActivePersona(request.getCreadorId());
        bloquearAgendasAfectadas(actividad, request);
        validarSinSolapamiento(request, id);
        actividad.setNombre(request.getNombre());
        actividad.setFecha(request.getFecha());
        actividad.setHoraInicio(request.getHoraInicio());
        actividad.setHoraFin(request.getHoraFin());
        actividad.setLugar(request.getLugar());
        actividad.setCreadorId(request.getCreadorId());
        ActividadResponse response = mapper.toResponse(repository.save(actividad));
        TransactionLog.afterCommit(() -> log.info("activity.updated id={} estado={}", response.getId(), response.getEstado()));
        return response;
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

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new CriterioConsultaActividadInvalidoException(
                    "La fecha desde no puede ser posterior a la fecha hasta"
            );
        }
    }

    private Sort normalizarSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.unsorted();
        }

        List<Sort.Order> ordenes = sort.stream()
                .peek(orden -> validarPropiedadOrdenamiento(orden.getProperty()))
                .toList();
        return Sort.by(ordenes);
    }

    private void validarPropiedadOrdenamiento(String propiedad) {
        if (!ALLOWED_SORT_PROPERTIES.contains(propiedad)) {
            throw new CriterioConsultaActividadInvalidoException(
                    "No se permite ordenar actividades por: " + propiedad
            );
        }
    }

    private void bloquearAgenda(String lugar, LocalDate fecha) {
        agendaActividadLock.lock(lugar, fecha);
    }

    private void bloquearAgendasAfectadas(Actividad actividad, ActividadRequest request) {
        List<AgendaKey> agendas = List.of(
                        new AgendaKey(actividad.getLugar(), actividad.getFecha()),
                        new AgendaKey(request.getLugar(), request.getFecha())
                ).stream()
                .distinct()
                .sorted(Comparator.comparing(AgendaKey::lugar).thenComparing(AgendaKey::fecha))
                .toList();

        agendas.forEach(agenda -> bloquearAgenda(agenda.lugar(), agenda.fecha()));
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

    private record AgendaKey(String lugar, LocalDate fecha) {
    }
}
