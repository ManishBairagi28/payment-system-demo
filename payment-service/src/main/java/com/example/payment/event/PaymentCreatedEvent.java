package com.example.payment.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a payment is created
 */
public class PaymentCreatedEvent implements Serializable {

    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String customerId;
    private String merchantId;
    private LocalDateTime timestamp;

    public PaymentCreatedEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public PaymentCreatedEvent(String paymentId, BigDecimal amount, String currency, 
                              String status, String customerId, String merchantId) {
        this();
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.customerId = customerId;
        this.merchantId = merchantId;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
