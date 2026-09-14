package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionVigenteResponseDTO {

    private UUID idRecomendacion;
    private Long idEvaluacionBase;
    private LocalDateTime fechaGeneracion;
    private Boolean vigente;
    private Boolean leida;
    private RutinaEjercicioDTO rutinaEjercicio;
    private PlanNutricionalDTO planNutricional;
}