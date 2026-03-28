package edu.cit.abel.washq.repository

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()

    data class Error(
        val code: String? = null,
        val message: String,
        val statusCode: Int? = null
    ) : Result<Nothing>()
}
