package pe.edu.upeu.saludablemente.nutricional.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionNutricionalResponseDto {

    private Long idEvaluacion;
    private Long idPersona;
    private LocalDate fechaEvaluacion;
    private String periodoSemestral;
    private String estadoEvaluacion;
    private String observaciones;
    private DetalleAntropometricoDto detalleAntropometrico;
    private DetalleBioquimicoDto detalleBioquimico;
}