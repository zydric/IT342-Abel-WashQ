package edu.cit.abel.washq.feature.booking.model

data class PaymentResponse(
    val paymentId: Long,
    val bookingId: Long,
    val amount: Double,
    val status: String,
    val checkoutUrl: String?,
    val currency: String?,
    val paymentMethod: String?
)
