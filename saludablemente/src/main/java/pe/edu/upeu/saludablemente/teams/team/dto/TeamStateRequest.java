package pe.edu.upeu.saludablemente.teams.team.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "EstadoEquipoRequest", description = "Estado explícito que debe tener un equipo")
public record TeamStateRequest(
        @JsonProperty("activo")
        @Schema(description = "Estado deseado del equipo", example = "false")
        @NotNull(message = "Active state is required")
        Boolean active
) {
}
