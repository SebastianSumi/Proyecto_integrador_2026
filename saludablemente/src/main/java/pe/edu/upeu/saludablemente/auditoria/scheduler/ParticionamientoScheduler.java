package pe.edu.upeu.saludablemente.auditoria.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.service.ParticionamientoOracleService;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParticionamientoScheduler {

    private static final String TABLA_BITACORA = "BITACORA_TRANSACCIONAL";
    private static final String TABLA_LECTURA = "BITACORA_LECTURA";

    private final ParticionamientoOracleService particionamientoService;

    @Scheduled(cron = "${auditoria.schedulers.particionamiento.cron:0 0 2 1 * *}")
    public void crearParticionesSiguienteMes() {
        log.info("Iniciando creacion de particiones del mes siguiente");

        try {
            LocalDateTime mesSiguiente = LocalDateTime.now().plusMonths(1);
            LocalDateTime inicioMesSiguiente = mesSiguiente.withDayOfMonth(1).toLocalDate().atStartOfDay();

            particionamientoService.crearParticionMensual(TABLA_BITACORA, inicioMesSiguiente);
            particionamientoService.crearParticionMensual(TABLA_LECTURA, inicioMesSiguiente);

            log.info("Particiones del mes {} creadas exitosamente", inicioMesSiguiente.getMonth());

        } catch (Exception e) {
            log.error("Error en scheduler de particionamiento: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${auditoria.schedulers.sincronizacion-particiones.cron:0 0 3 * * SUN}")
    public void sincronizarMetadatos() {
        log.info("Sincronizando metadatos de particiones");

        try {
            particionamientoService.sincronizarMetadatosParticiones(TABLA_BITACORA);
            particionamientoService.sincronizarMetadatosParticiones(TABLA_LECTURA);
        } catch (Exception e) {
            log.error("Error al sincronizar particiones: {}", e.getMessage(), e);
        }
    }
}
