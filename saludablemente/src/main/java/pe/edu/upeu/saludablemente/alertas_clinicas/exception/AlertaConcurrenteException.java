package pe.edu.upeu.saludablemente.alertas_clinicas.exception;

import pe.edu.upeu.saludablemente.exception.BusinessException;

public class AlertaConcurrenteException extends BusinessException {
    public AlertaConcurrenteException(String message) {
        super(message);
    }
}
