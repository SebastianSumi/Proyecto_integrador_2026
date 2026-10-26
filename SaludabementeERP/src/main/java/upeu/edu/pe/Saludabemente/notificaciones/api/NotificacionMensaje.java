package upeu.edu.pe.Saludabemente.notificaciones.api;

import java.util.Map;

/**
 * Payload que viaja por RabbitMQ desde NotificacionApiImpl hasta
 * NotificacionListener. No se expone fuera del módulo: es un DTO
 * de mensajería interna, no parte del contrato público.
 */
public record NotificacionMensaje(
        Long idPersona,
        String codigoEvento,
        String origenModulo,
        Long idOrigenReferencia,
        Map<String, Object> variables,
        String emailDestino
) {
}