package pe.edu.upeu.saludablemente.metas.meta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaCreateRequest;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaResponse;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaUpdateRequest;
import pe.edu.upeu.saludablemente.metas.meta.entity.EstadoMeta;
import pe.edu.upeu.saludablemente.metas.meta.entity.Meta;
import pe.edu.upeu.saludablemente.metas.meta.exception.EstadoMetaNoPermitidoException;
import pe.edu.upeu.saludablemente.metas.meta.exception.FechaLimiteMetaInvalidaException;
import pe.edu.upeu.saludablemente.metas.meta.mapper.MetaMapper;
import pe.edu.upeu.saludablemente.metas.meta.repository.MetaRepository;

@ExtendWith(MockitoExtension.class)
class MetaServiceImplTest {

    @Mock
    private MetaRepository repository;

    @Mock
    private MetaMapper mapper;

    @InjectMocks
    private MetaServiceImpl service;

    @Test
    void createsGoalWithInitialEnCursoState() {
        MetaCreateRequest request = createRequest();
        Meta mapped = meta(null, null);
        Meta saved = meta(1L, EstadoMeta.EN_CURSO);
        MetaResponse response = response(1L, EstadoMeta.EN_CURSO);
        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertSame(response, service.create(request));
        assertEquals(EstadoMeta.EN_CURSO, mapped.getEstado());
    }

    @Test
    void rejectsDeadlineBeforeStartOnCreate() {
        MetaCreateRequest request = createRequest();
        request.setFechaInicio(LocalDate.of(2026, 9, 14));
        request.setFechaLimite(LocalDate.of(2026, 9, 13));

        assertThrows(FechaLimiteMetaInvalidaException.class, () -> service.create(request));
        verify(mapper, never()).toEntity(request);
        verify(repository, never()).save(any());
    }

    @Test
    void findsGoalById() {
        Meta meta = meta(1L, EstadoMeta.EN_CURSO);
        MetaResponse response = response(1L, EstadoMeta.EN_CURSO);
        when(repository.findById(1L)).thenReturn(Optional.of(meta));
        when(mapper.toResponse(meta)).thenReturn(response);

        assertSame(response, service.findById(1L));
    }

