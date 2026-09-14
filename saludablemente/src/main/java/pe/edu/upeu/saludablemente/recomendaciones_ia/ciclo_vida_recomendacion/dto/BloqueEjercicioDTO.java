package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueEjercicioDTO {

    private String tipo;
    private Integer duracionMinutos;
    private String intensidad;
    private String descripcion;

    @SuppressWarnings("unchecked")
    public static BloqueEjercicioDTO fromMap(Map<String, Object> map) {
        if (map == null) return null;
        return BloqueEjercicioDTO.builder()
                .tipo((String) map.get("tipo"))
                .duracionMinutos((Integer) map.get("duracionMinutos"))
                .intensidad((String) map.get("intensidad"))
                .descripcion((String) map.get("descripcion"))
                .build();
    }
}