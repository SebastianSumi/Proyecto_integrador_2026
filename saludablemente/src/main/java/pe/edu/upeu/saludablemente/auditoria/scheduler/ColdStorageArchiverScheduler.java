package pe.edu.upeu.saludablemente.auditoria.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.service.ArchivadoFrioService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ColdStorageArchiverScheduler {

    private static final String TABLA_BITACORA = "BITACORA_TRANSACCIONAL";

    private final ArchivadoFrioService archivadoFrioService;

    @Scheduled(cron = "${auditoria.schedulers.archivado-frio.cron:0 0 3 1 * *}")
    public void archivarParticionesVencidas() {
        log.info("Iniciando archivado de particiones vencidas");

        try {
            var manifiestos = archivadoFrioService.archivarParticionesVencidas(TABLA_BITACORA);
            log.info("Archivado completado: {} manifiestos generados", manifiestos.size());

        } catch (Exception e) {
            log.error("Error en scheduler de archivado frio: {}", e.getMessage(), e);
        }
    }
}
