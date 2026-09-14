package pe.edu.upeu.saludablemente.actividades.actividad.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

/**
 * Activity count grouped by lifecycle state for an operational date range.
 */
@Getter
@AllArgsConstructor
public class ActividadAgregado {

    private final EstadoActividad estado;
    private final Long total;
}
