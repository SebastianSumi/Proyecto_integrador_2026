package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.exception.AlertasClinicasException;

public class GestionSlaEnrutamientoException extends AlertasClinicasException {

    public GestionSlaEnrutamientoException(String message, String codigoError, String mensajeUsuario) {
        super(message, codigoError, mensajeUsuario);
    }
}