    @Test
    void failsWhenGoalDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));

        assertEquals("Meta with id 99 was not found", exception.getMessage());
    }

    @Test
    void updatesOnlyEnCursoGoal() {
        MetaUpdateRequest request = updateRequest();
        Meta existing = meta(1L, EstadoMeta.EN_CURSO);
        MetaResponse response = response(1L, EstadoMeta.EN_CURSO);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        assertSame(response, service.update(1L, request));
        verify(mapper).updateEntity(request, existing);
        verify(repository).save(existing);
    }

    @Test
    void rejectsDeadlineBeforeStartOnUpdate() {
        MetaUpdateRequest request = updateRequest();
        request.setFechaInicio(LocalDate.of(2026, 9, 14));
        request.setFechaLimite(LocalDate.of(2026, 9, 13));
        Meta existing = meta(1L, EstadoMeta.EN_CURSO);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(FechaLimiteMetaInvalidaException.class, () -> service.update(1L, request));

        verify(mapper, never()).updateEntity(request, existing);
        verify(repository, never()).save(existing);
    }

    @Test
    void rejectsUpdateWhenGoalIsNotInProgress() {
        MetaUpdateRequest request = updateRequest();
        Meta existing = meta(1L, EstadoMeta.CUMPLIDA);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(EstadoMetaNoPermitidoException.class, () -> service.update(1L, request));
        verify(mapper, never()).updateEntity(request, existing);
        verify(repository, never()).save(existing);
    }

    @Test
    void completesInProgressGoalExplicitly() {
        Meta existing = meta(1L, EstadoMeta.EN_CURSO);
        MetaResponse response = response(1L, EstadoMeta.CUMPLIDA);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        assertSame(response, service.complete(1L));
        assertEquals(EstadoMeta.CUMPLIDA, existing.getEstado());
        verify(repository).save(existing);
    }

    @Test
    void rejectsCompletionWhenGoalIsNotInProgress() {
        Meta existing = meta(1L, EstadoMeta.VENCIDA);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(EstadoMetaNoPermitidoException.class, () -> service.complete(1L));
        verify(repository, never()).save(existing);
    }

    @Test
    void deletesInProgressGoal() {
        Meta existing = meta(1L, EstadoMeta.EN_CURSO);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }

    @Test
    void rejectsDeletionWhenGoalIsNotInProgress() {
        Meta existing = meta(1L, EstadoMeta.CUMPLIDA);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(EstadoMetaNoPermitidoException.class, () -> service.delete(1L));
        verify(repository, never()).delete(existing);
    }

    @Test
    void findsGoalsByPersonId() {
        Meta meta = meta(1L, EstadoMeta.EN_CURSO);
        MetaResponse response = response(1L, EstadoMeta.EN_CURSO);
        when(repository.findByPersonaId(10L)).thenReturn(List.of(meta));
        when(mapper.toResponse(meta)).thenReturn(response);

        assertEquals(List.of(response), service.findByPersonaId(10L));
    }

    @Test
    void declaresReadOnlyDefaultAndWriteTransactions() throws Exception {
        Transactional classTransaction = MetaServiceImpl.class.getAnnotation(Transactional.class);

        assertTrue(classTransaction.readOnly());
        assertFalse(MetaServiceImpl.class.getMethod("create", MetaCreateRequest.class)
                .getAnnotation(Transactional.class).readOnly());
        assertFalse(MetaServiceImpl.class.getMethod("update", Long.class, MetaUpdateRequest.class)
                .getAnnotation(Transactional.class).readOnly());
        assertFalse(MetaServiceImpl.class.getMethod("complete", Long.class)
                .getAnnotation(Transactional.class).readOnly());
        assertFalse(MetaServiceImpl.class.getMethod("delete", Long.class)
                .getAnnotation(Transactional.class).readOnly());
    }

    private MetaCreateRequest createRequest() {
        MetaCreateRequest request = new MetaCreateRequest();
        request.setPersonaId(10L);
        request.setTipoMeta("Peso");
        request.setValorObjetivo(new BigDecimal("65.00"));
        request.setValorActual(new BigDecimal("72.00"));
        request.setFechaInicio(LocalDate.of(2026, 9, 13));
        request.setFechaLimite(LocalDate.of(2026, 10, 13));
        return request;
    }

    private MetaUpdateRequest updateRequest() {
        MetaUpdateRequest request = new MetaUpdateRequest();
        request.setTipoMeta("Peso");
        request.setValorObjetivo(new BigDecimal("64.00"));
        request.setValorActual(new BigDecimal("70.00"));
        request.setFechaInicio(LocalDate.of(2026, 9, 13));
        request.setFechaLimite(LocalDate.of(2026, 10, 13));
        return request;
    }

    private Meta meta(Long id, EstadoMeta estado) {
        Meta meta = new Meta();
        meta.setId(id);
        meta.setPersonaId(10L);
        meta.setTipoMeta("Peso");
        meta.setValorObjetivo(new BigDecimal("65.00"));
        meta.setValorActual(new BigDecimal("72.00"));
        meta.setFechaInicio(LocalDate.of(2026, 9, 13));
        meta.setFechaLimite(LocalDate.of(2026, 10, 13));
        meta.setEstado(estado);
        return meta;
    }

    private MetaResponse response(Long id, EstadoMeta estado) {
        return MetaResponse.builder()
                .id(id)
                .personaId(10L)
                .tipoMeta("Peso")
                .valorObjetivo(new BigDecimal("65.00"))
                .valorActual(new BigDecimal("72.00"))
                .fechaInicio(LocalDate.of(2026, 9, 13))
                .fechaLimite(LocalDate.of(2026, 10, 13))
                .estado(estado)
                .build();
    }
}
