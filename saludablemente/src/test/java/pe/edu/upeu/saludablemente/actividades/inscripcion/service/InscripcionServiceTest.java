package pe.edu.upeu.saludablemente.actividades.inscripcion.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.ActividadRepository;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.mapper.InscripcionMapper;
import pe.edu.upeu.saludablemente.actividades.inscripcion.repository.InscripcionRepository;
import pe.edu.upeu.saludablemente.exception.ConflictException;

import java.time.LocalDateTime;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {
    @Mock ActividadRepository actividadRepository;
    @Mock InscripcionRepository inscripcionRepository;
    @Mock InscripcionMapper inscripcionMapper;
    @InjectMocks InscripcionService inscripcionService;

    @Test
    void rejectsEnrollmentWhenActivityIsNotScheduled() {
        Actividad activity = activity();
        activity.changeState(EstadoActividad.IN_PROGRESS);
        given(actividadRepository.findByIdForUpdate(1L)).willReturn(Optional.of(activity));

        assertThatThrownBy(() -> inscripcionService.enroll(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Enrollment can only change while the activity is scheduled");
    }

    @Test
    void rejectsAnAlreadyActiveEnrollment() {
        Inscripcion enrollment = new Inscripcion(1L, 10L, LocalDateTime.now());
        given(actividadRepository.findByIdForUpdate(1L)).willReturn(Optional.of(activity()));
        given(inscripcionRepository.findByActivityIdAndPersonId(1L, 10L)).willReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> inscripcionService.enroll(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already enrolled");
    }

    @Test
    void reactivatesACancelledEnrollmentInsteadOfCreatingHistoryDuplicates() {
        Inscripcion enrollment = new Inscripcion(1L, 10L, LocalDateTime.now().minusDays(1));
        enrollment.cancel(LocalDateTime.now().minusHours(1));
        given(actividadRepository.findByIdForUpdate(1L)).willReturn(Optional.of(activity()));
        given(inscripcionRepository.findByActivityIdAndPersonId(1L, 10L)).willReturn(Optional.of(enrollment));
        given(inscripcionRepository.save(enrollment)).willReturn(enrollment);

        inscripcionService.enroll(1L, 10L);

        assertThat(enrollment.getState()).isEqualTo(EstadoInscripcion.ENROLLED);
        assertThat(enrollment.getCancelledAt()).isNull();
        then(inscripcionRepository).should().save(enrollment);
    }

    @Test
    void validatesTheWholeBatchBeforeWritingAnything() {
        given(actividadRepository.findByIdForUpdate(1L)).willReturn(Optional.of(activity()));

        assertThatThrownBy(() -> inscripcionService.enrollBatch(1L, List.of(10L, 10L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicated");

        then(inscripcionRepository).shouldHaveNoInteractions();
    }

    @Test
    void aLaterActiveDuplicateFailsTheTransactionalBatch() throws NoSuchMethodException {
        Inscripcion active = new Inscripcion(1L, 20L, LocalDateTime.now());
        given(actividadRepository.findByIdForUpdate(1L)).willReturn(Optional.of(activity()));
        given(inscripcionRepository.findByActivityIdAndPersonId(1L, 10L)).willReturn(Optional.empty());
        given(inscripcionRepository.findByActivityIdAndPersonId(1L, 20L)).willReturn(Optional.of(active));

        assertThatThrownBy(() -> inscripcionService.enrollBatch(1L, List.of(10L, 20L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already enrolled");

        then(inscripcionRepository).should().save(any(Inscripcion.class));
        Method batchMethod = InscripcionService.class.getMethod("enrollBatch", Long.class, List.class);
        assertThat(batchMethod.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void cancellationIsLogicalAndIdempotent() {
        Inscripcion enrollment = new Inscripcion(1L, 10L, LocalDateTime.now());
        given(actividadRepository.findByIdForUpdate(1L)).willReturn(Optional.of(activity()));
        given(inscripcionRepository.findByActivityIdAndPersonId(1L, 10L)).willReturn(Optional.of(enrollment));

        inscripcionService.cancel(1L, 10L);
        LocalDateTime firstCancellation = enrollment.getCancelledAt();
        inscripcionService.cancel(1L, 10L);

        assertThat(enrollment.getState()).isEqualTo(EstadoInscripcion.CANCELLED);
        assertThat(enrollment.getCancelledAt()).isEqualTo(firstCancellation);
        then(inscripcionRepository).shouldHaveNoMoreInteractions();
    }

    private Actividad activity() {
        return new Actividad("Activity", null, LocalDateTime.of(2026, 9, 10, 9, 0),
                LocalDateTime.of(2026, 9, 10, 10, 0), "Hall", "HALL", 1L);
    }
}
