package edu.cit.abel.washq.feature.auth.model

import com.google.gson.annotations.SerializedName

data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String
)
