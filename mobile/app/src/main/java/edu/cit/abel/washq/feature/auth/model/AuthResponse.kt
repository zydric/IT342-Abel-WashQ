package edu.cit.abel.washq.feature.auth.model

data class AuthResponse(
    val user: UserDto,
    val accessToken: String
)
