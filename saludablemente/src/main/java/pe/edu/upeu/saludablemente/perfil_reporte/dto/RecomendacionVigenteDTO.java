package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionVigenteDTO {
    private UUID idRecomendacion;
    private Boolean vigente;
    private RutinaEjercicioDTO rutinaEjercicio;
    private PlanNutricionalDTO planNutricional;
}
