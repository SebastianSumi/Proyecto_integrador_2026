package pe.edu.upeu.saludablemente.metas.meta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import pe.edu.upeu.saludablemente.exception.GlobalExceptionHandler;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaCreateRequest;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaResponse;
import pe.edu.upeu.saludablemente.metas.meta.dto.MetaUpdateRequest;
import pe.edu.upeu.saludablemente.metas.meta.entity.EstadoMeta;
import pe.edu.upeu.saludablemente.metas.meta.service.MetaService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MetaControllerTest {

    @Mock
    private MetaService service;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MetaController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator()))
                .build();
    }

    @Test
    void listsAllGoals() throws Exception {
        when(service.findAll()).thenReturn(List.of(response(1L, 3L, EstadoMeta.EN_CURSO)));

        mockMvc.perform(get("/api/v1/metas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].personaId").value(3L));

        verify(service).findAll();
    }

    @Test
    void listsGoalsByParentPerson() throws Exception {
        when(service.findByPersonaId(3L)).thenReturn(List.of(response(1L, 3L, EstadoMeta.EN_CURSO)));

        mockMvc.perform(get("/api/v1/metas/persona/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personaId").value(3L));

        verify(service).findByPersonaId(3L);
    }

    @Test
    void returnsGoalById() throws Exception {
        when(service.findById(1L)).thenReturn(response(1L, 3L, EstadoMeta.EN_CURSO));

        mockMvc.perform(get("/api/v1/metas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(service).findById(1L);
    }

    @Test
    void createsGoalFromValidRequest() throws Exception {
        MetaCreateRequest request = createRequest();
        when(service.create(any(MetaCreateRequest.class))).thenReturn(response(1L, 3L, EstadoMeta.EN_CURSO));

        mockMvc.perform(post("/api/v1/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));

        verify(service).create(any(MetaCreateRequest.class));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        MetaCreateRequest request = createRequest();
        request.setPersonaId(null);

        mockMvc.perform(post("/api/v1/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(service, never()).create(any(MetaCreateRequest.class));
    }

    @Test
    void updatesGoalFromValidRequest() throws Exception {
        MetaUpdateRequest request = updateRequest();
        when(service.update(any(Long.class), any(MetaUpdateRequest.class)))
                .thenReturn(response(1L, 3L, EstadoMeta.EN_CURSO));

        mockMvc.perform(put("/api/v1/metas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(service).update(any(Long.class), any(MetaUpdateRequest.class));
    }

    @Test
    void completesGoalExplicitly() throws Exception {
        when(service.complete(1L)).thenReturn(response(1L, 3L, EstadoMeta.CUMPLIDA));

        mockMvc.perform(patch("/api/v1/metas/1/cumplimiento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CUMPLIDA"));

        verify(service).complete(1L);
    }

    @Test
    void deletesGoalInProgress() throws Exception {
        mockMvc.perform(delete("/api/v1/metas/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    private MetaCreateRequest createRequest() {
        MetaCreateRequest request = new MetaCreateRequest();
        request.setPersonaId(3L);
        request.setTipoMeta("Peso");
        request.setDescripcion("Reach target weight");
        request.setValorObjetivo(new BigDecimal("70.00"));
        request.setValorActual(new BigDecimal("75.00"));
        request.setFechaInicio(LocalDate.of(2026, 9, 1));
        request.setFechaLimite(LocalDate.of(2026, 12, 1));
        return request;
    }

    private MetaUpdateRequest updateRequest() {
        MetaUpdateRequest request = new MetaUpdateRequest();
        request.setTipoMeta("Peso");
        request.setDescripcion("Updated target weight");
        request.setValorObjetivo(new BigDecimal("68.00"));
        request.setValorActual(new BigDecimal("74.00"));
        request.setFechaInicio(LocalDate.of(2026, 9, 1));
        request.setFechaLimite(LocalDate.of(2026, 12, 1));
        return request;
    }

    private MetaResponse response(Long id, Long personaId, EstadoMeta estado) {
        return MetaResponse.builder()
                .id(id)
                .personaId(personaId)
                .tipoMeta("Peso")
                .descripcion("Reach target weight")
                .valorObjetivo(new BigDecimal("70.00"))
                .valorActual(new BigDecimal("75.00"))
                .fechaInicio(LocalDate.of(2026, 9, 1))
                .fechaLimite(LocalDate.of(2026, 12, 1))
                .estado(estado)
                .build();
    }
}
