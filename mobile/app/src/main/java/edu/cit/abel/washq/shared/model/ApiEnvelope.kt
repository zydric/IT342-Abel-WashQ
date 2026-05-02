package edu.cit.abel.washq.shared.model

data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T?,
    val error: ApiError?,
    val timestamp: String?
)
