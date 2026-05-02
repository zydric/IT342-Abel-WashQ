package edu.cit.abel.washq.feature.auth.model

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val address: String?,
    val contactNumber: String?
)
