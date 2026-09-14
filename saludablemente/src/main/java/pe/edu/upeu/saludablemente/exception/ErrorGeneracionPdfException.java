package pe.edu.upeu.saludablemente.exception;

public class ErrorGeneracionPdfException extends BusinessException {
    public ErrorGeneracionPdfException(String mensaje) {
        super(mensaje);
    }

    public ErrorGeneracionPdfException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
