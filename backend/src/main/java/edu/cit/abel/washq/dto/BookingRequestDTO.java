package edu.cit.abel.washq.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BookingRequestDTO {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Time Slot ID is required")
    private Long timeSlotId;

    @NotNull(message = "Estimated weight is required")
    @Min(value = 0, message = "Estimated weight must be positive")
    private BigDecimal estimatedWeightKg;

    private String specialInstructions;

    // --- Constructors ---

    public BookingRequestDTO() {}

    // --- Getters and Setters ---

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }

    public BigDecimal getEstimatedWeightKg() { return estimatedWeightKg; }
    public void setEstimatedWeightKg(BigDecimal estimatedWeightKg) { this.estimatedWeightKg = estimatedWeightKg; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
