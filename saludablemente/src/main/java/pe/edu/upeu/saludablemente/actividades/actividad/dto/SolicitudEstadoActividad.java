package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

@Schema(name = "ActividadEstadoRequest", description = "Estado deseado para una actividad")
public record SolicitudEstadoActividad(
        @JsonProperty("estado") @NotNull EstadoActividad state
) {
}
