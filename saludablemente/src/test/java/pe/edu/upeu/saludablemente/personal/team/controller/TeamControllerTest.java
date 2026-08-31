package pe.edu.upeu.saludablemente.personal.team.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.personal.team.service.TeamService;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    @Test
    void crear_conDatosValidos_respondeCreated() throws Exception {
        TeamRequest request = new TeamRequest();
        request.setNombre("Chirimoya");
        request.setDescripcion("Equipo de promocion de salud");

        when(teamService.crear(any())).thenReturn(
                TeamResponse.builder().id(1L).nombre("Chirimoya").descripcion("Equipo de promocion de salud").activo(true).build()
        );

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_conNombreVacio_respondeBadRequest() throws Exception {
        TeamRequest request = new TeamRequest();
        request.setNombre("");

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtener_conIdInexistente_respondeNotFound() throws Exception {
        when(teamService.obtener(999L)).thenThrow(new ResourceNotFoundException("Team no encontrado: 999"));

        mockMvc.perform(get("/api/v1/teams/999"))
                .andExpect(status().isNotFound());
    }
}
