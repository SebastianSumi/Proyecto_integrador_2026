package pe.edu.upeu.saludablemente.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void exposesSaludablementeApiMetadata() {
        OpenAPI openApi = new OpenApiConfig().saludablementeOpenApi();

        assertThat(openApi.getInfo())
                .extracting(info -> info.getTitle(), info -> info.getVersion(), info -> info.getDescription())
                .containsExactly(
                        "Saludablemente API",
                        "v1",
                        "Contrato REST del backend Saludablemente");
    }
}
