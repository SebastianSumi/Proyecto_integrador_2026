package pe.edu.upeu.saludablemente.teams.team.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EquipoResponse", description = "Representación pública de un equipo")
public record TeamResponse(
        Long id,
        @JsonProperty("nombre") String name,
        @JsonProperty("descripcion") String description,
        @JsonProperty("activo") boolean active
) {
}
