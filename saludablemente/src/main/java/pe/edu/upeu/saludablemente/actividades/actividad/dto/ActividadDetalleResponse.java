package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionDetalleResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Read-only activity view with its internal enrollment details.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDetalleResponse {

    private Long id;
    private String nombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String lugar;
    private EstadoActividad estado;
    private Long creadorId;
    private List<InscripcionDetalleResponse> inscripciones;
}
