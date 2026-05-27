package edu.cit.abel.washq.feature.catalog.model

data class ServiceDto(
    val id: Long,
    val name: String,
    val description: String?,
    val pricePerKg: Double,
    val estimatedDurationHours: Double?,
    val isActive: Boolean
)
