package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception.AlertasClinicasException;

public class ResolucionClinicaException extends AlertasClinicasException {

    public ResolucionClinicaException(String message, String codigoError, String mensajeUsuario) {
        super(message, codigoError, mensajeUsuario);
    }

    public ResolucionClinicaException(String message, Throwable cause, String codigoError, String mensajeUsuario) {
        super(message, cause, codigoError, mensajeUsuario);
    }
}