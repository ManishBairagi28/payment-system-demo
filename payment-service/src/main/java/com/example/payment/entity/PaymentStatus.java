package com.example.payment.entity;

/**
 * Enum representing possible payment statuses
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED,
    REFUNDED
}
