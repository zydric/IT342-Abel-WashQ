package edu.cit.abel.washq.feature.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponseDTO {
    private Long paymentId;
    private Long bookingId;
    private BigDecimal amount;
    private String status;
    private String checkoutUrl;
    private LocalDateTime createdAt;
    private String currency;
    private String paymentMethod;

    public PaymentResponseDTO() {}

    public PaymentResponseDTO(Long paymentId, Long bookingId, BigDecimal amount,
                              String status, String checkoutUrl, LocalDateTime createdAt,
                              String currency, String paymentMethod) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.checkoutUrl = checkoutUrl;
        this.createdAt = createdAt;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
    }

    // --- Getters and Setters ---
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
