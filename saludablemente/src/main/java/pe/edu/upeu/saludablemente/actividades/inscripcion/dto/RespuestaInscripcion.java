package pe.edu.upeu.saludablemente.actividades.inscripcion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;

import java.time.LocalDateTime;

public record RespuestaInscripcion(
        Long id,
        @JsonProperty("actividadId") Long activityId,
        @JsonProperty("personaId") Long personId,
        @JsonProperty("estado") EstadoInscripcion state,
        @JsonProperty("fechaInscripcion") LocalDateTime enrolledAt,
        @JsonProperty("fechaCancelacion") LocalDateTime cancelledAt
) {
}
