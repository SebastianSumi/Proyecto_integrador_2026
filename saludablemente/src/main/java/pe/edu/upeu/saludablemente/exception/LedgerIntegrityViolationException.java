package pe.edu.upeu.saludablemente.exception;

public class LedgerIntegrityViolationException extends BusinessException {

    public LedgerIntegrityViolationException(String mensaje) {
        super(mensaje);
    }
}
