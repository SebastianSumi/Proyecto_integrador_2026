package upeu.edu.pe.Saludabemente.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${saludablemente.notificaciones.rabbitmq.exchange}")
    private String exchange;

    @Value("${saludablemente.notificaciones.rabbitmq.queue}")
    private String queue;

    @Value("${saludablemente.notificaciones.rabbitmq.routing-key}")
    private String routingKey;

    @Value("${saludablemente.notificaciones.rabbitmq.dlx}")
    private String dlx;

    @Value("${saludablemente.notificaciones.rabbitmq.dlq}")
    private String dlq;

    @Value("${saludablemente.notificaciones.rabbitmq.dlq-routing-key}")
    private String dlqRoutingKey;

    // ── JSON converter: los mensajes viajan como JSON, no Java serializado ──
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ── Exchange principal (topic, durable) ──
    @Bean
    TopicExchange notificacionesExchange() {
        return new TopicExchange(exchange, true, false);
    }

    // ── Cola principal con dead-letter hacia el DLX ──
    @Bean
    Queue notificacionesQueue() {
        return QueueBuilder.durable(queue)
                .deadLetterExchange(dlx)
                .deadLetterRoutingKey(dlqRoutingKey)
                .build();
    }

    @Bean
    Binding notificacionesBinding(Queue notificacionesQueue,
                                  TopicExchange notificacionesExchange) {
        return BindingBuilder.bind(notificacionesQueue)
                .to(notificacionesExchange)
                .with(routingKey);
    }

    // ── DLX + DLQ (mensajes que no se pueden procesar) ──
    @Bean
    DirectExchange notificacionesDlx() {
        return new DirectExchange(dlx, true, false);
    }

    @Bean
    Queue notificacionesDlq() {
        return QueueBuilder.durable(dlq).build();
    }

    @Bean
    Binding dlqBinding(Queue notificacionesDlq, DirectExchange notificacionesDlx) {
        return BindingBuilder.bind(notificacionesDlq)
                .to(notificacionesDlx)
                .with(dlqRoutingKey);
    }
}