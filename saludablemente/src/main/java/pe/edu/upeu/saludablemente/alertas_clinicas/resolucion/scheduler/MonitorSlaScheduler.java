package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.MaquinariaEstadosService;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorSlaScheduler {

    private final AlertaClinicaRepository alertaRepository;
    private final MaquinariaEstadosService maquinariaEstadosService;

    @Scheduled(cron = "${alertas.sla.monitor.cron:0 0 * * * *}")
    @Transactional
    public void monitorearVencimientoSla() {
        LocalDateTime ahora = LocalDateTime.now();
        log.info("[Monitor SLA] Verificando vencimiento de alertas clínicas activas (> 72h)...");

        List<AlertaClinicaEntity> alertasVencidas = alertaRepository.findAlertasVencidas(
                List.of(EstadoAlerta.PENDIENTE, EstadoAlerta.EN_REVISION),
                ahora
        );

        if (alertasVencidas.isEmpty()) {
            log.info("[Monitor SLA] No hay alertas con SLA expirado en este ciclo.");
            return;
        }

        int actualizadas = 0;
        for (AlertaClinicaEntity alerta : alertasVencidas) {
            try {
                maquinariaEstadosService.transitarEstado(alerta, EstadoAlerta.VENCIDO);
                alertaRepository.save(alerta);
                actualizadas++;
                log.warn("[Monitor SLA] Alerta {} marcada como VENCIDO (Vencimiento programado: {})",
                        alerta.getIdAlerta(), alerta.getFechaVencimientoSla());
            } catch (Exception e) {
                log.error("[Monitor SLA] Error al transicionar alerta {} a VENCIDO: {}",
                        alerta.getIdAlerta(), e.getMessage());
            }
        }

        log.info("[Monitor SLA] Ciclo completado: {} alertas transicionadas a VENCIDO.", actualizadas);
    }
}
