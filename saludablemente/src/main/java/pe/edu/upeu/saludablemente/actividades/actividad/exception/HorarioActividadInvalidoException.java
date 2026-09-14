package pe.edu.upeu.saludablemente.actividades.actividad.exception;

import pe.edu.upeu.saludablemente.exception.BusinessValidationException;

public class HorarioActividadInvalidoException extends BusinessValidationException {

    public HorarioActividadInvalidoException(String message) {
        super(message);
    }
}
