package pe.edu.upeu.saludablemente.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI saludablementeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Saludablemente API")
                .version("v1")
                .description("Contrato REST del backend Saludablemente"));
    }
}
