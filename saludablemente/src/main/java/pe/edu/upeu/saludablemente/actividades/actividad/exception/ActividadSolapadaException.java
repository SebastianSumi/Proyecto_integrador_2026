package pe.edu.upeu.saludablemente.actividades.actividad.exception;

import pe.edu.upeu.saludablemente.exception.BusinessConflictException;

public class ActividadSolapadaException extends BusinessConflictException {

    public ActividadSolapadaException(String message) {
        super(message);
    }
}
