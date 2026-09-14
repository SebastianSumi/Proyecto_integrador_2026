package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanNutricionalDTO {

    private String estrategiaGeneral;
    private List<String> pautasClave;
    private List<String> alimentosPrioritarios;
    private List<String> alimentosAReducir;

    @SuppressWarnings("unchecked")
    public static PlanNutricionalDTO fromMap(Map<String, Object> map) {
        if (map == null) return null;

        return PlanNutricionalDTO.builder()
                .estrategiaGeneral((String) map.get("estrategiaGeneral"))
                .pautasClave((List<String>) map.get("pautasClave"))
                .alimentosPrioritarios((List<String>) map.get("alimentosPrioritarios"))
                .alimentosAReducir((List<String>) map.get("alimentosAReducir"))
                .build();
    }
}