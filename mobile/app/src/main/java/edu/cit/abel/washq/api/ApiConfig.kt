package edu.cit.abel.washq.api

import edu.cit.abel.washq.BuildConfig

object ApiConfig {
    // Backend currently exposes /auth/* routes, so base URL should not include /api/v1.
    // Trailing slash is normalized to keep Retrofit path joining consistent.
    val BASE_URL: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
    const val PREFS_NAME: String = "washq_prefs"
    const val AUTH_TOKEN_KEY: String = "auth_token"
    const val USER_ID_KEY: String = "user_id"
    const val USER_EMAIL_KEY: String = "user_email"
    const val USER_FIRST_NAME_KEY: String = "user_first_name"
}
