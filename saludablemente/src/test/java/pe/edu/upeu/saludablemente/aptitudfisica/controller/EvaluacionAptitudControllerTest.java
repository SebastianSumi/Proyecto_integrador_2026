package pe.edu.upeu.saludablemente.aptitudfisica.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.DetallePruebaFisicaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudAgregadoDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudRequestDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResumenDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResponseDto;
import pe.edu.upeu.saludablemente.aptitudfisica.service.EvaluacionAptitudService;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluacionAptitudController.class)
class EvaluacionAptitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EvaluacionAptitudService evaluacionAptitudService;

    private EvaluacionAptitudRequestDto evaluacionValida() {
        EvaluacionAptitudRequestDto request = new EvaluacionAptitudRequestDto();
        request.setIdPersona(1L);
        request.setFechaRegistro(LocalDate.now());

        DetallePruebaFisicaDto detalle = new DetallePruebaFisicaDto();
        detalle.setIdPrueba(1L);
        detalle.setValorObtenido(new BigDecimal("12.50"));
        detalle.setPuntajeParcial(new BigDecimal("16.00"));
        request.setDetalles(List.of(detalle));
        return request;
    }

    @Test
    void registrarConDatosValidosRetorna201() throws Exception {
        when(evaluacionAptitudService.registrarEvaluacion(any(EvaluacionAptitudRequestDto.class)))
                .thenReturn(EvaluacionAptitudResponseDto.builder()
                        .idEvaluacionAptitud(1L)
                        .idPersona(1L)
                        .build());

        mockMvc.perform(post("/api/v1/evaluaciones-aptitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evaluacionValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEvaluacionAptitud").value(1));
    }

    @Test
    void registrarSinDetallesRetorna400() throws Exception {
        EvaluacionAptitudRequestDto request = evaluacionValida();
        request.setDetalles(List.of());

        mockMvc.perform(post("/api/v1/evaluaciones-aptitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerEvaluacionInexistenteRetorna404() throws Exception {
        when(evaluacionAptitudService.obtener(99L))
                .thenThrow(new ResourceNotFoundException("Evaluacion de aptitud fisica no encontrada: 99"));

        mockMvc.perform(get("/api/v1/evaluaciones-aptitud/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarResumenRetorna200() throws Exception {
        when(evaluacionAptitudService.listarResumen(any(), any(), any(), any()))
                .thenReturn(List.of(EvaluacionAptitudResumenDto.builder()
                        .idEvaluacionAptitud(1L)
                        .idPersona(1L)
                        .cantidadDetalles(2)
                        .build()));

        mockMvc.perform(get("/api/v1/evaluaciones-aptitud/resumen").param("sincronizado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidadDetalles").value(2));
    }

    @Test
    void agregadosRetorna200() throws Exception {
        when(evaluacionAptitudService.obtenerAgregados(1L))
                .thenReturn(EvaluacionAptitudAgregadoDto.builder()
                        .totalEvaluaciones(3L)
                        .sumaPuntajeGlobal(new BigDecimal("45.00"))
                        .build());

        mockMvc.perform(get("/api/v1/evaluaciones-aptitud/agregados").param("personaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvaluaciones").value(3));
    }
}