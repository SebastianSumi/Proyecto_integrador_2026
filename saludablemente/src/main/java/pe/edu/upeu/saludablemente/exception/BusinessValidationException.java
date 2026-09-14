package pe.edu.upeu.saludablemente.exception;

public abstract class BusinessValidationException extends RuntimeException {

    protected BusinessValidationException(String message) {
        super(message);
    }
}
