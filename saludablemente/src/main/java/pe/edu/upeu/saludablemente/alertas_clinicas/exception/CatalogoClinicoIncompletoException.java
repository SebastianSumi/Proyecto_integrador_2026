package pe.edu.upeu.saludablemente.alertas_clinicas.exception;

import pe.edu.upeu.saludablemente.exception.BusinessException;

public class CatalogoClinicoIncompletoException extends BusinessException {
    public CatalogoClinicoIncompletoException(String message) {
        super(message);
    }
}
