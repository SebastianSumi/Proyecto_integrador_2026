package pe.edu.upeu.saludablemente.actividades.inscripcion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;

import java.time.LocalDateTime;

/**
 * Public representation of an enrollment.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionResponse {

    private Long id;
    private Long actividadId;
    private Long personaId;
    private EstadoInscripcion estado;
    private LocalDateTime inscritaEn;
    private LocalDateTime canceladaEn;
}
