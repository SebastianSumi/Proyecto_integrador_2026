package upeu.edu.pe.Saludabemente.notificaciones.listener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.exception.DeliveryFallidoException;
import upeu.edu.pe.Saludabemente.notificaciones.api.NotificacionMensaje;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.Notificacion;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.NotificacionDlq;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.NotificacionDlqRepository;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores.DespachadorEmailService;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.despachadores.DespachadorInAppService;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla.PlantillaEngineService;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionListener {

    private final DespachadorInAppService inAppDespachador;
    private final DespachadorEmailService emailDespachador;
    private final PlantillaEngineService plantillaEngine;
    private final NotificacionDlqRepository dlqRepository;

    @Value("${saludablemente.notificaciones.reintentos.intervalo-minutos:10}")
    private int intervaloReintentoMinutos;

    @RabbitListener(queues = "${saludablemente.notificaciones.rabbitmq.queue}")
    public void consumir(NotificacionMensaje mensaje,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            // 1. In-App siempre — es la fuente de verdad
            Notificacion notificacion = inAppDespachador.despachar(
                    mensaje.idPersona(),
                    mensaje.codigoEvento(),
                    mensaje.origenModulo(),
                    mensaje.idOrigenReferencia(),
                    mensaje.variables());

            // 2. Email solo si hay destinatario
            if (mensaje.emailDestino() != null && !mensaje.emailDestino().isBlank()) {
                despacharEmailConFallbackADlq(notificacion, mensaje);
            }

            channel.basicAck(deliveryTag, false);
            log.info("Notificación procesada: evento={} persona={}",
                    mensaje.codigoEvento(), mensaje.idPersona());

        } catch (Exception ex) {
            log.error("Fallo procesando notificación {} para persona {}: {}",
                    mensaje.codigoEvento(), mensaje.idPersona(), ex.getMessage(), ex);
            // nack sin requeue → va al DLX → cola DLQ de Rabbit
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void despacharEmailConFallbackADlq(Notificacion notificacion,
                                               NotificacionMensaje mensaje) {
        try {
            emailDespachador.despachar(
                    mensaje.emailDestino(),
                    mensaje.codigoEvento(),
                    mensaje.variables());
        } catch (DeliveryFallidoException ex) {
            log.warn("Email no enviado a {} (evento={}). Persistiendo en DLQ de tabla.",
                    mensaje.emailDestino(), mensaje.codigoEvento());

            String payload = renderizarPayloadSeguro(mensaje);

            NotificacionDlq dlq = new NotificacionDlq();
            dlq.setIdNotificacion(notificacion.getIdNotificacion());
            dlq.setDestinatario(mensaje.emailDestino());
            dlq.setCodigoEvento(mensaje.codigoEvento());
            dlq.setPayload(payload);
            dlq.setError(truncar(ex.getMessage(), 500));
            dlq.setIntentos(0);
            dlq.setProximoIntento(LocalDateTime.now().plusMinutes(intervaloReintentoMinutos));
            dlq.setFechaCreacion(LocalDateTime.now());
            dlq.setResuelto(Boolean.FALSE);
            dlqRepository.save(dlq);
        }
    }

    private String renderizarPayloadSeguro(NotificacionMensaje mensaje) {
        try {
            return plantillaEngine.renderizarCuerpo(
                    mensaje.codigoEvento(),
                    CanalNotificacion.EMAIL,
                    mensaje.variables());
        } catch (RuntimeException ex) {
            return "[No se pudo renderizar el cuerpo: " + ex.getMessage() + "]";
        }
    }

    private String truncar(String texto, int max) {
        if (texto == null) return null;
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}