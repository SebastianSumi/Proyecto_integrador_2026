package pe.edu.upeu.saludablemente.alertas_clinicas.exception;

import pe.edu.upeu.saludablemente.exception.BusinessException;

public class TransicionEstadoInvalidaException extends BusinessException {
    public TransicionEstadoInvalidaException(String message) {
        super(message);
    }
}
