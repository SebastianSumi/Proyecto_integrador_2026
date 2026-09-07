package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "ActividadRequest", description = "Datos para registrar o actualizar una actividad")
public record SolicitudActividad(
        @JsonProperty("nombre") @NotBlank @Size(max = 100) String name,
        @JsonProperty("descripcion") @Size(max = 300) String description,
        @JsonProperty("fecha") @NotNull LocalDate activityDate,
        @JsonProperty("horaInicio") @NotNull LocalTime startTime,
        @JsonProperty("horaFin") @NotNull LocalTime endTime,
        @JsonProperty("lugar") @NotBlank @Size(max = 100) String place,
        @JsonProperty("usuarioCreadorId") @NotNull @Positive Long creatorUserId
) {
}
