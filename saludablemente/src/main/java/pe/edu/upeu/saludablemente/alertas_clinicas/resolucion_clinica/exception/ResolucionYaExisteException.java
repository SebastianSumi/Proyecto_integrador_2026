package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.exception;

import java.util.UUID;

public class ResolucionYaExisteException extends ResolucionClinicaException {

    public ResolucionYaExisteException(UUID idAlerta) {
        super(
                String.format("La alerta %s ya tiene una resolución registrada", idAlerta),
                "RESOLUCION_YA_EXISTE",
                "Esta alerta ya ha sido resuelta anteriormente. No se puede resolver dos veces"
        );
    }
}