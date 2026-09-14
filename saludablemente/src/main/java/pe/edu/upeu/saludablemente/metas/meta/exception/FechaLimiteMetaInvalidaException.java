package pe.edu.upeu.saludablemente.metas.meta.exception;

import pe.edu.upeu.saludablemente.exception.BusinessValidationException;

public class FechaLimiteMetaInvalidaException extends BusinessValidationException {

    public FechaLimiteMetaInvalidaException(String message) {
        super(message);
    }
}
