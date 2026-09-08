package pe.edu.upeu.saludablemente.actividades.inscripcion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.RespuestaInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.actividades.inscripcion.service.InscripcionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InscripcionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InscripcionControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean InscripcionService inscripcionService;

    @Test
    void enrollsUsingTheSpanishPublicContract() throws Exception {
        given(inscripcionService.enroll(1L, 10L)).willReturn(response(EstadoInscripcion.ENROLLED));
        mockMvc.perform(post("/api/v1/actividades/1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"personaId\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.actividadId").value(1))
                .andExpect(jsonPath("$.personaId").value(10))
                .andExpect(jsonPath("$.estado").value("INSCRITA"));
    }

    @Test
    void enrollsABoundedAtomicBatch() throws Exception {
        given(inscripcionService.enrollBatch(eq(1L), eq(List.of(10L, 11L))))
                .willReturn(List.of(response(EstadoInscripcion.ENROLLED)));
        mockMvc.perform(post("/api/v1/actividades/1/inscripciones/lote")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"personaIds\":[10,11]}"))
                .andExpect(status().isCreated());
    }

    @Test
    void cancelsLogicallyThroughDeleteSemantics() throws Exception {
        given(inscripcionService.cancel(1L, 10L)).willReturn(response(EstadoInscripcion.CANCELLED));
        mockMvc.perform(delete("/api/v1/actividades/1/inscripciones/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void listsHistoryOnlyWhenExplicitlyRequested() throws Exception {
        given(inscripcionService.findByActivity(1L, true)).willReturn(List.of());
        mockMvc.perform(get("/api/v1/actividades/1/inscripciones?incluirCanceladas=true"))
                .andExpect(status().isOk());
        then(inscripcionService).should().findByActivity(1L, true);
    }

    @Test
    void rejectsNonPositivePersonIds() throws Exception {
        mockMvc.perform(post("/api/v1/actividades/1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"personaId\":0}"))
                .andExpect(status().isBadRequest());
    }

    private RespuestaInscripcion response(EstadoInscripcion state) {
        return new RespuestaInscripcion(1L, 1L, 10L, state, LocalDateTime.of(2026, 9, 7, 12, 0),
                state == EstadoInscripcion.CANCELLED ? LocalDateTime.of(2026, 9, 7, 13, 0) : null);
    }
}
