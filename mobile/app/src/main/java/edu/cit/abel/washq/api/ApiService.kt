package edu.cit.abel.washq.api

import edu.cit.abel.washq.model.ApiEnvelope
import edu.cit.abel.washq.model.AuthResponse
import edu.cit.abel.washq.model.LoginRequest
import edu.cit.abel.washq.model.RegisterRequest
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
