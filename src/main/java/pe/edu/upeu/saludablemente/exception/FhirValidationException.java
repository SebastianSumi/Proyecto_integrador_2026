package pe.edu.upeu.saludablemente.exception;

public class FhirValidationException extends BusinessException {

    public FhirValidationException(String mensaje) {
        super(mensaje);
    }

    public FhirValidationException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
