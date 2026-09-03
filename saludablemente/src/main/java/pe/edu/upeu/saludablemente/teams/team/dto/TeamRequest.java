package pe.edu.upeu.saludablemente.teams.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EquipoRequest", description = "Datos para registrar o actualizar un equipo")
public record TeamRequest(
        @JsonProperty("nombre")
        @Schema(description = "Nombre único del equipo", example = "Nutrición")
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,
        @JsonProperty("descripcion")
        @Schema(description = "Descripción opcional del equipo", example = "Equipo de apoyo nutricional")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
