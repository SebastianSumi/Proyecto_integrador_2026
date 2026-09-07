package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "ActividadResponse", description = "Representación pública de una actividad")
public record RespuestaActividad(
        Long id,
        @JsonProperty("nombre") String name,
        @JsonProperty("descripcion") String description,
        @JsonProperty("fecha") LocalDate activityDate,
        @JsonProperty("horaInicio") LocalTime startTime,
        @JsonProperty("horaFin") LocalTime endTime,
        @JsonProperty("lugar") String place,
        @JsonProperty("estado") EstadoActividad state,
        @JsonProperty("usuarioCreadorId") Long creatorUserId
) {
}
