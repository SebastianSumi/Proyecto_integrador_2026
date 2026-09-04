package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.dto;

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
    private BigDecimal multiplicadorReincidencia;
    private String unidad;
    private String diagnostico;
    private Boolean esAlterado;

    // Constructor para indicadores alterados
    public static IndicadorEvaluadoDTO alterado(TipoIndicador tipo, BigDecimal valor, BigDecimal limite) {
        return IndicadorEvaluadoDTO.builder()
                .tipoIndicador(tipo)
                .valorMedido(valor)
                .limiteReferencia(limite)
                .esAlterado(true)
                .build();
    }

    // Constructor para indicadores normales
    public static IndicadorEvaluadoDTO normal(TipoIndicador tipo, BigDecimal valor, BigDecimal limite) {
        return IndicadorEvaluadoDTO.builder()
                .tipoIndicador(tipo)
                .valorMedido(valor)
                .limiteReferencia(limite)
                .esAlterado(false)
                .build();
    }
}