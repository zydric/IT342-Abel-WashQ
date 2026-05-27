package edu.cit.abel.washq.feature.dashboard.model

data class WeatherResponse(
    val city: String,
    val temperature: Double,
    val condition: String,
    val description: String,
    val humidity: Int,
    val iconUrl: String
)
