package edu.cit.abel.washq.feature.user.model

data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String
)
