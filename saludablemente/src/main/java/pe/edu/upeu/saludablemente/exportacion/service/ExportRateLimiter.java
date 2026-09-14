package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.LimiteExportacionExcedidoException;
import pe.edu.upeu.saludablemente.exportacion.dto.CuotaUsuarioDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;
import pe.edu.upeu.saludablemente.exportacion.repository.TareaExportacionRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportRateLimiter {

    private final TareaExportacionRepository tareaRepository;

    @Value("${exportacion.rate-limit.exportaciones-por-hora:10}")
    private int limiteExportacionesPorHora;

    @Value("${exportacion.rate-limit.registros-diarios:500000}")
    private long limiteRegistrosDiarios;

    @Value("${exportacion.rate-limit.registros-por-exportacion:100000}")
    private long limiteRegistrosPorExportacion;

    @Transactional(readOnly = true)
    public void verificarCuota(Long idUsuario, FiltroPoblacionalParams filtros) {
        if (idUsuario == null) {
            return;
        }

        CuotaUsuarioDTO cuota = consultarCuota(idUsuario);

        if (!cuota.isCuotaDisponible()) {
            log.error("Cuota excedida para usuario {}: {}", idUsuario, cuota.getMotivoBloqueo());
            throw new LimiteExportacionExcedidoException(cuota.getMotivoBloqueo());
        }

        log.debug("Cuota verificada para usuario {}: {} exportaciones/h, {} registros/24h",
                idUsuario, cuota.getExportacionesUltimaHora(), cuota.getRegistrosUltimas24Horas());
    }

    @Transactional(readOnly = true)
    public CuotaUsuarioDTO consultarCuota(Long idUsuario) {
        LocalDateTime haceUnaHora = LocalDateTime.now().minusHours(1);
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);

        int exportacionesUltimaHora = (int) tareaRepository
                .contarPorUsuarioDesde(idUsuario, haceUnaHora);

        long registrosUltimas24Horas = tareaRepository
                .sumarRegistrosExportadosPorUsuarioDesde(idUsuario, hace24Horas);

        String motivo = null;
        boolean disponible = true;

        if (exportacionesUltimaHora >= limiteExportacionesPorHora) {
            disponible = false;
            motivo = String.format(
                    "Ha excedido el limite de %d exportaciones por hora (actual: %d)",
                    limiteExportacionesPorHora, exportacionesUltimaHora);
        } else if (registrosUltimas24Horas >= limiteRegistrosDiarios) {
            disponible = false;
            motivo = String.format(
                    "Ha excedido el limite de %d registros por dia (actual: %d)",
                    limiteRegistrosDiarios, registrosUltimas24Horas);
        }

        return CuotaUsuarioDTO.builder()
                .idUsuario(idUsuario)
                .exportacionesUltimaHora(exportacionesUltimaHora)
                .limiteExportacionesPorHora(limiteExportacionesPorHora)
                .registrosUltimas24Horas(registrosUltimas24Horas)
                .limiteRegistrosDiarios(limiteRegistrosDiarios)
                .cuotaDisponible(disponible)
                .motivoBloqueo(motivo)
                .build();
    }
}
