package edu.cit.abel.washq.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponseDTO {

    private Long id;
    private UserDTO user;
    private ServiceDTO service;
    private TimeSlotDTO timeSlot;
    private BigDecimal estimatedWeightKg;
    private String specialInstructions;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String paymentUrl;

    // --- Constructors ---
    public BookingResponseDTO() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public ServiceDTO getService() { return service; }
    public void setService(ServiceDTO service) { this.service = service; }

    public TimeSlotDTO getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlotDTO timeSlot) { this.timeSlot = timeSlot; }

    public BigDecimal getEstimatedWeightKg() { return estimatedWeightKg; }
    public void setEstimatedWeightKg(BigDecimal estimatedWeightKg) { this.estimatedWeightKg = estimatedWeightKg; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}
