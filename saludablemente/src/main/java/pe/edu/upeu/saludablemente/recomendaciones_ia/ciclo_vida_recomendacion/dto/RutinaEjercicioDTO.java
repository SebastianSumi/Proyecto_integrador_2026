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
public class RutinaEjercicioDTO {
    private String enfoquePrincipal;
    private Integer frecuenciaSemanalDias;
    private List<BloqueEjercicioDTO> bloques;
    private List<String> contraindicaciones;

    @SuppressWarnings("unchecked")
    public static RutinaEjercicioDTO fromMap(Map<String, Object> map) {
        if (map == null) return null;

        RutinaEjercicioDTO dto = new RutinaEjercicioDTO();
        dto.setEnfoquePrincipal((String) map.get("enfoquePrincipal"));
        dto.setFrecuenciaSemanalDias((Integer) map.get("frecuenciaSemanalDias"));

        Object bloquesObj = map.get("bloques");
        if (bloquesObj instanceof List) {
            List<Map<String, Object>> bloquesMap = (List<Map<String, Object>>) bloquesObj;
            dto.setBloques(bloquesMap.stream()
                    .map(BloqueEjercicioDTO::fromMap)
                    .toList());
        }

        dto.setContraindicaciones((List<String>) map.get("contraindicaciones"));
        return dto;
    }
}