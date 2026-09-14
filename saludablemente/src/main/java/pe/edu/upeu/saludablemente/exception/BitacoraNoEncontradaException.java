package pe.edu.upeu.saludablemente.exception;

public class BitacoraNoEncontradaException extends ResourceNotFoundException {

    public BitacoraNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
