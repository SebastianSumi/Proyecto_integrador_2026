package pe.edu.upeu.saludablemente.actividades.inscripcion.exception;

import pe.edu.upeu.saludablemente.exception.BusinessConflictException;

public class InscripcionVigenteException extends BusinessConflictException {

    public InscripcionVigenteException(String message) {
        super(message);
    }
}
