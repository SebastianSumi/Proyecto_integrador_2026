package pe.edu.upeu.saludablemente.exception;

public class ReporteNoEncontradoException extends ResourceNotFoundException {
    public ReporteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
