package pe.edu.upeu.saludablemente.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CorsFilter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorsConfigTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("http://localhost:4200"));
        properties.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        properties.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        properties.setAllowCredentials(false);
        properties.setMaxAge(3600L);

        CorsConfig corsConfig = new CorsConfig(properties);
        mockMvc = MockMvcBuilders.standaloneSetup(new SampleApiController())
                .addFilters(new CorsFilter(corsConfig.corsConfigurationSource()))
                .build();
    }

    @Test
    void allowsTheConfiguredAngularDevelopmentOrigin() throws Exception {
        mockMvc.perform(get("/api/v1/sample")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void rejectsAnOriginOutsideTheConfiguredAllowlist() throws Exception {
        mockMvc.perform(get("/api/v1/sample")
                        .header(HttpHeaders.ORIGIN, "http://untrusted.example"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void answersPreflightWithOnlyTheConfiguredPolicy() throws Exception {
        mockMvc.perform(options("/api/v1/sample")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @RestController
    private static class SampleApiController {

        @GetMapping(value = "/api/v1/sample", produces = MediaType.TEXT_PLAIN_VALUE)
        String sample() {
            return "ok";
        }
    }
}
