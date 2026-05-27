package edu.cit.abel.washq.shared.api

import edu.cit.abel.washq.feature.auth.model.AuthResponse
import edu.cit.abel.washq.feature.auth.model.LoginRequest
import edu.cit.abel.washq.feature.auth.model.RegisterRequest
import edu.cit.abel.washq.feature.booking.model.BookingRequest
import edu.cit.abel.washq.feature.booking.model.BookingResponse
import edu.cit.abel.washq.feature.catalog.model.ServiceDto
import edu.cit.abel.washq.feature.timeslot.model.TimeSlotDto
import edu.cit.abel.washq.feature.user.model.UserDto
import edu.cit.abel.washq.shared.model.ApiEnvelope
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ── Auth ──
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiEnvelope<AuthResponse>>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiEnvelope<AuthResponse>>

    // ── Services Catalog ──
    @GET("api/v1/services")
    suspend fun getServices(): Response<ApiEnvelope<List<ServiceDto>>>

    // ── Time Slots ──
    @GET("api/v1/slots")
    suspend fun getAvailableSlots(
        @Query("date") date: String
    ): Response<ApiEnvelope<List<TimeSlotDto>>>

    // ── Bookings ──
    @POST("api/v1/bookings")
    suspend fun createBooking(
        @Body request: BookingRequest
    ): Response<ApiEnvelope<BookingResponse>>

    @GET("api/v1/bookings")
    suspend fun getBookings(): Response<ApiEnvelope<List<BookingResponse>>>

    @DELETE("api/v1/bookings/{id}")
    suspend fun cancelBooking(
        @Path("id") id: Long
    ): Response<ApiEnvelope<BookingResponse>>

    // ── User Profile ──
    @GET("auth/me")
    suspend fun getProfile(): Response<ApiEnvelope<UserDto>>

    @PUT("api/v1/users/{id}")
    suspend fun updateProfile(
        @Path("id") id: Long,
        @Body dto: UserDto
    ): Response<ApiEnvelope<UserDto>>

    @Multipart
    @POST("api/v1/users/{id}/avatar")
    suspend fun uploadAvatar(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): Response<ApiEnvelope<Map<String, String>>>
}
