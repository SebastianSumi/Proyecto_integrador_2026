package pe.edu.upeu.saludablemente.exception;

public class StorageExportException extends BusinessException {

    public StorageExportException(String mensaje) {
        super(mensaje);
    }

    public StorageExportException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
