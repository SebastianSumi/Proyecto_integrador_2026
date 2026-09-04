package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.exception;

import java.util.UUID;

public class AlertaConcurrenteException extends CicloVidaAlertaException {

    public AlertaConcurrenteException(UUID idAlerta) {
        super(
                String.format("Alerta %s modificada concurrentemente", idAlerta),
                "ALERTA_CONCURRENTE",
                "La alerta fue modificada por otro usuario. Por favor recargue la vista"
        );
    }

    public AlertaConcurrenteException(UUID idAlerta, Throwable cause) {
        super(
                String.format("Alerta %s modificada concurrentemente", idAlerta),
                "ALERTA_CONCURRENTE",
                "La alerta fue modificada por otro usuario. Por favor recargue la vista"
        );
    }
}