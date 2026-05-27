package edu.cit.abel.washq.feature.timeslot.model

data class TimeSlotDto(
    val id: Long,
    val slotDate: String,
    val startTime: String,
    val endTime: String,
    val maxCapacity: Int,
    val currentBookingCount: Int,
    val isAvailable: Boolean
)
