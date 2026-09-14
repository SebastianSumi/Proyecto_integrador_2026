package pe.edu.upeu.saludablemente.exportacion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exportacion.config.ExportStorageConfig;
import pe.edu.upeu.saludablemente.exportacion.entity.TareaExportacionEntity;
import pe.edu.upeu.saludablemente.exportacion.repository.TareaExportacionRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificacionDescargasExpiradasScheduler {

    private final TareaExportacionRepository tareaRepository;
    private final ExportStorageConfig storageConfig;

    @Scheduled(cron = "${exportacion.schedulers.verificacion-descargas-expiradas.cron:0 */15 * * * *}")
    @Transactional
    public void verificarDescargasExpiradas() {
        // Tareas completadas hace mas de 24 horas
        LocalDateTime fechaLimite = LocalDateTime.now().minusHours(24);
        List<TareaExportacionEntity> antiguas = tareaRepository.findCompletadasAntiguas(fechaLimite);

        if (antiguas.isEmpty()) {
            return;
        }

        log.info("Verificando paquetes antiguos para purga: {} tareas", antiguas.size());

        for (TareaExportacionEntity tarea : antiguas) {
            try {
                if (tarea.getRutaAlmacenamiento() != null) {
                    Path archivo = storageConfig.getRutaFinales().resolve(tarea.getRutaAlmacenamiento()).normalize();
                    if (Files.deleteIfExists(archivo)) {
                        log.debug("Archivo purgado: {}", archivo);
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo purgar paquete de tarea {}: {}", tarea.getIdTarea(), e.getMessage());
            }
        }
    }
}
