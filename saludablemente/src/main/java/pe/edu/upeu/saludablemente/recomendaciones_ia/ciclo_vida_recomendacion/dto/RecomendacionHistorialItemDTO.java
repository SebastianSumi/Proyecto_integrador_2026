package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.EstadoInferencia;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionHistorialItemDTO {

    private UUID idRecomendacion;
    private String periodoSemestral;
    private LocalDateTime fechaGeneracion;
    private String modeloIaUsado;
    private Boolean vigente;
    private EstadoInferencia estadoInferencia;
}