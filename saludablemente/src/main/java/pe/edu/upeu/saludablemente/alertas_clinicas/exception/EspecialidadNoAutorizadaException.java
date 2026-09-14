package pe.edu.upeu.saludablemente.alertas_clinicas.exception;

import pe.edu.upeu.saludablemente.exception.BusinessException;

public class EspecialidadNoAutorizadaException extends BusinessException {
    public EspecialidadNoAutorizadaException(String message) {
        super(message);
    }
}
