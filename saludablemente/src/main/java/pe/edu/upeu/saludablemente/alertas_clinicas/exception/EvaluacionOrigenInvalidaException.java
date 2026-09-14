package pe.edu.upeu.saludablemente.alertas_clinicas.exception;

import pe.edu.upeu.saludablemente.exception.BusinessException;

public class EvaluacionOrigenInvalidaException extends BusinessException {
    public EvaluacionOrigenInvalidaException(String message) {
        super(message);
    }
}
