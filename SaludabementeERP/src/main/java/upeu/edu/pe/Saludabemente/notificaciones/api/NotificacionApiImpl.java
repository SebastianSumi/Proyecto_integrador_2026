package upeu.edu.pe.Saludabemente.notificaciones.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionApiImpl implements NotificacionApi {

    private final RabbitTemplate rabbitTemplate;

    @Value("${saludablemente.notificaciones.rabbitmq.exchange}")
    private String exchange;

    @Override
    public void enviar(Long idPersona,
                       String codigoEvento,
                       String origenModulo,
                       Long idOrigenReferencia,
                       Map<String, Object> variables,
                       String emailDestino) {

        NotificacionMensaje mensaje = new NotificacionMensaje(
                idPersona, codigoEvento, origenModulo, idOrigenReferencia,
                variables, emailDestino);

        String routingKey = "notificacion." + codigoEvento;

        rabbitTemplate.convertAndSend(exchange, routingKey, mensaje);

        log.debug("Notificación publicada: evento={} persona={} origen={}",
                codigoEvento, idPersona, origenModulo);
    }
}