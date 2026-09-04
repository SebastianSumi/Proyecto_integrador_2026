package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception.AlertasClinicasException;

public class CicloVidaAlertaException extends AlertasClinicasException {

    public CicloVidaAlertaException(String message, String codigoError, String mensajeUsuario) {
        super(message, codigoError, mensajeUsuario);
    }
}