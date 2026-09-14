package pe.edu.upeu.saludablemente.metas.meta.exception;

import pe.edu.upeu.saludablemente.exception.BusinessConflictException;

public class EstadoMetaNoPermitidoException extends BusinessConflictException {

    public EstadoMetaNoPermitidoException(String message) {
        super(message);
    }
}
