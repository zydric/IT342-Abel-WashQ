package edu.cit.abel.washq.feature.booking.model

data class BookingRequest(
    val serviceId: Long,
    val timeSlotId: Long,
    val estimatedWeightKg: Double,
    val specialInstructions: String?
)
