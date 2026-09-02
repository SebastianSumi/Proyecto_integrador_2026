package pe.edu.upeu.saludablemente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pe.edu.upeu.saludablemente.config.CorsProperties;

@SpringBootApplication
@EnableConfigurationProperties(CorsProperties.class)
public class SaludablementeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaludablementeApplication.class, args);
    }
}