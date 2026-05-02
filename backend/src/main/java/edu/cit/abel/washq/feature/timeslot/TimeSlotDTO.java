package edu.cit.abel.washq.feature.timeslot;

/**
 * API response shape for a time slot.
 */
public class TimeSlotDTO {

    private Long id;
    private String slotDate;              // "yyyy-MM-dd"
    private String startTime;             // "HH:mm"
    private String endTime;               // "HH:mm"
    private Integer maxCapacity;
    private Integer currentBookingCount;
    private Boolean isAvailable;

    public TimeSlotDTO() {}

    public TimeSlotDTO(Long id, String slotDate, String startTime, String endTime,
                       Integer maxCapacity, Integer currentBookingCount, Boolean isAvailable) {
        this.id = id;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.currentBookingCount = currentBookingCount;
        this.isAvailable = isAvailable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlotDate() { return slotDate; }
    public void setSlotDate(String slotDate) { this.slotDate = slotDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public Integer getCurrentBookingCount() { return currentBookingCount; }
    public void setCurrentBookingCount(Integer currentBookingCount) { this.currentBookingCount = currentBookingCount; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
