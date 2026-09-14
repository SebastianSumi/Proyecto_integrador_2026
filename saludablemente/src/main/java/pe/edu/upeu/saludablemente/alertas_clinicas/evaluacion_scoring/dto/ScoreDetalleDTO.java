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
public class ScoreDetalleDTO {
    private TipoIndicador tipoIndicador;
    private BigDecimal valorMedido;
    private BigDecimal porcentajeDesviacion;
    private Integer puntosBase;
    private BigDecimal multiplicadorReincidencia;
    private Integer puntosFinales;
    private String justificacion;
}
