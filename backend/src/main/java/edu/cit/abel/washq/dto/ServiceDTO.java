package edu.cit.abel.washq.dto;

import java.math.BigDecimal;

/**
 * Read-only projection of a Service returned to callers.
 * Field names are camelCase so Jackson serialises them correctly for the frontend.
 */
public class ServiceDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerKg;
    private Integer estimatedDurationHours;
    private Boolean isActive;

    // --- Constructors ---

    public ServiceDTO() {}

    public ServiceDTO(Long id, String name, String description,
                      BigDecimal pricePerKg, Integer estimatedDurationHours,
                      Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pricePerKg = pricePerKg;
        this.estimatedDurationHours = estimatedDurationHours;
        this.isActive = isActive;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerKg() { return pricePerKg; }
    public void setPricePerKg(BigDecimal pricePerKg) { this.pricePerKg = pricePerKg; }

    public Integer getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
