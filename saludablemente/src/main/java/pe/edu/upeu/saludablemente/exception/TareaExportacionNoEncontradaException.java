package pe.edu.upeu.saludablemente.exception;

public class TareaExportacionNoEncontradaException extends ResourceNotFoundException {

    public TareaExportacionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
