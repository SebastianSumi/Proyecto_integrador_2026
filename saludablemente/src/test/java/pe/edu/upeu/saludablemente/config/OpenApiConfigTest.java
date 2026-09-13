package pe.edu.upeu.saludablemente.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void exposesProjectMetadataForTheGeneratedApiDocumentation() {
        OpenAPI openAPI = config.saludablementeOpenAPI();

        assertNotNull(openAPI.getInfo());
        assertEquals("Saludablemente API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertEquals("REST API for Saludablemente wellness management modules.", openAPI.getInfo().getDescription());
    }
}
