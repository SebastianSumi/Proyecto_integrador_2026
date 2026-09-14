package pe.edu.upeu.saludablemente.exportacion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.config.ExportStorageConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class LimpiezaArchivosTemporalesScheduler {

    private final ExportStorageConfig storageConfig;

    @Scheduled(cron = "${exportacion.schedulers.limpieza-temporales.cron:0 0 * * * *}")
    public void limpiarArchivosTemporales() {
        log.debug("Iniciando tarea programada de limpieza de archivos temporales");

        Path rutaTemporal = storageConfig.getRutaTemporal();
        if (rutaTemporal == null || !Files.exists(rutaTemporal)) {
            return;
        }

        // Archivos temporales de mas de 2 horas
        Instant limite = Instant.now().minus(2, ChronoUnit.HOURS);

        try (Stream<Path> archivos = Files.list(rutaTemporal)) {
            long eliminados = archivos
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toInstant().isBefore(limite);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .peek(p -> {
                        try {
                            Files.deleteIfExists(p);
                            log.debug("Archivo temporal eliminado: {}", p.getFileName());
                        } catch (IOException e) {
                            log.warn("No se pudo eliminar archivo temporal {}: {}", p, e.getMessage());
                        }
                    })
                    .count();

            if (eliminados > 0) {
                log.info("Limpieza de temporales completada: {} archivos eliminados", eliminados);
            }
        } catch (IOException e) {
            log.error("Error al listar archivos temporales: {}", e.getMessage(), e);
        }
    }
}
