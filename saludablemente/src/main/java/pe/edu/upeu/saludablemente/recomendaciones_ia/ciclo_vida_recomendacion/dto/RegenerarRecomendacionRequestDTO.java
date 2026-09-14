package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.MotivoRegeneracion;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegenerarRecomendacionRequestDTO {

    @NotNull
    private Long idPersona;
    private Long idEvaluacion;
    private String instruccionAdicional;
    private MotivoRegeneracion motivoRegeneracion;
}