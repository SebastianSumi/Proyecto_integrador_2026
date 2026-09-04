package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.exception;

import java.util.UUID;

public class MotivoDesestimacionRequeridoException extends ResolucionClinicaException {

    public MotivoDesestimacionRequeridoException(UUID idAlerta) {
        super(
                String.format("Motivo de desestimación requerido para alerta %s", idAlerta),
                "MOTIVO_DESESTIMACION_REQUERIDO",
                "Debe seleccionar un motivo clínico para desestimar la alerta"
        );
    }
}