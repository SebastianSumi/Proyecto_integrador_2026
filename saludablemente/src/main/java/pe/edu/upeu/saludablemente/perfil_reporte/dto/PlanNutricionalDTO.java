package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanNutricionalDTO {
    private String estrategiaGeneral;
    private List<String> pautasClave;
    private List<String> alimentosPrioritarios;
    private List<String> alimentosAReducir;
}
