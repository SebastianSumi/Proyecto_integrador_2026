package pe.edu.upeu.saludablemente.teams.team.controller;

import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamStateRequest;
import pe.edu.upeu.saludablemente.teams.team.service.TeamService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    void createsTeamWithLocationHeader() throws Exception {
        given(teamService.create(any(TeamRequest.class)))
                .willReturn(new TeamResponse(1L, "Nutrition", "Nutrition support team", true));

        mockMvc.perform(post("/api/v1/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Nutrition","descripcion":"Nutrition support team"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/equipos/1"))
                .andExpect(jsonPath("$.nombre").value("Nutrition"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void rejectsInvalidTeamRequest() throws Exception {
        mockMvc.perform(post("/api/v1/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations[0].field").value("name"));
    }

    @Test
    void returnsTeamById() throws Exception {
        given(teamService.findById(1L))
                .willReturn(new TeamResponse(1L, "Nutrition", null, true));

        mockMvc.perform(get("/api/v1/equipos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Nutrition"));
    }

    @Test
    void returnsNotFoundWhenTeamDoesNotExist() throws Exception {
        given(teamService.findById(999L)).willThrow(new ResourceNotFoundException("Team", 999L));

        mockMvc.perform(get("/api/v1/equipos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Team with id 999 was not found"));
    }
    @Test
    void listsTeams() throws Exception {
        given(teamService.findAll(null)).willReturn(List.of(
                new TeamResponse(1L, "Nutrition", null, true),
                new TeamResponse(2L, "Activities", null, false)
        ));

        mockMvc.perform(get("/api/v1/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Nutrition"))
                .andExpect(jsonPath("$[1].activo").value(false));
    }

    @Test
    void filtersTeamsByActiveState() throws Exception {
        given(teamService.findAll(true)).willReturn(List.of(
                new TeamResponse(1L, "Nutrition", null, true)
        ));

        mockMvc.perform(get("/api/v1/equipos?activo=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));

        then(teamService).should().findAll(true);
    }

    @Test
    void updatesTeam() throws Exception {
        given(teamService.update(eq(1L), any(TeamRequest.class)))
                .willReturn(new TeamResponse(1L, "Wellness", "Updated", true));

        mockMvc.perform(put("/api/v1/equipos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Wellness\",\"descripcion\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Wellness"));
    }

    @Test
    void updatesTeamStateExplicitly() throws Exception {
        given(teamService.updateState(eq(1L), any(TeamStateRequest.class)))
                .willReturn(new TeamResponse(1L, "Nutrition", null, false));

        mockMvc.perform(patch("/api/v1/equipos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        then(teamService).should().updateState(eq(1L), any(TeamStateRequest.class));
    }
}
