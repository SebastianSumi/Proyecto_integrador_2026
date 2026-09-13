package pe.edu.upeu.saludablemente.actividades.inscripcion.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceImplTest {

    @Mock
    private InscripcionRepository repository;

    @Mock
    private InscripcionMapper mapper;

    @Mock
    private ActividadService actividadService;

    @InjectMocks
    private InscripcionServiceImpl service;

    @Test
    void findsAllEnrollments() {
        Inscripcion inscripcion = enrollment(1L, EstadoInscripcion.INSCRITA, null);
        InscripcionResponse response = response(1L, EstadoInscripcion.INSCRITA, null);
        when(repository.findAll()).thenReturn(List.of(inscripcion));
        when(mapper.toResponse(inscripcion)).thenReturn(response);

        assertEquals(List.of(response), service.findAll());
    }

    @Test
    void returnsEnrollmentById() {
        Inscripcion inscripcion = enrollment(1L, EstadoInscripcion.INSCRITA, null);
        InscripcionResponse response = response(1L, EstadoInscripcion.INSCRITA, null);
        when(repository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(mapper.toResponse(inscripcion)).thenReturn(response);

        assertEquals(response, service.findById(1L));
    }

    @Test
    void failsWhenEnrollmentDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));

        assertEquals("Inscripcion with id 99 was not found", exception.getMessage());
    }

    @Test
    void registersEnrollmentWhenNoActiveEnrollmentExists() {
        InscripcionRequest request = request();
        Inscripcion mapped = enrollment(null, null, null);
        Inscripcion saved = enrollment(1L, EstadoInscripcion.INSCRITA, null);
        InscripcionResponse response = response(1L, EstadoInscripcion.INSCRITA, null);
        when(repository.existsByActividadIdAndPersonaIdAndEstado(10L, 20L, EstadoInscripcion.INSCRITA))
                .thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, service.register(request));
        assertEquals(EstadoInscripcion.INSCRITA, mapped.getEstado());
        assertTrue(mapped.getInscritaEn().isBefore(LocalDateTime.now().plusSeconds(1)));
        verify(actividadService).validateExists(10L);
    }

    @Test
    void rejectsRegistrationWhenActivityDoesNotExist() {
        InscripcionRequest request = request();
        doThrow(new ResourceNotFoundException("Actividad with id 10 was not found"))
                .when(actividadService).validateExists(10L);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.register(request));

        assertEquals("Actividad with id 10 was not found", exception.getMessage());
        verify(repository, never()).existsByActividadIdAndPersonaIdAndEstado(any(), any(), any());
        verify(mapper, never()).toEntity(request);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsRegistrationWhenActiveEnrollmentExists() {
        InscripcionRequest request = request();
        when(repository.existsByActividadIdAndPersonaIdAndEstado(10L, 20L, EstadoInscripcion.INSCRITA))
                .thenReturn(true);

        assertThrows(InscripcionVigenteException.class, () -> service.register(request));

        verify(mapper, never()).toEntity(request);
        verify(repository, never()).save(any());
    }

    @Test
    void cancelsActiveEnrollmentOnce() {
        Inscripcion inscripcion = enrollment(1L, EstadoInscripcion.INSCRITA, null);
        InscripcionResponse response = response(1L, EstadoInscripcion.CANCELADA, LocalDateTime.of(2026, 9, 12, 10, 0));
        when(repository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(repository.save(inscripcion)).thenReturn(inscripcion);
        when(mapper.toResponse(inscripcion)).thenReturn(response);

        assertEquals(response, service.cancel(1L));
        assertEquals(EstadoInscripcion.CANCELADA, inscripcion.getEstado());
        assertTrue(inscripcion.getCanceladaEn().isBefore(LocalDateTime.now().plusSeconds(1)));
        verify(repository).save(inscripcion);
    }

    @Test
    void returnsCanceledEnrollmentWithoutChangingItsTimestamp() {
        LocalDateTime canceledAt = LocalDateTime.of(2026, 9, 12, 10, 0);
        Inscripcion inscripcion = enrollment(1L, EstadoInscripcion.CANCELADA, canceledAt);
        InscripcionResponse response = response(1L, EstadoInscripcion.CANCELADA, canceledAt);
        when(repository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(mapper.toResponse(inscripcion)).thenReturn(response);

        assertSame(response, service.cancel(1L));
        assertEquals(canceledAt, inscripcion.getCanceladaEn());
        verify(repository, never()).save(any());
    }

    @Test
    void declaresReadOnlyDefaultAndWriteTransactions() throws Exception {
        Transactional classTransaction = InscripcionServiceImpl.class.getAnnotation(Transactional.class);

        assertTrue(classTransaction.readOnly());
        assertFalse(InscripcionServiceImpl.class.getMethod("register", InscripcionRequest.class)
                .getAnnotation(Transactional.class).readOnly());
        assertFalse(InscripcionServiceImpl.class.getMethod("cancel", Long.class)
                .getAnnotation(Transactional.class).readOnly());
    }

    private InscripcionRequest request() {
        InscripcionRequest request = new InscripcionRequest();
        request.setActividadId(10L);
        request.setPersonaId(20L);
        return request;
    }

    private Inscripcion enrollment(Long id, EstadoInscripcion estado, LocalDateTime canceledAt) {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(id);
        inscripcion.setActividadId(10L);
        inscripcion.setPersonaId(20L);
        inscripcion.setEstado(estado);
        inscripcion.setInscritaEn(LocalDateTime.of(2026, 9, 12, 9, 0));
        inscripcion.setCanceladaEn(canceledAt);
        return inscripcion;
    }

    private InscripcionResponse response(Long id, EstadoInscripcion estado, LocalDateTime canceledAt) {
        return InscripcionResponse.builder()
                .id(id)
                .actividadId(10L)
                .personaId(20L)
                .estado(estado)
                .inscritaEn(LocalDateTime.of(2026, 9, 12, 9, 0))
                .canceladaEn(canceledAt)
                .build();
    }
}
