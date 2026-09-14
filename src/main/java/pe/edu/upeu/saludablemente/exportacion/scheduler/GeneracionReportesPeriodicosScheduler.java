package pe.edu.upeu.saludablemente.exportacion.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;
import pe.edu.upeu.saludablemente.exportacion.dto.IniciarExportacionRequestDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;
import pe.edu.upeu.saludablemente.exportacion.enums.ModoPrivacidad;
import pe.edu.upeu.saludablemente.exportacion.service.ExportacionBatchOrchestrator;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneracionReportesPeriodicosScheduler {

    private static final Long USUARIO_SISTEMA = 0L;
    private final ExportacionBatchOrchestrator orchestrator;

    @Scheduled(cron = "${exportacion.schedulers.generacion-periodica.cron:0 0 3 1 * *}")
    public void generarReportesCierrePeriodico() {
        LocalDateTime ahora = LocalDateTime.now();
        log.info("Iniciando generacion programada de reportes institucionales ({})", ahora);

        String periodoSemestral = calcularPeriodoSemestral(ahora);

        try {
            IniciarExportacionRequestDTO request = IniciarExportacionRequestDTO.builder()
                    .tituloLote("Consolidado Institucional " + periodoSemestral)
                    .formato(FormatoSalida.ZIP_EXPEDIENTES)
                    .modoPrivacidad(ModoPrivacidad.ANONIMIZADO_ESTADISTICO)
                    .filtros(FiltroPoblacionalParams.builder()
                            .periodos(List.of(periodoSemestral))
                            .incluirHistorialCompleto(true)
                            .build())
                    .build();

            orchestrator.iniciarExportacion(request, USUARIO_SISTEMA);
            log.info("Reporte institucional programado con exito para periodo {}", periodoSemestral);

        } catch (Exception e) {
            log.error("Error en generacion periodica automatica: {}", e.getMessage(), e);
        }
    }

    private String calcularPeriodoSemestral(LocalDateTime fecha) {
        int anio = fecha.getYear();
        int semestre = fecha.getMonthValue() <= 6 ? 1 : 2;
        return anio + "-" + semestre;
    }
}
