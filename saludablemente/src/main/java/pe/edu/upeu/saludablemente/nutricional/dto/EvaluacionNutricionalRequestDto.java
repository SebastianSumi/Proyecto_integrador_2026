package pe.edu.upeu.saludablemente.nutricional.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EvaluacionNutricionalRequestDto {

    @NotNull
    private Long idPersona;

    @NotNull
    @PastOrPresent
    private LocalDate fechaEvaluacion;

    @Size(max = 10)
    private String periodoSemestral;

    @Size(max = 30)
    private String estadoEvaluacion;

    @Size(max = 300)
    private String observaciones;

    @Valid
    @NotNull
    private DetalleAntropometricoDto detalleAntropometrico;
}