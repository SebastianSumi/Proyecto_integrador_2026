package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Flat operational projection used to search scheduled activities.
 */
@Getter
@AllArgsConstructor
public class ActividadResumen {

    private final Long id;
    private final String nombre;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final String lugar;
    private final EstadoActividad estado;
}
