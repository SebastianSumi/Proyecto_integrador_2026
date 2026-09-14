package pe.edu.upeu.saludablemente.exception;

public class ColdStorageOperationException extends BusinessException {

    public ColdStorageOperationException(String mensaje) {
        super(mensaje);
    }

    public ColdStorageOperationException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
