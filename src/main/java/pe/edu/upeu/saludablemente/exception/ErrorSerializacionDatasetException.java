package pe.edu.upeu.saludablemente.exception;

public class ErrorSerializacionDatasetException extends BusinessException {

    public ErrorSerializacionDatasetException(String mensaje) {
        super(mensaje);
    }

    public ErrorSerializacionDatasetException(String formato, String mensaje, Throwable cause) {
        super("[" + formato + "] " + mensaje);
        initCause(cause);
    }

    public ErrorSerializacionDatasetException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
