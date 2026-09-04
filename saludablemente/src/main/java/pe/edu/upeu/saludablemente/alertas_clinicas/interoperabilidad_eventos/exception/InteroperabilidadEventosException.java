package pe.edu.upeu.saludablemente.alertas_clinicas.interoperabilidad_eventos.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception.AlertasClinicasException;

public class InteroperabilidadEventosException extends AlertasClinicasException {

    public InteroperabilidadEventosException(String message, String codigoError, String mensajeUsuario) {
        super(message, codigoError, mensajeUsuario);
    }
}