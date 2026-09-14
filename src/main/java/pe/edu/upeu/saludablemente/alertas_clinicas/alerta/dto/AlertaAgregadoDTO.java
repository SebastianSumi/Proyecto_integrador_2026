package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class AlertaAgregadoDTO {

    private Long totalAlertas;
    private Long totalCriticas;
    private Long totalPendientes;
    private Long totalEnRevision;
    private Long totalAtendidas;
    private Long totalDesestimadas;
    private Long totalVencidas;
    private Double promedioScoreRiesgo;

    public AlertaAgregadoDTO(Long totalAlertas, Long totalCriticas, Long totalPendientes,
                             Long totalEnRevision, Long totalAtendidas, Long totalDesestimadas,
                             Long totalVencidas, Double promedioScoreRiesgo) {
        this.totalAlertas = totalAlertas != null ? totalAlertas : 0L;
        this.totalCriticas = totalCriticas != null ? totalCriticas : 0L;
        this.totalPendientes = totalPendientes != null ? totalPendientes : 0L;
        this.totalEnRevision = totalEnRevision != null ? totalEnRevision : 0L;
        this.totalAtendidas = totalAtendidas != null ? totalAtendidas : 0L;
        this.totalDesestimadas = totalDesestimadas != null ? totalDesestimadas : 0L;
        this.totalVencidas = totalVencidas != null ? totalVencidas : 0L;
        this.promedioScoreRiesgo = promedioScoreRiesgo != null ? promedioScoreRiesgo : 0.0;
    }
}
