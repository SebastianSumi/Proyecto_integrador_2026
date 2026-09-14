package pe.edu.upeu.saludablemente.exception;

public class CifradoArchivoException extends BusinessException {

    public CifradoArchivoException(String mensaje) {
        super(mensaje);
    }

    public CifradoArchivoException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
