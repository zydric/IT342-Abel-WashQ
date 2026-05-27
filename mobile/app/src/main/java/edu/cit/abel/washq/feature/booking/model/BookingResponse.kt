package edu.cit.abel.washq.feature.booking.model

import edu.cit.abel.washq.feature.catalog.model.ServiceDto
import edu.cit.abel.washq.feature.timeslot.model.TimeSlotDto
import edu.cit.abel.washq.feature.user.model.UserDto

data class BookingResponse(
    val id: Long,
    val service: ServiceDto?,
    val timeSlot: TimeSlotDto?,
    val user: UserDto?,
    val estimatedWeightKg: Double,
    val specialInstructions: String?,
    val status: String,
    val totalAmount: Double?,
    val paymentUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)
