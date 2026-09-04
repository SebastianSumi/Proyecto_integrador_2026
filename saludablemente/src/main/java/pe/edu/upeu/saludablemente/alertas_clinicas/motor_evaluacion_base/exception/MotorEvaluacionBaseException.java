package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception.AlertasClinicasException;

public class MotorEvaluacionBaseException extends AlertasClinicasException {

    public MotorEvaluacionBaseException(String message, String codigoError, String mensajeUsuario) {
        super(message, codigoError, mensajeUsuario);
    }
}