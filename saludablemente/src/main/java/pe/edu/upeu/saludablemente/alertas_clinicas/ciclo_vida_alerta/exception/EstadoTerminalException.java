package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.util.UUID;

public class EstadoTerminalException extends CicloVidaAlertaException {

    public EstadoTerminalException(UUID idAlerta, EstadoAlerta estadoActual) {
        super(
                String.format("Alerta %s en estado terminal %s", idAlerta, estadoActual),
                "ESTADO_TERMINAL",
                "La alerta ya ha sido finalizada y no puede modificarse"
        );
    }
}