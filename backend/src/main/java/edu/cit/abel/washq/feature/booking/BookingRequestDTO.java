package edu.cit.abel.washq.feature.booking;

import java.math.BigDecimal;

public class BookingRequestDTO {

    private Long serviceId;
    private Long timeSlotId;
    private BigDecimal estimatedWeightKg;
    private String specialInstructions;

    public BookingRequestDTO() {}

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }

    public BigDecimal getEstimatedWeightKg() { return estimatedWeightKg; }
    public void setEstimatedWeightKg(BigDecimal estimatedWeightKg) { this.estimatedWeightKg = estimatedWeightKg; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
