package pe.edu.upeu.saludablemente.exportacion.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Getter
@Configuration
public class ExportStorageConfig {

    @Value("${exportacion.storage.ruta-base:./exportacion-storage}")
    private String rutaBaseConfig;

    private Path rutaBaseAbsoluta;
    private Path rutaTemporal;
    private Path rutaFinales;

    @PostConstruct
    public void inicializar() {
        try {
            this.rutaBaseAbsoluta = resolverRutaAbsoluta(rutaBaseConfig);
            this.rutaTemporal = rutaBaseAbsoluta.resolve("temporal");
            this.rutaFinales = rutaBaseAbsoluta.resolve("finales");

            Files.createDirectories(rutaTemporal);
            Files.createDirectories(rutaFinales);

            log.info("Storage de exportacion inicializado en: {}", rutaBaseAbsoluta);
            log.info("  - temporal: {}", rutaTemporal);
            log.info("  - finales:  {}", rutaFinales);

        } catch (IOException e) {
            log.error("No se pudo inicializar el storage de exportacion: {}", e.getMessage(), e);
            throw new IllegalStateException("No se pudo crear el storage de exportacion", e);
        }
    }

    private Path resolverRutaAbsoluta(String rutaConfigurada) {
        Path path = Paths.get(rutaConfigurada);
        if (!path.isAbsolute()) {
            path = Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }
}
