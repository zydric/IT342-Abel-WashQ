package edu.cit.abel.washq.model

data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String
)
