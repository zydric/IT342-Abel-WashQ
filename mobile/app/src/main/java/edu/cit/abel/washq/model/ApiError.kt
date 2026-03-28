package edu.cit.abel.washq.model

import com.google.gson.JsonElement

data class ApiError(
    val code: String,
    val message: String,
    val details: JsonElement? = null
)
