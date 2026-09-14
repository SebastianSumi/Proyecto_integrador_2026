package pe.edu.upeu.saludablemente.exception;

public class HashCalculationException extends BusinessException {

    public HashCalculationException(String mensaje) {
        super(mensaje);
    }

    public HashCalculationException(String mensaje, Throwable cause) {
        super(mensaje);
        initCause(cause);
    }
}
