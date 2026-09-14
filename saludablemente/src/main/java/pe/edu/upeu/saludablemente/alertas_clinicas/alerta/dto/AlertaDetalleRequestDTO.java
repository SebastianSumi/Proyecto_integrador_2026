package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto;

import jakarta.validation.constraints.NotNull;
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
public class AlertaDetalleRequestDTO {

    @NotNull(message = "El tipo de indicador es obligatorio")
    private TipoIndicador tipoIndicador;

    @NotNull(message = "El valor medido es obligatorio")
    private BigDecimal valorMedido;

    private BigDecimal limiteReferencia;
}
