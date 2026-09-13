package pe.edu.upeu.saludablemente.actividades.actividad.exception;

import pe.edu.upeu.saludablemente.exception.BusinessValidationException;

/**
 * Signals invalid filtering or ordering criteria for an activity query.
 */
public class CriterioConsultaActividadInvalidoException extends BusinessValidationException {

    public CriterioConsultaActividadInvalidoException(String message) {
        super(message);
    }
}
