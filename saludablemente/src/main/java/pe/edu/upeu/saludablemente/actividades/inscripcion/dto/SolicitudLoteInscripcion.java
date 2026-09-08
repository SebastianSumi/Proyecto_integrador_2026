package pe.edu.upeu.saludablemente.actividades.inscripcion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SolicitudLoteInscripcion(
        @JsonProperty("personaIds") @NotEmpty @Size(max = 100) List<@Positive Long> personIds
) {
}
