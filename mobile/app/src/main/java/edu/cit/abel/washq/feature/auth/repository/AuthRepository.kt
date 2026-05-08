package edu.cit.abel.washq.feature.auth.repository

import com.google.gson.reflect.TypeToken
import edu.cit.abel.washq.shared.api.ApiService
import edu.cit.abel.washq.shared.api.RetrofitClient
import edu.cit.abel.washq.shared.model.ApiEnvelope
import edu.cit.abel.washq.feature.auth.model.AuthResponse
import edu.cit.abel.washq.feature.auth.model.LoginRequest
import edu.cit.abel.washq.feature.auth.model.RegisterRequest
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import edu.cit.abel.washq.shared.model.Result

class AuthRepository(
    private val apiService: ApiService
) {

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return executeCall { apiService.register(request) }
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return executeCall { apiService.login(request) }
    }

    private suspend fun executeCall(
        call: suspend () -> Response<ApiEnvelope<AuthResponse>>
    ): Result<AuthResponse> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                Result.Success(body.data)
            } else {
                val parsedError = parseError(response)
                Result.Error(
                    code = parsedError?.error?.code,
                    message = parsedError?.error?.message ?: "",
                    statusCode = response.code()
                )
            }
        } catch (_: IOException) {
            Result.Error(
                code = "NETWORK-001",
                message = ""
            )
        } catch (httpException: HttpException) {
            Result.Error(
                code = "HTTP-${httpException.code()}",
                message = "",
                statusCode = httpException.code()
            )
        } catch (_: Exception) {
            Result.Error(
                code = "SYSTEM-001",
                message = ""
            )
        }
    }

    private fun parseError(response: Response<ApiEnvelope<AuthResponse>>): ApiEnvelope<AuthResponse>? {
        return try {
            val rawError = response.errorBody()?.string().orEmpty()
            if (rawError.isBlank()) {
                null
            } else {
                val type = object : TypeToken<ApiEnvelope<AuthResponse>>() {}.type
                RetrofitClient.gson().fromJson<ApiEnvelope<AuthResponse>>(rawError, type)
            }
        } catch (_: Exception) {
            null
        }
    }
}
