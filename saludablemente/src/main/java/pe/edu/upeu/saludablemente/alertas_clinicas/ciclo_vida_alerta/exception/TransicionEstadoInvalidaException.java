package pe.edu.upeu.saludablemente.alertas_clinicas.ciclo_vida_alerta.exception;

import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.util.UUID;

public class TransicionEstadoInvalidaException extends CicloVidaAlertaException {

    public TransicionEstadoInvalidaException(UUID idAlerta, EstadoAlerta estadoActual, EstadoAlerta estadoDestino) {
        super(
                String.format("Transición inválida para alerta %s: %s -> %s",
                        idAlerta, estadoActual, estadoDestino),
                "TRANSICION_ESTADO_INVALIDA",
                String.format("No se permite cambiar de %s a %s", estadoActual, estadoDestino)
        );
    }

    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje, "TRANSICION_ESTADO_INVALIDA", mensaje);
    }
}