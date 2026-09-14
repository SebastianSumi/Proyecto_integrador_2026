package pe.edu.upeu.saludablemente.perfil_reporte.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.InputStream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ThymeleafPdfEngineConfig {

    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void validarCargaFuentesTrueType() {
        try (InputStream fontStream = resourceLoader
                .getResource("classpath:fonts/Roboto-Regular.ttf")
                .getInputStream()) {
            if (fontStream == null) {
                throw new IllegalStateException("Fuente Roboto-Regular.ttf no encontrada en classpath:fonts/");
            }
            log.info("Fuentes TrueType verificadas para el motor PDF");
        } catch (Exception e) {
            log.warn("No se pudieron validar las fuentes TrueType para PDF: {}", e.getMessage());
        }
    }

    @Bean(name = "pdfTemplateEngine")
    public TemplateEngine pdfTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/pdf/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
