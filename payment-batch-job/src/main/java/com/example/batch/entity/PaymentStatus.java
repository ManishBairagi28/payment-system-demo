package com.example.batch.entity;

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
