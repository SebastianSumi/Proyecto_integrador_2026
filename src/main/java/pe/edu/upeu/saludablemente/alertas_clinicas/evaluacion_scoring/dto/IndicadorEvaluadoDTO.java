package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorEvaluadoDTO {
    private TipoIndicador tipoIndicador;
    private BigDecimal valorMedido;
    private BigDecimal limiteReferencia;
    private BigDecimal porcentajeDesviacion;
    private Boolean esAlterado;
    private String diagnostico;
    private String unidad;
    @Builder.Default
    private BigDecimal multiplicadorReincidencia = BigDecimal.ONE;
}
