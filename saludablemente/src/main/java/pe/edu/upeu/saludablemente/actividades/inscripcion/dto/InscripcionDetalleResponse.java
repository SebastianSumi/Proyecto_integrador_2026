package pe.edu.upeu.saludablemente.actividades.inscripcion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;

import java.time.LocalDateTime;

/**
 * Enrollment fields embedded in an activity detail response.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionDetalleResponse {

    private Long id;
    private Long personaId;
    private EstadoInscripcion estado;
    private LocalDateTime inscritaEn;
    private LocalDateTime canceladaEn;
}
