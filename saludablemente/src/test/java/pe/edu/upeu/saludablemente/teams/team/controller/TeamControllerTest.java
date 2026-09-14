package pe.edu.upeu.saludablemente.teams.team.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
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
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.service.TeamService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock
    private TeamService service;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TeamController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator()))
                .build();
    }

    @Test
    void listsTeamsWithOptionalActiveFilter() throws Exception {
        when(service.findAll(true)).thenReturn(List.of(response(1L, "Wellness", "Wellness team", true)));

        mockMvc.perform(get("/api/v1/teams").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Wellness"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(service).findAll(true);
    }

    @Test
    void returnsTeamById() throws Exception {
        when(service.findById(7L)).thenReturn(response(7L, "Wellness", "Wellness team", true));

        mockMvc.perform(get("/api/v1/teams/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.name").value("Wellness"));
    }

    @Test
    void createsTeamFromValidRequest() throws Exception {
        TeamRequest request = request("Wellness", "Wellness team");
        when(service.create(any(TeamRequest.class))).thenReturn(response(10L, "Wellness", "Wellness team", true));

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        TeamRequest request = request(" ", "x".repeat(201));

        mockMvc.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error de validación en los datos enviados"));

        verify(service, never()).create(any(TeamRequest.class));
    }

    @Test
    void updatesTeamFromValidRequest() throws Exception {
        TeamRequest request = request("Updated", "Updated team");
        when(service.update(any(Long.class), any(TeamRequest.class)))
                .thenReturn(response(10L, "Updated", "Updated team", true));

        mockMvc.perform(put("/api/v1/teams/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(service).update(any(Long.class), any(TeamRequest.class));
    }

    @Test
    void changesTeamState() throws Exception {
        when(service.changeState(10L, false)).thenReturn(response(10L, "Wellness", "Wellness team", false));

        mockMvc.perform(patch("/api/v1/teams/10/state").param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(service).changeState(10L, false);
    }

    @Test
    void mapsMissingTeamToNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Team with id 99 was not found"));

        mockMvc.perform(get("/api/v1/teams/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Team with id 99 was not found"));
    }

    private TeamRequest request(String name, String description) {
        TeamRequest request = new TeamRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    private TeamResponse response(Long id, String name, String description, boolean active) {
        return TeamResponse.builder()
                .id(id)
                .name(name)
                .description(description)
                .active(active)
                .build();
    }
}
