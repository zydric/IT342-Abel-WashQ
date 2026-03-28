package edu.cit.abel.washq.api

object ApiConfig {
    // Backend currently exposes /auth/* routes, so base URL should not include /api/v1.
    // Emulator default. For physical device, replace with local network IP.
    const val BASE_URL: String = "http://10.0.2.2:8080/"
    const val PREFS_NAME: String = "washq_prefs"
    const val AUTH_TOKEN_KEY: String = "auth_token"
    const val USER_ID_KEY: String = "user_id"
    const val USER_EMAIL_KEY: String = "user_email"
    const val USER_FIRST_NAME_KEY: String = "user_first_name"
}
