package pe.edu.upeu.saludablemente.actividades.actividad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.ActividadSolapadaException;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.HorarioActividadInvalidoException;
import pe.edu.upeu.saludablemente.actividades.actividad.service.ActividadService;
import pe.edu.upeu.saludablemente.exception.GlobalExceptionHandler;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ActividadControllerTest {

    @Mock
    private ActividadService service;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ActividadController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator()))
                .build();
    }

    @Test
    void listsActivities() throws Exception {
        when(service.findAll()).thenReturn(List.of(response(1L, "Taller de bienestar")));

        mockMvc.perform(get("/api/v1/actividades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Taller de bienestar"));

        verify(service).findAll();
    }

    @Test
    void returnsActivityById() throws Exception {
        when(service.findById(7L)).thenReturn(response(7L, "Taller de bienestar"));

        mockMvc.perform(get("/api/v1/actividades/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    void createsActivityFromValidRequest() throws Exception {
        when(service.create(any(ActividadRequest.class))).thenReturn(response(10L, "Taller de bienestar"));

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));

        verify(service).create(any(ActividadRequest.class));
    }

    @Test
    void updatesActivityFromValidRequest() throws Exception {
        when(service.update(any(Long.class), any(ActividadRequest.class)))
                .thenReturn(response(10L, "Taller actualizado"));

        mockMvc.perform(put("/api/v1/actividades/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Taller actualizado"));

        verify(service).update(any(Long.class), any(ActividadRequest.class));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        ActividadRequest invalidRequest = request();
        invalidRequest.setNombre(" ");

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(service, never()).create(any(ActividadRequest.class));
    }

    @Test
    void mapsInvalidScheduleToBadRequest() throws Exception {
        when(service.create(any(ActividadRequest.class)))
                .thenThrow(new HorarioActividadInvalidoException("La hora de inicio debe ser anterior a la hora de fin"));

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("La hora de inicio debe ser anterior a la hora de fin"));
    }

    @Test
    void mapsMissingActivityToNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Actividad with id 99 was not found"));

        mockMvc.perform(get("/api/v1/actividades/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void mapsScheduleConflictToConflict() throws Exception {
        when(service.create(any(ActividadRequest.class)))
                .thenThrow(new ActividadSolapadaException("Ya existe una actividad programada en ese lugar y horario"));

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya existe una actividad programada en ese lugar y horario"));
    }

    private ActividadRequest request() {
        ActividadRequest request = new ActividadRequest();
        request.setNombre("Taller de bienestar");
        request.setFecha(LocalDate.of(2026, 10, 15));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(11, 0));
        request.setLugar("Sala principal");
        request.setCreadorId(5L);
        return request;
    }

    private ActividadResponse response(Long id, String nombre) {
        return ActividadResponse.builder()
                .id(id)
                .nombre(nombre)
                .fecha(LocalDate.of(2026, 10, 15))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(11, 0))
                .lugar("Sala principal")
                .estado(EstadoActividad.PROGRAMADA)
                .creadorId(5L)
                .build();
    }
}
