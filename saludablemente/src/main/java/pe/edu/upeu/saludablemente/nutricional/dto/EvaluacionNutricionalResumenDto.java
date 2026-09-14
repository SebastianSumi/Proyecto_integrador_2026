package pe.edu.upeu.saludablemente.nutricional.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.nutricional.entity.EstadoEvaluacionNutricional;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionNutricionalResumenDto {

    private Long idEvaluacion;
    private Long idPersona;
    private LocalDate fechaEvaluacion;
    private EstadoEvaluacionNutricional estadoEvaluacion;
    private String periodoSemestral;
}