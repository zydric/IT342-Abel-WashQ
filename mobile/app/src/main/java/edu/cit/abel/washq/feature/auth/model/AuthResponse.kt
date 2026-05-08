package edu.cit.abel.washq.feature.auth.model

import edu.cit.abel.washq.feature.user.model.UserDto

data class AuthResponse(
    val user: UserDto,
    val accessToken: String
)
