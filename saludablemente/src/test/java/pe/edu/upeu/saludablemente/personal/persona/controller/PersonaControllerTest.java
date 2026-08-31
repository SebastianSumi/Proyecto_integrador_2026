package pe.edu.upeu.saludablemente.personal.persona.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaRequest;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaResponse;
import pe.edu.upeu.saludablemente.personal.persona.service.PersonaService;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResumen;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonaController.class)
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonaService personaService;

    @Test
    void listar_respondeOkConLasPersonasDelService() throws Exception {
        when(personaService.listar()).thenReturn(List.of(
                PersonaResponse.builder()
                        .id(1L)
                        .nombres("Maria")
                        .apellidoPaterno("Lopez")
                        .team(new TeamResumen(1L, "Chirimoya"))
                        .build()
        ));

        mockMvc.perform(get("/api/v1/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Maria"));
    }

    @Test
    void crear_conDatosValidos_respondeCreated() throws Exception {
        PersonaRequest request = new PersonaRequest();
        request.setNombres("Maria");
        request.setApellidoPaterno("Lopez");
        request.setCelular("987654321");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        request.setSexo("F");
        request.setTeamId(1L);

        when(personaService.crear(any())).thenReturn(
                PersonaResponse.builder().id(1L).nombres("Maria").apellidoPaterno("Lopez").build()
        );

        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_conNombreVacio_respondeBadRequestSinLlegarAlService() throws Exception {
        PersonaRequest request = new PersonaRequest();
        request.setNombres("");
        request.setApellidoPaterno("Lopez");
        request.setCelular("987654321");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        request.setSexo("F");
        request.setTeamId(1L);

        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conTeamIdNulo_respondeBadRequestSinLlegarAlService() throws Exception {
        PersonaRequest request = new PersonaRequest();
        request.setNombres("Maria");
        request.setApellidoPaterno("Lopez");
        request.setCelular("987654321");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        request.setSexo("F");

        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtener_conIdInexistente_respondeNotFound() throws Exception {
        when(personaService.obtener(999L)).thenThrow(new ResourceNotFoundException("Persona no encontrada: 999"));

        mockMvc.perform(get("/api/v1/personas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listar_conTeamIdExistente_respondeOkFiltrado() throws Exception {
        when(personaService.listarPorTeam(1L)).thenReturn(List.of(
                PersonaResponse.builder().id(10L).nombres("Maria").apellidoPaterno("Lopez").build()
        ));

        mockMvc.perform(get("/api/v1/personas").param("teamId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void listar_conTeamIdInexistente_respondeNotFound() throws Exception {
        when(personaService.listarPorTeam(999L)).thenThrow(new ResourceNotFoundException("Team no encontrado: 999"));

        mockMvc.perform(get("/api/v1/personas").param("teamId", "999"))
                .andExpect(status().isNotFound());
    }
}
