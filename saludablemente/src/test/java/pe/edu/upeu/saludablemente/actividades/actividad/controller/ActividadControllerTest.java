package pe.edu.upeu.saludablemente.actividades.actividad.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.RespuestaActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.SolicitudEstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.service.ActividadService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActividadController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActividadControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ActividadService actividadService;

    @Test
    void createsActivityUsingTheSpanishPublicContract() throws Exception {
        given(actividadService.create(any(SolicitudActividad.class))).willReturn(response(EstadoActividad.SCHEDULED));

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Workshop","descripcion":"Health session","fecha":"2026-09-10",
                                 "horaInicio":"09:00:00","horaFin":"10:00:00","lugar":"Main Hall","usuarioCreadorId":7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/actividades/1"))
                .andExpect(jsonPath("$.nombre").value("Workshop"))
                .andExpect(jsonPath("$.fecha").value("2026-09-10"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.usuarioCreadorId").value(7));
    }

    @Test
    void rejectsIncompleteActivityRequests() throws Exception {
        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void setsActivityStateExplicitly() throws Exception {
        given(actividadService.updateState(eq(1L), any(SolicitudEstadoActividad.class)))
                .willReturn(response(EstadoActividad.CANCELLED));

        mockMvc.perform(patch("/api/v1/actividades/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void filtersUsingTheSpanishPublicStateValue() throws Exception {
        given(actividadService.findAll(EstadoActividad.SCHEDULED)).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/actividades?estado=PROGRAMADA"))
                .andExpect(status().isOk());

        then(actividadService).should().findAll(EstadoActividad.SCHEDULED);
    }

    private RespuestaActividad response(EstadoActividad state) {
        return new RespuestaActividad(1L, "Workshop", "Health session", LocalDate.of(2026, 9, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "Main Hall", state, 7L);
    }
}
