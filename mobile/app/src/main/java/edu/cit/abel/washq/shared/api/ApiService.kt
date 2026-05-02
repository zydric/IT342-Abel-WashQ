package edu.cit.abel.washq.shared.api

import edu.cit.abel.washq.shared.model.ApiEnvelope
import edu.cit.abel.washq.feature.auth.model.AuthResponse
import edu.cit.abel.washq.feature.auth.model.LoginRequest
import edu.cit.abel.washq.feature.auth.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiEnvelope<AuthResponse>>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiEnvelope<AuthResponse>>
}
