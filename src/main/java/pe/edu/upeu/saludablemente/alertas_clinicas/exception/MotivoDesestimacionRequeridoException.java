package pe.edu.upeu.saludablemente.alertas_clinicas.exception;

import pe.edu.upeu.saludablemente.exception.BusinessException;

public class MotivoDesestimacionRequeridoException extends BusinessException {
    public MotivoDesestimacionRequeridoException(String message) {
        super(message);
    }
}
