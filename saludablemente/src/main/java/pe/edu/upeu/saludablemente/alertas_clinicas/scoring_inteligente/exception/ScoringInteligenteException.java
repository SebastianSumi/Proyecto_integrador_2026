package pe.edu.upeu.saludablemente.alertas_clinicas.scoring_inteligente.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception.AlertasClinicasException;

public class ScoringInteligenteException extends AlertasClinicasException {

    public ScoringInteligenteException(String message, String codigoError, String mensajeUsuario) {
        super(message, codigoError, mensajeUsuario);
    }
}