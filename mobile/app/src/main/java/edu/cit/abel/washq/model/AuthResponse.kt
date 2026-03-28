package edu.cit.abel.washq.model

data class AuthResponse(
    val user: UserDto,
    val accessToken: String
)
