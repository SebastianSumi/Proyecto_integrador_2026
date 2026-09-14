package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Public representation of an activity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadResponse {

    private Long id;
    private String nombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String lugar;
    private EstadoActividad estado;
    private Long creadorId;
}
