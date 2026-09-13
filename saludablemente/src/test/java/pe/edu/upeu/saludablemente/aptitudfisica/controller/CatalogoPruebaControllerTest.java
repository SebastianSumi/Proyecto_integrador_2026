package pe.edu.upeu.saludablemente.aptitudfisica.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.CatalogoPruebaDto;
import pe.edu.upeu.saludablemente.aptitudfisica.service.CatalogoPruebaService;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogoPruebaController.class)
class CatalogoPruebaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CatalogoPruebaService catalogoPruebaService;

    private CatalogoPruebaDto pruebaValida() {
        return CatalogoPruebaDto.builder()
                .nombrePrueba("Carrera 100m")
                .unidadMedida("seg")
                .descripcion("Carrera de velocidad")
                .activo(true)
                .build();
    }

    @Test
    void crearConDatosValidosRetorna201() throws Exception {
        when(catalogoPruebaService.crear(any(CatalogoPruebaDto.class)))
                .thenReturn(CatalogoPruebaDto.builder().idPrueba(1L).nombrePrueba("Carrera 100m").build());

        mockMvc.perform(post("/api/v1/catalogo-pruebas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pruebaValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPrueba").value(1));
    }

    @Test
    void crearConDatosInvalidosRetorna400() throws Exception {
        CatalogoPruebaDto request = pruebaValida();
        request.setNombrePrueba("");

        mockMvc.perform(post("/api/v1/catalogo-pruebas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPruebaInexistenteRetorna404() throws Exception {
        when(catalogoPruebaService.obtener(99L))
                .thenThrow(new ResourceNotFoundException("Prueba del catalogo no encontrada: 99"));

        mockMvc.perform(get("/api/v1/catalogo-pruebas/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}