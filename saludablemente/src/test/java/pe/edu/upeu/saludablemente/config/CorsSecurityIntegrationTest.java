package pe.edu.upeu.saludablemente.config;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class CorsSecurityIntegrationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void securityFilterChainAllowsConfiguredOriginForApiPath() throws Exception {
        mockMvc.perform(get("/api/nonexistent").header(ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isNotFound())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void securityFilterChainRejectsUnconfiguredOriginForApiPath() throws Exception {
        mockMvc.perform(get("/api/nonexistent").header(ORIGIN, "http://untrusted.example"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void securityFilterChainAnswersAllowedPreflightForApiPath() throws Exception {
        mockMvc.perform(options("/api/nonexistent")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS,
                        org.hamcrest.Matchers.containsString("POST")));
    }
}
