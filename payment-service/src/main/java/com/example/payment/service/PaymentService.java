package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.event.PaymentCreatedEvent;
import com.example.payment.event.PaymentEventPublisher;
import com.example.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service layer for payment operations
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new payment
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        logger.debug("Creating payment with request: {}", request);

        // Create payment entity
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDescription(request.getDescription());
        payment.setCustomerId(request.getCustomerId());
        payment.setMerchantId(request.getMerchantId());

        // Save to database
        payment = paymentRepository.save(payment);
        logger.info("Payment saved with ID: {} and status: {}", payment.getId(), payment.getStatus());

        // Publish event asynchronously
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().toString(),
                payment.getCustomerId(),
                payment.getMerchantId()
        );
        eventPublisher.publishPaymentCreated(event);

        return mapToResponse(payment);
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
        return mapToResponse(payment);
    }

    /**
     * Search payments with filters
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> searchPayments(String status, LocalDateTime startDate,
                                                LocalDateTime endDate, Pageable pageable) {
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        Page<Payment> payments = paymentRepository.searchPayments(paymentStatus, startDate, endDate, pageable);
        return payments.map(this::mapToResponse);
    }

    /**
     * Count pending payments for metrics
     */
    @Transactional(readOnly = true)
    public long countPendingPayments() {
        return paymentRepository.countByStatus(PaymentStatus.PENDING);
    }

    /**
     * Map entity to response DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus().toString());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setDescription(payment.getDescription());
        response.setCustomerId(payment.getCustomerId());
        response.setMerchantId(payment.getMerchantId());
        return response;
    }
}