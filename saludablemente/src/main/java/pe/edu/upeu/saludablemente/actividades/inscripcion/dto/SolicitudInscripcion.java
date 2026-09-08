package pe.edu.upeu.saludablemente.actividades.inscripcion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitudInscripcion(
        @JsonProperty("personaId") @NotNull @Positive Long personId
) {
}
