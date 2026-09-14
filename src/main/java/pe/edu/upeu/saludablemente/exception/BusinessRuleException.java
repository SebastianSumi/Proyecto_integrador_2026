package pe.edu.upeu.saludablemente.exception;

/**
 * Se lanza cuando el dato recibido tiene una forma valida (paso Bean Validation)
 * pero viola una regla de negocio (celular duplicado, credencial inactiva,
 * prueba inexistente en el catalogo, etc.). El GlobalExceptionHandler la
 * traduce a HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String mensaje) {
        super(mensaje);
    }
}