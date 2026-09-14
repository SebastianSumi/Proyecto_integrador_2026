package pe.edu.upeu.saludablemente.metas.meta.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.observability.TransactionLog;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaCreateRequest;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaResponse;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaUpdateRequest;
import pe.edu.upeu.saludablemente.metas.meta.entity.EstadoMeta;
import pe.edu.upeu.saludablemente.metas.meta.entity.Meta;
import pe.edu.upeu.saludablemente.metas.meta.exception.EstadoMetaNoPermitidoException;
import pe.edu.upeu.saludablemente.metas.meta.exception.FechaLimiteMetaInvalidaException;
import pe.edu.upeu.saludablemente.metas.meta.mapper.MetaMapper;
import pe.edu.upeu.saludablemente.metas.meta.repository.MetaRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MetaServiceImpl implements MetaService {

    private final MetaRepository repository;
    private final MetaMapper mapper;

    @Override
    public List<MetaResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<MetaResponse> findByPersonaId(Long personaId) {
        return repository.findByPersonaId(personaId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public MetaResponse findById(Long id) {
        return mapper.toResponse(findMeta(id));
    }

    @Override
    @Transactional
    public MetaResponse create(MetaCreateRequest request) {
        validateDates(request.getFechaInicio(), request.getFechaLimite());
        Meta meta = mapper.toEntity(request);
        meta.setEstado(EstadoMeta.EN_CURSO);
        MetaResponse response = mapper.toResponse(repository.save(meta));
        TransactionLog.afterCommit(() -> log.info("goal.created id={} personaId={} estado={}", response.getId(),
                response.getPersonaId(), response.getEstado()));
        return response;
    }

    @Override
    @Transactional
    public MetaResponse update(Long id, MetaUpdateRequest request) {
        Meta meta = findMeta(id);
        requireInProgress(meta, "La meta solo puede modificarse mientras esté en curso");
        validateDates(request.getFechaInicio(), request.getFechaLimite());
        mapper.updateEntity(request, meta);
        MetaResponse response = mapper.toResponse(repository.save(meta));
        TransactionLog.afterCommit(() -> log.info("goal.updated id={} personaId={} estado={}", response.getId(),
                response.getPersonaId(), response.getEstado()));
        return response;
    }

    @Override
    @Transactional
    public MetaResponse complete(Long id) {
        Meta meta = findMeta(id);
        requireInProgress(meta, "La meta solo puede completarse mientras esté en curso");
        meta.setEstado(EstadoMeta.CUMPLIDA);
        MetaResponse response = mapper.toResponse(repository.save(meta));
        TransactionLog.afterCommit(() -> log.info("goal.completed id={} personaId={} estado={}", response.getId(),
                response.getPersonaId(), response.getEstado()));
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Meta meta = findMeta(id);
        requireInProgress(meta, "La meta solo puede eliminarse mientras esté en curso");
        repository.delete(meta);
        TransactionLog.afterCommit(() -> log.info("goal.deleted id={} personaId={} estado={}", meta.getId(),
                meta.getPersonaId(), meta.getEstado()));
    }

    private Meta findMeta(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta with id " + id + " was not found"));
    }

    private void validateDates(java.time.LocalDate fechaInicio, java.time.LocalDate fechaLimite) {
        if (fechaLimite.isBefore(fechaInicio)) {
            throw new FechaLimiteMetaInvalidaException(
                    "La fecha límite no puede ser anterior a la fecha de inicio"
            );
        }
    }

    private void requireInProgress(Meta meta, String message) {
        if (meta.getEstado() != EstadoMeta.EN_CURSO) {
            throw new EstadoMetaNoPermitidoException(message);
        }
    }
}
