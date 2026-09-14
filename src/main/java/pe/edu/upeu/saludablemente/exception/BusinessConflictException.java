package pe.edu.upeu.saludablemente.exception;

public abstract class BusinessConflictException extends RuntimeException {

    protected BusinessConflictException(String message) {
        super(message);
    }
}
