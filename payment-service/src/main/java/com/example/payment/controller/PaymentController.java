package com.example.payment.controller;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.service.PaymentService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller for payment operations
 * Handles payment creation, retrieval, and search
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public PaymentController(PaymentService paymentService, MeterRegistry meterRegistry) {
        this.paymentService = paymentService;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Create a new payment
     * Includes custom metrics for business monitoring
     */
    @PostMapping
    @Timed(value = "payment.request.duration", description = "Time taken to process payment creation")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        MDC.put("operation", "createPayment");

        logger.info("Creating payment with amount: {} {}", request.getAmount(), request.getCurrency());

        try {
            PaymentResponse response = paymentService.createPayment(request);

            // Record business metrics
            meterRegistry.counter("payments.created.total",
                            "status", response.getStatus(),
                            "currency", response.getCurrency(),
                            "service", "payment-service",
                            "environment", "local")
                    .increment();

            MDC.put("paymentId", response.getId());
            logger.info("Payment created successfully with ID: {}", response.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating payment", e);
            meterRegistry.counter("payments.failed.total",
                            "service", "payment-service",
                            "environment", "local")
                    .increment();
            throw e;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String id) {
        MDC.put("paymentId", id);
        MDC.put("operation", "getPaymentById");

        logger.info("Retrieving payment with ID: {}", id);

        try {
            PaymentResponse response = paymentService.getPaymentById(id);
            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Search payments with filters
     */
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> searchPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {

        MDC.put("operation", "searchPayments");
        logger.info("Searching payments with status: {}, startDate: {}, endDate: {}", status, startDate, endDate);

        try {
            Page<PaymentResponse> payments = paymentService.searchPayments(status, startDate, endDate, pageable);

            // Update gauge for pending payments
            long pendingCount = paymentService.countPendingPayments();
            meterRegistry.gauge("payments.pending.count", pendingCount);

            return ResponseEntity.ok(payments);
        } finally {
            MDC.clear();
        }
    }
}