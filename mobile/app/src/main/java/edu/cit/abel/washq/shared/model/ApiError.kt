package edu.cit.abel.washq.shared.model

import com.google.gson.JsonElement

data class ApiError(
    val code: String,
    val message: String,
    val details: JsonElement? = null
)
