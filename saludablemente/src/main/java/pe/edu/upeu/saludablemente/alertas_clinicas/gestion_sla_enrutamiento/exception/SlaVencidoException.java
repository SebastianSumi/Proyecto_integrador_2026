package pe.edu.upeu.saludablemente.alertas_clinicas.gestion_sla_enrutamiento.exception;

import java.time.LocalDateTime;
import java.util.UUID;

public class SlaVencidoException extends GestionSlaEnrutamientoException {

    public SlaVencidoException(UUID idAlerta, LocalDateTime fechaVencimiento) {
        super(
                String.format("SLA vencido para alerta %s (fecha: %s)", idAlerta, fechaVencimiento),
                "SLA_VENCIDO",
                "La alerta ha excedido el tiempo máximo de atención (72 horas)"
        );
    }
}