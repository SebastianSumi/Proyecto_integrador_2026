package pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception;

public abstract class AlertasClinicasException extends RuntimeException {

    private final String codigoError;
    private final String mensajeUsuario;

    public AlertasClinicasException(String message, String codigoError, String mensajeUsuario) {
        super(message);
        this.codigoError = codigoError;
        this.mensajeUsuario = mensajeUsuario;
    }

    public AlertasClinicasException(String message, Throwable cause, String codigoError, String mensajeUsuario) {
        super(message, cause);
        this.codigoError = codigoError;
        this.mensajeUsuario = mensajeUsuario;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }
}