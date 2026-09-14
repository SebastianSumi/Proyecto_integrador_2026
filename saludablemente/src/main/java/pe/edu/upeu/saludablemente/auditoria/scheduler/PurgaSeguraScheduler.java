package pe.edu.upeu.saludablemente.auditoria.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.config.AuditoriaRetentionConfig;
import pe.edu.upeu.saludablemente.auditoria.service.PurgaSeguraService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurgaSeguraScheduler {

    private static final String TABLA_BITACORA = "BITACORA_TRANSACCIONAL";

    private final PurgaSeguraService purgaService;
    private final AuditoriaRetentionConfig retentionConfig;

    @Scheduled(cron = "${auditoria.schedulers.purga.cron:0 0 4 1 1 *}")
    public void purgarParticionesVencidas() {
        if (!retentionConfig.isPurgaAutomatica()) {
            log.info("Purga automatica deshabilitada por configuracion. Se omite.");
            return;
        }

        log.info("Iniciando purga de particiones vencidas");

        try {
            var purgadas = purgaService.purgarParticionesVencidas(TABLA_BITACORA);
            log.info("Purga completada: {} particiones eliminadas", purgadas.size());

        } catch (Exception e) {
            log.error("Error en scheduler de purga: {}", e.getMessage(), e);
        }
    }
}
