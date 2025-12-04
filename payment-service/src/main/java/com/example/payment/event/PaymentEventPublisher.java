package com.example.payment.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Publisher for payment events to RabbitMQ
 */
@Component
public class PaymentEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.payment}")
    private String paymentExchange;

    @Value("${rabbitmq.routing-key.payment-created}")
    private String paymentCreatedRoutingKey;

    @Autowired
    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish payment created event asynchronously
     */
    @Async
    public void publishPaymentCreated(PaymentCreatedEvent event) {
        try {
            logger.info("Publishing payment created event for payment ID: {}", event.getPaymentId());
            rabbitTemplate.convertAndSend(paymentExchange, paymentCreatedRoutingKey, event);
            logger.info("Successfully published payment created event for payment ID: {}", event.getPaymentId());
        } catch (Exception e) {
            logger.error("Failed to publish payment created event for payment ID: {}", event.getPaymentId(), e);
            // In production, you might want to implement retry logic or store failed events
        }
    }
}
