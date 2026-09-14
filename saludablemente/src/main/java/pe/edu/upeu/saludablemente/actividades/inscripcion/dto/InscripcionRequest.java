package pe.edu.upeu.saludablemente.actividades.inscripcion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Input contract for registering a person in an activity.
 */
@Getter
@Setter
public class InscripcionRequest {

    @NotNull
    @Positive
    private Long actividadId;

    @NotNull
    @Positive
    private Long personaId;
}
