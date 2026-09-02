package pe.edu.upeu.saludablemente;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI saludablementeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Saludablemente API")
                        .version("v1")
                        .description("REST API for the Saludablemente platform."))
                .addServersItem(new Server().url("/"));
    }
}