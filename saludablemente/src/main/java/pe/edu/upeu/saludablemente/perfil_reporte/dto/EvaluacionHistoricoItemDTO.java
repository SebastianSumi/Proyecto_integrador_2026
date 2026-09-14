package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionHistoricoItemDTO {
    private Long idEvaluacion;
    private String periodo;
    private LocalDateTime fecha;
    private BigDecimal imc;
    private Integer nivelGrasaVisceral;
    private BigDecimal porcentajeMusculo;
    private BigDecimal glucosaMgDl;
    private BigDecimal colesterolTotalMgDl;
    private BigDecimal trigliceridosMgDl;
    private Integer presionSistolica;
    private Integer presionDiastolica;
}
