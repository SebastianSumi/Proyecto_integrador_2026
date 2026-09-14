package pe.edu.upeu.saludablemente.recomendaciones_ia.shared.exception;

public class RecomendacionesIAException extends RuntimeException {
    private final String codigoError;
    private final String mensajeUsuario;

    public RecomendacionesIAException(String message, String codigoError, String mensajeUsuario) {
        super(message);
        this.codigoError = codigoError;
        this.mensajeUsuario = mensajeUsuario;
    }

    public String getCodigoError() { return codigoError; }
    public String getMensajeUsuario() { return mensajeUsuario; }
}