package pe.edu.upeu.saludablemente.nutricional.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.nutricional.dto.DetalleAntropometricoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalRequestDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResponseDto;
import pe.edu.upeu.saludablemente.nutricional.service.EvaluacionNutricionalService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluacionNutricionalController.class)
class EvaluacionNutricionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EvaluacionNutricionalService evaluacionNutricionalService;

    private EvaluacionNutricionalRequestDto evaluacionValida() {
        EvaluacionNutricionalRequestDto request = new EvaluacionNutricionalRequestDto();
        request.setIdPersona(1L);
        request.setFechaEvaluacion(LocalDate.now());
        request.setPeriodoSemestral("2026-I");

        DetalleAntropometricoDto detalle = new DetalleAntropometricoDto();
        detalle.setEstaturaCm(new BigDecimal("170.00"));
        detalle.setPesoKg(new BigDecimal("70.00"));
        request.setDetalleAntropometrico(detalle);
        return request;
    }

    @Test
    void registrarAntropometriaValidaRetorna201() throws Exception {
        when(evaluacionNutricionalService.registrarAntropometria(any(EvaluacionNutricionalRequestDto.class)))
                .thenReturn(EvaluacionNutricionalResponseDto.builder()
                        .idEvaluacion(1L)
                        .idPersona(1L)
                        .build());

        mockMvc.perform(post("/api/v1/evaluaciones-nutricionales/antropometria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evaluacionValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEvaluacion").value(1));
    }

    @Test
    void registrarSinDetalleAntropometricoRetorna400() throws Exception {
        EvaluacionNutricionalRequestDto request = evaluacionValida();
        request.setDetalleAntropometrico(null);

        mockMvc.perform(post("/api/v1/evaluaciones-nutricionales/antropometria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerEvaluacionInexistenteRetorna404() throws Exception {
        when(evaluacionNutricionalService.obtener(99L))
                .thenThrow(new ResourceNotFoundException("Evaluacion nutricional no encontrada: 99"));

        mockMvc.perform(get("/api/v1/evaluaciones-nutricionales/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}