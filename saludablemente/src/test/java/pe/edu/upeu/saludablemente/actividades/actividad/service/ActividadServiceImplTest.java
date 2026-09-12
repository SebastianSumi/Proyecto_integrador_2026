package pe.edu.upeu.saludablemente.actividades.actividad.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.ActividadSolapadaException;
import pe.edu.upeu.saludablemente.actividades.actividad.mapper.ActividadMapper;
import pe.edu.upeu.saludablemente.actividades.actividad.repository.ActividadRepository;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActividadServiceImplTest {

    @Mock
    private ActividadRepository repository;

    @Mock
    private ActividadMapper mapper;

    @InjectMocks
    private ActividadServiceImpl service;

    @Test
    void findsAllActivities() {
        Actividad actividad = actividad(1L, "Caminata saludable");
        ActividadResponse response = response(1L, "Caminata saludable");
        when(repository.findAll()).thenReturn(List.of(actividad));
        when(mapper.toResponse(actividad)).thenReturn(response);

        assertEquals(List.of(response), service.findAll());
    }

    @Test
    void returnsActivityById() {
        Actividad actividad = actividad(1L, "Caminata saludable");
        ActividadResponse response = response(1L, "Caminata saludable");
        when(repository.findById(1L)).thenReturn(Optional.of(actividad));
        when(mapper.toResponse(actividad)).thenReturn(response);

        assertEquals(response, service.findById(1L));
    }

    @Test
    void failsWhenActivityDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));

        assertEquals("Actividad with id 99 was not found", exception.getMessage());
    }

    @Test
    void createsActivityWhenScheduleIsAvailable() {
        ActividadRequest request = request("Caminata saludable");
        Actividad mapped = actividad(null, "Caminata saludable");
        Actividad saved = actividad(1L, "Caminata saludable");
        ActividadResponse response = response(1L, "Caminata saludable");
        when(repository.existeSolapamiento("Parque central", request.getFecha(), request.getHoraInicio(), request.getHoraFin(), null))
                .thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, service.create(request));
    }

    @Test
    void rejectsCreationWhenScheduleOverlaps() {
        ActividadRequest request = request("Caminata saludable");
        when(repository.existeSolapamiento("Parque central", request.getFecha(), request.getHoraInicio(), request.getHoraFin(), null))
                .thenReturn(true);

        assertThrows(ActividadSolapadaException.class, () -> service.create(request));

        verify(mapper, never()).toEntity(request);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updatesActivityWhileExcludingItsOwnSchedule() {
        ActividadRequest request = request("Caminata actualizada");
        Actividad existing = actividad(1L, "Caminata anterior");
        Actividad saved = actividad(1L, "Caminata actualizada");
        ActividadResponse response = response(1L, "Caminata actualizada");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existeSolapamiento("Parque central", request.getFecha(), request.getHoraInicio(), request.getHoraFin(), 1L))
                .thenReturn(false);
        when(repository.save(existing)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        assertEquals(response, service.update(1L, request));
        assertEquals("Caminata actualizada", existing.getNombre());
    }

    @Test
    void rejectsUpdateWhenScheduleOverlapsAnotherActivity() {
        ActividadRequest request = request("Caminata actualizada");
        Actividad existing = actividad(1L, "Caminata anterior");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existeSolapamiento("Parque central", request.getFecha(), request.getHoraInicio(), request.getHoraFin(), 1L))
                .thenReturn(true);

        assertThrows(ActividadSolapadaException.class, () -> service.update(1L, request));

        verify(repository, never()).save(existing);
    }

    @Test
    void declaresReadOnlyDefaultAndWriteTransactions() throws Exception {
        Transactional classTransaction = ActividadServiceImpl.class.getAnnotation(Transactional.class);

        assertTrue(classTransaction.readOnly());
        assertFalse(ActividadServiceImpl.class.getMethod("create", ActividadRequest.class)
                .getAnnotation(Transactional.class).readOnly());
        assertFalse(ActividadServiceImpl.class.getMethod("update", Long.class, ActividadRequest.class)
                .getAnnotation(Transactional.class).readOnly());
    }

    private ActividadRequest request(String nombre) {
        ActividadRequest request = new ActividadRequest();
        request.setNombre(nombre);
        request.setFecha(LocalDate.of(2026, 9, 12));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(9, 0));
        request.setLugar("Parque central");
        request.setCreadorId(7L);
        return request;
    }

    private Actividad actividad(Long id, String nombre) {
        Actividad actividad = new Actividad();
        actividad.setId(id);
        actividad.setNombre(nombre);
        actividad.setFecha(LocalDate.of(2026, 9, 12));
        actividad.setHoraInicio(LocalTime.of(8, 0));
        actividad.setHoraFin(LocalTime.of(9, 0));
        actividad.setLugar("Parque central");
        actividad.setEstado(EstadoActividad.PROGRAMADA);
        actividad.setCreadorId(7L);
        return actividad;
    }

    private ActividadResponse response(Long id, String nombre) {
        return ActividadResponse.builder()
                .id(id)
                .nombre(nombre)
                .fecha(LocalDate.of(2026, 9, 12))
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(9, 0))
                .lugar("Parque central")
                .estado(EstadoActividad.PROGRAMADA)
                .creadorId(7L)
                .build();
    }
}
