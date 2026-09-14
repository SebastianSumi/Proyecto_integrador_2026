package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoHistoricoDTO {
    private String periodo;
    private BigDecimal imc;
    private Integer grasaVisceral;
    private BigDecimal glucosa;
}
