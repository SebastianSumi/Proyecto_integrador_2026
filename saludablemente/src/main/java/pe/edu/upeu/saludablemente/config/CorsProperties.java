package pe.edu.upeu.saludablemente.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "application.cors")
public record CorsProperties(List<String> allowedOrigins) {
}