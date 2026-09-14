package pe.edu.upeu.saludablemente.auditoria.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.entity.AlertaSeguridadEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoAlertaSeguridad;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;
import pe.edu.upeu.saludablemente.auditoria.repository.AlertaSeguridadRepository;
import pe.edu.upeu.saludablemente.auditoria.service.LedgerIntegrityVerifierService;
import pe.edu.upeu.saludablemente.auditoria.service.NotificacionSeguridadService;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerIntegrityScheduler {

    private final LedgerIntegrityVerifierService ledgerVerifier;
    private final AlertaSeguridadRepository alertaRepository;
    private final NotificacionSeguridadService notificacionService;

    @Scheduled(cron = "${auditoria.schedulers.integridad-ledger.cron:0 0 2 * * *}")
    public void verificarIntegridadNocturna() {
        log.info("Iniciando verificacion nocturna de integridad del ledger");

        try {
            var resultado = ledgerVerifier.verificarCadenaCompleta();

            if (resultado.isIntegra()) {
                log.info("Ledger integro. {} registros verificados", resultado.getTotalRegistros());
                return;
            }

            log.error("ALERTA CRITICA: Ledger comprometido. {} registros rotos",
                    resultado.getSecuenciasRotas().size());

            AlertaSeguridadEntity alerta = AlertaSeguridadEntity.builder()
                    .usuarioAfectado("SISTEMA")
                    .tipoAnomalia(TipoAnomalia.OUT_OF_HOURS_MODIFICATION)
                    .severidad(SeveridadAnomalia.CRITICAL)
                    .metadataContexto(String.format(
                            "{\"secuenciasRotas\":%s,\"totalRegistros\":%d}",
                            resultado.getSecuenciasRotas(), resultado.getTotalRegistros()))
                    .estadoAlerta(EstadoAlertaSeguridad.NO_RESUELTA)
                    .fechaDeteccion(LocalDateTime.now())
                    .build();

            alertaRepository.save(alerta);
            notificacionService.notificarAlertaCritica(alerta);

        } catch (Exception e) {
            log.error("Error en verificacion nocturna de integridad: {}", e.getMessage(), e);
        }
    }
}
