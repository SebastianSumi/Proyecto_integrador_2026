package pe.edu.upeu.saludablemente.actividades.inscripcion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.time.LocalDateTime;
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
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionRequest;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionResponse;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.exception.InscripcionVigenteException;
import pe.edu.upeu.saludablemente.actividades.inscripcion.service.InscripcionService;
import pe.edu.upeu.saludablemente.exception.GlobalExceptionHandler;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InscripcionControllerTest {

    @Mock
    private InscripcionService service;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new InscripcionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator()))
                .build();
    }

    @Test
    void listsEnrollments() throws Exception {
        when(service.findAll()).thenReturn(List.of(response(1L, EstadoInscripcion.INSCRITA, null)));

        mockMvc.perform(get("/api/v1/inscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].estado").value("INSCRITA"));

        verify(service).findAll();
    }

    @Test
    void returnsEnrollmentById() throws Exception {
        when(service.findById(7L)).thenReturn(response(7L, EstadoInscripcion.INSCRITA, null));

        mockMvc.perform(get("/api/v1/inscripciones/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.actividadId").value(15L));
    }

    @Test
    void registersEnrollmentFromValidRequest() throws Exception {
        when(service.register(any(InscripcionRequest.class))).thenReturn(response(10L, EstadoInscripcion.INSCRITA, null));

        mockMvc.perform(post("/api/v1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.estado").value("INSCRITA"));

        verify(service).register(any(InscripcionRequest.class));
    }

    @Test
    void cancelsEnrollment() throws Exception {
        LocalDateTime cancelledAt = LocalDateTime.of(2026, 9, 12, 10, 30);
        when(service.cancel(10L)).thenReturn(response(10L, EstadoInscripcion.CANCELADA, cancelledAt));

        mockMvc.perform(patch("/api/v1/inscripciones/10/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.canceladaEn").value("2026-09-12T10:30:00"));

        verify(service).cancel(10L);
    }

    @Test
    void rejectsInvalidRegisterRequest() throws Exception {
        InscripcionRequest invalidRequest = request();
        invalidRequest.setPersonaId(null);

        mockMvc.perform(post("/api/v1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(service, never()).register(any(InscripcionRequest.class));
    }

    @Test
    void mapsMissingEnrollmentToNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Inscripcion with id 99 was not found"));

        mockMvc.perform(get("/api/v1/inscripciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void mapsDuplicateEnrollmentToConflict() throws Exception {
        when(service.register(any(InscripcionRequest.class)))
                .thenThrow(new InscripcionVigenteException("La persona ya cuenta con una inscripción vigente para esta actividad"));

        mockMvc.perform(post("/api/v1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("La persona ya cuenta con una inscripción vigente para esta actividad"));
    }

    @Test
    void returnsSameResponseWhenCancellationIsRepeated() throws Exception {
        LocalDateTime cancelledAt = LocalDateTime.of(2026, 9, 12, 10, 30);
        InscripcionResponse cancelled = response(10L, EstadoInscripcion.CANCELADA, cancelledAt);
        when(service.cancel(10L)).thenReturn(cancelled);

        mockMvc.perform(patch("/api/v1/inscripciones/10/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceladaEn").value("2026-09-12T10:30:00"));
        mockMvc.perform(patch("/api/v1/inscripciones/10/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceladaEn").value("2026-09-12T10:30:00"));

        verify(service, times(2)).cancel(10L);
    }

    private InscripcionRequest request() {
        InscripcionRequest request = new InscripcionRequest();
        request.setActividadId(15L);
        request.setPersonaId(22L);
        return request;
    }

    private InscripcionResponse response(Long id, EstadoInscripcion estado, LocalDateTime canceladaEn) {
        return InscripcionResponse.builder()
                .id(id)
                .actividadId(15L)
                .personaId(22L)
                .estado(estado)
                .inscritaEn(LocalDateTime.of(2026, 9, 12, 9, 0))
                .canceladaEn(canceladaEn)
                .build();
    }
}
