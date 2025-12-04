package com.example.payment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for payment creation request
 */
public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be in ISO 4217 format")
    private String currency;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    // Constructors
    public PaymentRequest() {
    }

    public PaymentRequest(BigDecimal amount, String currency, String description, 
                         String customerId, String merchantId) {
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.customerId = customerId;
        this.merchantId = merchantId;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                ", customerId='" + customerId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                '}';
    }
}
