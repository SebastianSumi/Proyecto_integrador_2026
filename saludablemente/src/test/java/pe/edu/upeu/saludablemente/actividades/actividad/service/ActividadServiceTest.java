package pe.edu.upeu.saludablemente.actividades.actividad.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.RespuestaActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudEstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.mapper.ActividadMapper;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.BloqueoLugarActividadRepository;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.ActividadRepository;
import pe.edu.upeu.saludablemente.exception.ConflictException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock ActividadRepository actividadRepository;
    @Mock BloqueoLugarActividadRepository bloqueoLugarRepository;
    @Mock ActividadMapper actividadMapper;
    @InjectMocks ActividadService actividadService;

    @Test
    void rejectsAnEndTimeThatIsNotAfterTheStartTime() {
        SolicitudActividad request = request(LocalTime.of(10, 0), LocalTime.of(10, 0));

        assertThatThrownBy(() -> actividadService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End time must be after start time");

        then(actividadRepository).shouldHaveNoInteractions();
        then(bloqueoLugarRepository).shouldHaveNoInteractions();
    }

    @Test
    void rejectsOverlappingActivitiesAtTheSameNormalizedPlace() {
        SolicitudActividad request = request(LocalTime.of(9, 0), LocalTime.of(10, 0));
        given(actividadRepository.existsOverlapping(
                "MAIN HALL",
                LocalDateTime.of(request.activityDate(), request.startTime()),
                LocalDateTime.of(request.activityDate(), request.endTime()),
                null
        )).willReturn(true);

        assertThatThrownBy(() -> actividadService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Another activity overlaps at this place");

        then(bloqueoLugarRepository).should().ensureExists("MAIN HALL");
        then(bloqueoLugarRepository).should().lockByPlaceKey("MAIN HALL");
        then(actividadRepository).should().existsOverlapping(any(), any(), any(), any());
        then(actividadRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void permitsContiguousActivitiesBecauseTheRepositoryUsesOpenIntervalOverlap() {
        SolicitudActividad request = request(LocalTime.of(10, 0), LocalTime.of(11, 0));
        Actividad activity = new Actividad("Workshop", null,
                LocalDateTime.of(request.activityDate(), request.startTime()),
                LocalDateTime.of(request.activityDate(), request.endTime()),
                "Main Hall", "MAIN HALL", 7L);
        RespuestaActividad expected = response(EstadoActividad.SCHEDULED);
        given(actividadRepository.existsOverlapping(any(), any(), any(), any())).willReturn(false);
        given(actividadMapper.toEntity(request, "Main Hall", "MAIN HALL")).willReturn(activity);
        given(actividadRepository.save(activity)).willReturn(activity);
        given(actividadMapper.toResponse(activity)).willReturn(expected);

        assertThat(actividadService.create(request)).isEqualTo(expected);
        then(actividadRepository).should().save(activity);
    }

    @Test
    void appliesValidLifecycleTransitionsAndTreatsTheSameStateAsIdempotent() {
        Actividad activity = activity();
        given(actividadRepository.findById(1L)).willReturn(Optional.of(activity));
        given(actividadMapper.toResponse(activity)).willAnswer(invocation -> response(activity.getState()));

        assertThat(actividadService.updateState(1L, new SolicitudEstadoActividad(EstadoActividad.IN_PROGRESS)).state())
                .isEqualTo(EstadoActividad.IN_PROGRESS);
        assertThat(actividadService.updateState(1L, new SolicitudEstadoActividad(EstadoActividad.IN_PROGRESS)).state())
                .isEqualTo(EstadoActividad.IN_PROGRESS);
        assertThat(actividadService.updateState(1L, new SolicitudEstadoActividad(EstadoActividad.FINISHED)).state())
                .isEqualTo(EstadoActividad.FINISHED);
    }

    @Test
    void rejectsTransitionsFromATerminalState() {
        Actividad activity = activity();
        activity.changeState(EstadoActividad.CANCELLED);
        given(actividadRepository.findById(1L)).willReturn(Optional.of(activity));

        assertThatThrownBy(() -> actividadService.updateState(1L,
                new SolicitudEstadoActividad(EstadoActividad.IN_PROGRESS)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot change activity state from CANCELLED to IN_PROGRESS");
    }

    private SolicitudActividad request(LocalTime start, LocalTime end) {
        return new SolicitudActividad("Workshop", null, LocalDate.of(2026, 9, 10), start, end,
                "  Main   Hall ", 7L);
    }

    private Actividad activity() {
        return new Actividad("Workshop", null,
                LocalDateTime.of(2026, 9, 10, 9, 0),
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Main Hall", "MAIN HALL", 7L);
    }

    private RespuestaActividad response(EstadoActividad state) {
        return new RespuestaActividad(1L, "Workshop", null, LocalDate.of(2026, 9, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "Main Hall", state, 7L);
    }
}
