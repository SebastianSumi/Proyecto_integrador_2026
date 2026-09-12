package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Input contract for creating or updating an activity.
 */
@Getter
@Setter
public class ActividadRequest {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    @NotBlank
    @Size(max = 100)
    private String lugar;

    @NotNull
    @Positive
    private Long creadorId;
}
