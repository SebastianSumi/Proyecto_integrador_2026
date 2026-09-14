package pe.edu.upeu.saludablemente.exception;

public class ParticionamientoException extends BusinessException {

    public ParticionamientoException(String mensaje) {
        super(mensaje);
    }

    public ParticionamientoException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
