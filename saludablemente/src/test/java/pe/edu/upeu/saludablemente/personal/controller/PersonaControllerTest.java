package pe.edu.upeu.saludablemente.personal.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.dto.PersonaRequestDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;
import pe.edu.upeu.saludablemente.personal.service.PersonaService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonaController.class)
@ActiveProfiles("test")
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonaService personaService;

    private PersonaRequestDto personaValida() {
        PersonaRequestDto request = new PersonaRequestDto();
        request.setIdTeam(1L);
        request.setNombres("Juan");
        request.setApellidoPaterno("Perez");
        request.setApellidoMaterno("Lopez");
        request.setCelular("987654321");
        request.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        request.setSexo("M");
        request.setTallaPolo("M");
        return request;
    }

    @Test
    void crearConDatosValidosRetorna201() throws Exception {
        when(personaService.createPersona(any(PersonaRequestDto.class)))
                .thenReturn(PersonaResponseDto.builder().idPersona(1L).nombres("Juan").build());

        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPersona").value(1));
    }

    @Test
    void crearConDatosInvalidosRetorna400() throws Exception {
        PersonaRequestDto request = personaValida();
        request.setNombres("");

        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPersonaInexistenteRetorna404() throws Exception {
        when(personaService.obtener(99L))
                .thenThrow(new ResourceNotFoundException("Persona no encontrada: 99"));

        mockMvc.perform(get("/api/v1/personas/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarConFiltrosDinamicosRetorna200() throws Exception {
        when(personaService.listar(any(), any(), any()))
                .thenReturn(List.of(PersonaResponseDto.builder().idPersona(1L).nombres("Juan").build()));

        mockMvc.perform(get("/api/v1/personas").param("nombres", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPersona").value(1));
    }
}