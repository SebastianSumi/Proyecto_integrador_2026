package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.service.MaquinariaEstadosService;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorVencimientoSlaScheduler {

    private final AlertaClinicaRepository alertaRepository;
    private final MaquinariaEstadosService maquinariaEstadosService;

    /**
     * Ejecuta cada hora para verificar alertas vencidas
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora
    @Transactional
    public void verificarAlertasVencidas() {
        log.info("⏰ Ejecutando monitoreo de SLA...");

        LocalDateTime ahora = LocalDateTime.now();
        List<EstadoAlerta> estadosPendientes = List.of(EstadoAlerta.PENDIENTE, EstadoAlerta.EN_REVISION);

        // Buscar alertas vencidas
        List<AlertaClinicaEntity> alertasVencidas = alertaRepository.findAlertasVencidas(
                estadosPendientes, ahora);

        if (alertasVencidas.isEmpty()) {
            log.debug("✅ No hay alertas vencidas");
            return;
        }

        log.info("⚠️ Encontradas {} alertas vencidas", alertasVencidas.size());

        for (AlertaClinicaEntity alerta : alertasVencidas) {
            try {
                // Usar MaquinariaEstadosService para la transición
                maquinariaEstadosService.transitarEstado(alerta, EstadoAlerta.VENCIDO);
                alertaRepository.save(alerta);

                log.warn("⏰ Alerta {} marcada como VENCIDA (estado anterior: {}, SLA vencido: {})",
                        alerta.getIdAlerta(),
                        alerta.getEstado(),
                        alerta.getFechaVencimientoSla());

                // TODO: Publicar evento de escalamiento (se implementará en interoperabilidad)
                // publicarEventoEscalamiento(alerta);

            } catch (Exception e) {
                log.error("Error al marcar alerta {} como vencida: {}",
                        alerta.getIdAlerta(), e.getMessage(), e);
            }
        }

        log.info("✅ Monitoreo de SLA completado. {} alertas marcadas como vencidas",
                alertasVencidas.size());
    }

    /**
     * Ejecuta cada 5 minutos para verificar alertas próximas a vencer (30 min)
     */
    @Scheduled(cron = "0 */5 * * * *") // Cada 5 minutos
    @Transactional
    public void verificarAlertasProximasAVencer() {
        log.debug("⏰ Verificando alertas próximas a vencer...");

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limiteVencimiento = ahora.plusMinutes(30); // Próximas 30 minutos

        List<EstadoAlerta> estadosPendientes = List.of(EstadoAlerta.PENDIENTE, EstadoAlerta.EN_REVISION);

        // Buscar alertas que vencen en los próximos 30 minutos
        List<AlertaClinicaEntity> alertasProximas = alertaRepository.findAlertasVencidas(
                estadosPendientes, limiteVencimiento);

        // Filtrar las que realmente vencen en los próximos 30 minutos (no las ya vencidas)
        alertasProximas = alertasProximas.stream()
                .filter(a -> a.getFechaVencimientoSla() != null &&
                        a.getFechaVencimientoSla().isAfter(ahora) &&
                        a.getFechaVencimientoSla().isBefore(limiteVencimiento))
                .toList();

        if (!alertasProximas.isEmpty()) {
            log.warn("⚠️ {} alertas próximas a vencer en 30 minutos", alertasProximas.size());

            for (AlertaClinicaEntity alerta : alertasProximas) {
                long minutosRestantes = java.time.Duration.between(ahora, alerta.getFechaVencimientoSla()).toMinutes();
                log.warn("⏰ Alerta {} vence en {} minutos - Severidad: {}",
                        alerta.getIdAlerta(),
                        minutosRestantes,
                        alerta.getNivelSeveridad());

                // TODO: Publicar evento de advertencia (se implementará en interoperabilidad)
                // publicarEventoAdvertenciaSla(alerta, minutosRestantes);
            }
        }
    }

    /**
     * Ejecuta diariamente para limpiar alertas vencidas antiguas
     */
    @Scheduled(cron = "0 0 2 * * *") // Cada día a las 2 AM
    @Transactional
    public void limpiarAlertasVencidasAntiguas() {
        log.info("🧹 Limpiando alertas vencidas antiguas...");

        LocalDateTime limiteAntiguo = LocalDateTime.now().minusDays(30); // 30 días

        List<AlertaClinicaEntity> alertasAntiguas = alertaRepository.findAlertasVencidas(
                List.of(EstadoAlerta.VENCIDO), limiteAntiguo);

        if (!alertasAntiguas.isEmpty()) {
            log.info("🗑️ Eliminando {} alertas vencidas antiguas (más de 30 días)",
                    alertasAntiguas.size());

            // En producción, se podría archivar en lugar de eliminar
            alertaRepository.deleteAll(alertasAntiguas);
        }
    }
}