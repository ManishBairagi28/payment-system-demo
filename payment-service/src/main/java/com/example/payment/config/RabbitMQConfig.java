package com.example.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for payment events
 * Sets up exchanges, queues, and dead letter queue
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.payment}")
    private String paymentExchange;

    @Value("${rabbitmq.queue.payment-events}")
    private String paymentEventsQueue;

    @Value("${rabbitmq.queue.payment-events-dlq}")
    private String paymentEventsDlq;

    @Value("${rabbitmq.routing-key.payment-created}")
    private String paymentCreatedRoutingKey;

    /**
     * Payment events exchange
     */
    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(paymentExchange, true, false);
    }

    /**
     * Dead letter exchange for failed messages
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx." + paymentExchange, true, false);
    }

    /**
     * Payment events queue with DLQ configuration
     */
    @Bean
    public Queue paymentEventsQueue() {
        return QueueBuilder.durable(paymentEventsQueue)
                .withArgument("x-dead-letter-exchange", "dlx." + paymentExchange)
                .withArgument("x-dead-letter-routing-key", "dlq.payment-events")
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    /**
     * Dead letter queue for failed payment events
     */
    @Bean
    public Queue paymentEventsDlq() {
        return QueueBuilder.durable(paymentEventsDlq)
                .withArgument("x-message-ttl", 86400000) // 24 hours retention
                .build();
    }

    /**
     * Binding for payment events queue
     */
    @Bean
    public Binding paymentEventsBinding() {
        return BindingBuilder
                .bind(paymentEventsQueue())
                .to(paymentExchange())
                .with(paymentCreatedRoutingKey);
    }

    /**
     * Binding for dead letter queue
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(paymentEventsDlq())
                .to(deadLetterExchange())
                .with("dlq.payment-events");
    }

    /**
     * JSON message converter
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
